# The rail model after the 2.0 update

The [paper](./Studienarbeit.pdf) describes how the tool read blueprints before
Factorio 2.0 (sections 5.3.2 and 5.3.3). Chapter 5.3.3 is honest about the cost
of that approach: for every rail shape in every orientation, the positions of
every rail that could connect to it or cross it were read out of the game by
hand in debug mode and written into `LookUp.java` as a `switch` case. Fourteen
cases, "at least 60 LookUp objects" for the crossing table alone, and the paper
calls the implementation effort "erheblich".

2.0 made that approach untenable rather than merely tedious. The update replaced
one curve with two (`curved-rail-a`, `curved-rail-b`), added a rail at the 1:2
slope (`half-diagonal-rail`), kept the old shapes under a `legacy-` prefix, added
an elevated layer, and widened the direction enum from eight values to sixteen -
so the table would have had to be measured again, several times over.

This document describes what replaced it.

## The idea

A rail has exactly two ends a train can enter it through, and Factorio
guarantees that **every rail connection point lands on an integer tile
coordinate** ([FFF-377](https://factorio.com/blog/post/fff-377)). So a rail can
be described once, by where its two ends are and which way they face, and
everything the analyser needs follows:

| Question | Answer |
| --- | --- |
| Do two rails connect? | An end of one sits on an end of the other, facing the opposite way. |
| Does a signal separate two rails? | The signal stands at one particular end of its rail, and separates it from whatever is attached to that end. |
| Do two rails cross? | Their centre lines meet somewhere that is not a shared end. |

The description lives in
[`rail/RailGeometry.java`](../app/src/main/java/factorio/train/analyser/rail/RailGeometry.java),
and only one orientation of each shape is written down - the other seven are
rotations of it. Adding the three new 2.0 shapes took three lines rather than
three `switch` blocks.

## The ends

Offsets are from the entity's own blueprint position; `h` is the *heading*, the
direction pointing out of the rail there, in 2.0's 16-value direction enum. Two
rails connect when their ends coincide and their headings differ by 8.

| Rail | Direction | End A | End B |
| --- | --- | --- | --- |
| `straight-rail` | 0 `│` | (0,-1) h0 | (0,1) h8 |
| | 2 `╱` | (1,-1) h2 | (-1,1) h10 |
| | 4 `─` | (1,0) h4 | (-1,0) h12 |
| | 6 `╲` | (1,1) h6 | (-1,-1) h14 |
| `half-diagonal-rail` | 0 | (1,2) h7 | (-1,-2) h15 |
| | 2 | (1,-2) h1 | (-1,2) h9 |
| | 4 | (2,-1) h3 | (-2,1) h11 |
| | 6 | (2,1) h5 | (-2,-1) h13 |
| `curved-rail-a` | 0 | (0,2) h8 | (-1,-3) h15 |
| | 2 | (0,2) h8 | (1,-3) h1 |
| `curved-rail-b` | 0 | (1,2) h7 | (-2,-2) h14 |
| | 2 | (-1,2) h9 | (2,-2) h2 |
| `legacy-straight-rail` | 0, 4 | as `straight-rail` | |
| | 2 `╲` upper half | (0,-1) h14 | (1,0) h6 |
| | 10 `╲` lower half | (-1,0) h14 | (0,1) h6 |
| | 6 `╱` lower half | (1,0) h2 | (0,1) h10 |
| | 14 `╱` upper half | (0,-1) h2 | (-1,0) h10 |
| `legacy-curved-rail` | 0 | (1,4) h8 | (-2,-3) h14 |
| | 2 | (-1,4) h8 | (2,-3) h2 |

Directions 4, 8 and 12 are the listed direction-0 row rotated clockwise; 6, 10
and 14 are the direction-2 row rotated. Odd headings are the 1:2 slopes a
half-diagonal end leaves at - 26.565°, which Factorio reports as the nearest
direction value rather than the nominal 22.5°, so the tangents in
`Direction.java` are the exact slopes and not the nominal angles.

Two things worth knowing from the table:

- A 2.0 **45° turn** is `curved-rail-a` + `curved-rail-b`, and a 90° turn is two
  of those back to back, the pair of `curved-rail-b` meeting at their 45° ends.
  Splitting the curve is what lets opposite curves meet in the middle to form an
  S-bend with a signal between them. The composite 45° curve offsets by 9 tiles
  along the approach and 4 across, an effective radius of 9√2 - which matches
  what the developers said about the shape.
- A **legacy diagonal** covers only half of the 2×2 block it sits in, which is
  why a 1.1 45° run alternates between two direction values. That is the detail
  the old code encoded as neighbours at (0,±2) and (±2,0).

## How the table was checked

The offsets were derived from the geometry and then verified against three
independently published 2.0 blueprint books, around 55 000 rail ends in total.
Every end at a diagonal or half-diagonal heading matches another end exactly.
The only unmatched ends are orthogonal ones running off the edge of a blueprint,
and elevated rails meeting a ramp - both expected.

The test suite checks the same property from the other side: for each real
blueprint it asserts that every ground rail ends up in some node of the model,
which fails if any connection is missed.

## Signals

Where a signal may sit is engine behaviour that the geometry does not predict,
so [`rail/SignalSpots.java`](../app/src/main/java/factorio/train/analyser/rail/SignalSpots.java)
is measured data: 152 placements across 38 rail orientations, probed against
Factorio 2.1.12 by the
[Factorio Blueprint Editor](https://github.com/FactoryGameFan/factorio-blueprint-editor)
project.

A signal faces the trains it governs, so its direction is *opposite* to the
travel direction it permits. Combined with the ends, that gives the rule in
`RailNetwork`: a signal whose direction faces into its rail lets trains out
through the end it stands at, and one facing out of the rail lets them in. This
replaces the part of the 1.1 implementation the paper spends the most space on -
`isInsideCurrentSection()`, with its 4.3-tile distance threshold, its 0.3-tile
offset for curves, and its hardcoded list of curve-meets-diagonal combinations
(paper, figures 38 to 41). Those were all approximations of "which end is the
signal at", which the geometry now answers exactly.

## Crossings

Two rails cross when their centre lines intersect somewhere that is not a shared
end. A point that is an end of *both* rails is a junction - rails running into
each other, or diverging from the same spot - and not a crossing.

Straight rails are a single segment. Curves are approximated by the cubic Bézier
through both ends that matches their tangents, stretched until it is as long as
the arc length the developers published for that piece. Factorio's curves are
not circular arcs - they are fitted with polynomials so the ends land on whole
tiles - and `curved-rail-b` in particular bows about twice as far off its chord
as a circular arc between the same tangents would, so matching the length
matters. What is left is a few hundredths of a tile, far inside what decides
whether two rails cross, since rails cross transversally rather than grazing
each other.

## Elevated rails

The four elevated shapes are recognised and carry the same geometry as their
ground counterparts, on a layer of their own, so rails only ever connect to
rails on the same layer. They are **left out by default**:
`new Graph(blueprint)` analyses the ground network and reports which elevated
rails it skipped.

To finish the job, two things are needed:

1. `rail-ramp` has to be measured - it is the piece that joins the two layers,
   and without it an elevated network comes out disconnected from the ground one.
   `RailGeometry.UNMAPPED_RAILS` is where it goes.
2. `SignalSpots` has no entries for the elevated shapes, because placing one in
   the game needs a rail support and the probe could not sweep signals around
   them. Until that is measured an elevated rail gets no signals at all, which
   would merge blocks that the game keeps apart.

`rail-support` itself carries no train and needs nothing beyond being ignored.
Once the two items above exist, `new Graph(blueprint, true)` is the whole switch.

## Blueprints from before the update

A blueprint exported before 2.0 states the old rail names and the old
eight-value direction enum. `LegacyBlueprint` converts it the way the game does
on import - renaming the rails with a `legacy-` prefix and doubling every
direction - so blueprints collected for the original study still work. The
conversion is reported on the console so it is never silent.
