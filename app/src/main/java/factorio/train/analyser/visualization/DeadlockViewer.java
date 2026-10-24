package factorio.train.analyser.visualization;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

import factorio.train.analyser.rail.RailEnd;
import factorio.train.analyser.rail.RailGeometry;

/**
 * Graphical view of a Factorio rail network with the detected deadlocks highlighted.
 *
 * Input is the JSON output of the analysis:
 * {"result":[{"deadlockPath":[Node...],"isDeadlock":true}], "graph":{"sections":[{"nodes":[...]}]}}
 *
 * Coordinates: the positions in the JSON are Factorio tiles, exactly as the blueprint states them,
 * so they may be negative. The shape of each rail piece comes from RailGeometry, which is the same
 * geometry the analysis itself is built on - so the drawing cannot drift away from the model, and
 * every rail Factorio 2.0 has is covered, including the elevated ones.
 *
 * Trains: the deadlock path contains the nodes visited by the algorithm (recursion order). From it a
 * smallest waiting cycle is reconstructed: trains in nodes of different sections, each of which wants
 * to enter a section that is occupied by the next train. This is a reconstruction for illustration,
 * not a simulation.
 *
 * Usage:
 *   DeadlockViewer.show(jsonString);                      // from the application
 *   DeadlockViewer.show(new File("output.json"));
 *   java -cp app.jar ...DeadlockViewer output.json        // standalone
 *   java -cp app.jar ...DeadlockViewer output.json --png out.png [--deadlock 1] [--select 12]
 */
public final class DeadlockViewer {

    /** Which level a rail piece runs on. */
    static final int GROUND = 0, RAMP = 1, ELEVATED = 2;

    /** How far an elevated piece's shadow falls, in tiles. */
    private static final double SHADOW_OFFSET = 1.1;

    private DeadlockViewer() {
    }

    // ------------------------------------------------------------------------------------------
    // Data model
    // ------------------------------------------------------------------------------------------

    static final class Track {
        final int id;
        final String name;
        final int dir;
        final double x, y;     // in tiles, as the blueprint states it
        final Path2D shape;
        final Point2D mid;
        final double[][] ends; // end points (tiles) of the rail piece
        final double[][] line; // centre line, running ground end first on a ramp
        final int level;       // GROUND, RAMP or ELEVATED
        Shape railsCache, tiesCache, shadowCache;

        Track(int id, String name, int dir, double x, double y) {
            this.id = id;
            this.name = name;
            this.dir = dir;
            this.x = x;
            this.y = y;
            Geometry g = Geometry.of(name, dir, x, y);
            this.shape = g.shape;
            this.mid = g.mid;
            this.ends = g.ends;
            this.line = g.line;
            this.level = name == null || !RailGeometry.isRail(name) ? GROUND
                    : RailGeometry.isRamp(name) ? RAMP
                    : RailGeometry.isElevated(name) ? ELEVATED : GROUND;
        }

        /**
         * The shadow this piece casts on the ground, which is what tells the
         * two levels apart. A ramp's shadow grows from nothing at its ground
         * end to full at its elevated end, so it also shows which way it climbs.
         */
        Shape shadow() {
            if (level == GROUND) return null;
            if (shadowCache == null) {
                List<double[]> pts = resample(line, level == RAMP ? 24 : line.length);
                Path2D.Double p = new Path2D.Double();
                for (int i = 0; i < pts.size(); i++) {
                    double lift = level == RAMP ? (double) i / (pts.size() - 1) : 1.0;
                    double[] q = pts.get(i);
                    double sx = q[0] + SHADOW_OFFSET * lift, sy = q[1] + SHADOW_OFFSET * lift;
                    if (i == 0) p.moveTo(sx, sy); else p.lineTo(sx, sy);
                }
                shadowCache = new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                        .createStrokedShape(p);
            }
            return shadowCache;
        }

        /** Spreads count points evenly along a polyline by arc length. */
        static List<double[]> resample(double[][] line, int count) {
            List<double[]> out = new ArrayList<>();
            double total = 0;
            double[] cum = new double[line.length];
            for (int i = 1; i < line.length; i++) {
                total += Math.hypot(line[i][0] - line[i - 1][0], line[i][1] - line[i - 1][1]);
                cum[i] = total;
            }
            if (total <= 0) {
                out.add(new double[]{line[0][0], line[0][1]});
                out.add(new double[]{line[0][0], line[0][1]});
                return out;
            }
            int steps = Math.max(2, count);
            int seg = 0;
            for (int i = 0; i < steps; i++) {
                double want = total * i / (steps - 1);
                while (seg < line.length - 2 && cum[seg + 1] < want) seg++;
                double span = cum[seg + 1] - cum[seg];
                double f = span <= 0 ? 0 : (want - cum[seg]) / span;
                out.add(new double[]{
                        line[seg][0] + (line[seg + 1][0] - line[seg][0]) * f,
                        line[seg][1] + (line[seg + 1][1] - line[seg][1]) * f});
            }
            return out;
        }

        /** The two rail strands (as an area, cached). */
        Shape rails() {
            if (railsCache == null) {
                Area a = new Area(new BasicStroke(1.04f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND).createStrokedShape(shape));
                a.subtract(new Area(new BasicStroke(0.80f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND).createStrokedShape(shape)));
                railsCache = a;
            }
            return railsCache;
        }

        /** The ties (as an area, cached). */
        Shape ties() {
            if (tiesCache == null) {
                tiesCache = new BasicStroke(1.55f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f,
                        new float[]{0.20f, 0.62f}, 0.1f).createStrokedShape(shape);
            }
            return tiesCache;
        }
    }

    static final class Node {
        final int id;
        final double length;   // arc length in tiles
        final boolean input, output;
        final List<Integer> trackIds = new ArrayList<>();
        final List<List<Integer>> next = new ArrayList<>();
        final List<List<Integer>> protectedFrom = new ArrayList<>();
        int sectionId = -1;

        Node(int id, double length, boolean input, boolean output) {
            this.id = id;
            this.length = length;
            this.input = input;
            this.output = output;
        }

        Set<Integer> nextFlat() {
            Set<Integer> s = new LinkedHashSet<>();
            for (List<Integer> l : next) s.addAll(l);
            return s;
        }

        boolean outputOnly() {
            return output && !input;
        }
    }

    static final class Result {
        final List<Integer> path = new ArrayList<>();
        boolean deadlock;
    }

    /** Centre line of a node as a polyline (tiles). */
    static final class Polyline {
        final double[] x, y, cum;
        final double length;

        Polyline(List<double[]> pts) {
            int n = pts.size();
            x = new double[n];
            y = new double[n];
            cum = new double[n];
            for (int i = 0; i < n; i++) {
                x[i] = pts.get(i)[0];
                y[i] = pts.get(i)[1];
                if (i > 0) cum[i] = cum[i - 1] + Math.hypot(x[i] - x[i - 1], y[i] - y[i - 1]);
            }
            length = cum[n - 1];
        }

        /** {x, y, tangentX, tangentY} at arc length s. */
        double[] at(double s) {
            s = Math.max(0, Math.min(length, s));
            int i = 1;
            while (i < x.length - 1 && cum[i] < s) i++;
            double seg = cum[i] - cum[i - 1];
            double t = seg < 1e-9 ? 0 : (s - cum[i - 1]) / seg;
            double tx = seg < 1e-9 ? 1 : (x[i] - x[i - 1]) / seg;
            double ty = seg < 1e-9 ? 0 : (y[i] - y[i - 1]) / seg;
            return new double[]{x[i - 1] + (x[i] - x[i - 1]) * t, y[i - 1] + (y[i] - y[i - 1]) * t, tx, ty};
        }

        Path2D sub(double s0, double s1) {
            Path2D.Double p = new Path2D.Double();
            double[] a = at(s0);
            p.moveTo(a[0], a[1]);
            for (int i = 1; i < x.length - 1; i++) {
                if (cum[i] > s0 + 1e-9 && cum[i] < s1 - 1e-9) p.lineTo(x[i], y[i]);
            }
            double[] b = at(s1);
            p.lineTo(b[0], b[1]);
            return p;
        }
    }

    static final class Model {
        final Map<Integer, Track> tracks = new LinkedHashMap<>();
        final Map<Integer, Node> nodes = new LinkedHashMap<>();
        final List<Result> results = new ArrayList<>();
        private final Map<Integer, Polyline> chains = new HashMap<>();
        private Map<String, Integer> endCount;

        /** Centre line of the node (tracks ordered along their connections). */
        Polyline chain(Node n) {
            if (!chains.containsKey(n.id)) chains.put(n.id, buildChain(n));
            return chains.get(n.id);
        }

        /** Number of rails ending at this point (1 = open end). */
        int endpointCount(double px, double py) {
            if (endCount == null) {
                endCount = new HashMap<>();
                for (Track t : tracks.values()) {
                    for (double[] e : t.ends) endCount.merge(key(e[0], e[1]), 1, Integer::sum);
                }
            }
            return endCount.getOrDefault(key(px, py), 0);
        }

        private static String key(double px, double py) {
            return Math.round(px * 4) + "," + Math.round(py * 4);
        }

        private Polyline buildChain(Node n) {
            List<Track> ts = new ArrayList<>();
            for (int id : new LinkedHashSet<>(n.trackIds)) {
                Track t = tracks.get(id);
                if (t != null) ts.add(t);
            }
            if (ts.isEmpty()) return null;
            int k = ts.size();
            List<List<Integer>> adj = new ArrayList<>();
            for (int i = 0; i < k; i++) adj.add(new ArrayList<>());
            for (int i = 0; i < k; i++) {
                for (int j = i + 1; j < k; j++) {
                    if (touches(ts.get(i), ts.get(j))) {
                        adj.get(i).add(j);
                        adj.get(j).add(i);
                    }
                }
            }
            int start = 0;
            for (int i = 0; i < k; i++) {
                if (adj.get(i).size() <= 1) {
                    start = i;
                    break;
                }
            }
            List<Integer> order = new ArrayList<>();
            boolean[] seen = new boolean[k];
            int cur = start;
            while (true) {
                order.add(cur);
                seen[cur] = true;
                int nxt = -1;
                for (int j : adj.get(cur)) {
                    if (!seen[j]) {
                        nxt = j;
                        break;
                    }
                }
                if (nxt < 0) break;
                cur = nxt;
            }
            List<double[]> pts = new ArrayList<>();
            double[] prevExit = null;
            for (int oi = 0; oi < order.size(); oi++) {
                Track t = ts.get(order.get(oi));
                List<double[]> sp = flatten(t.shape);
                boolean rev;
                if (prevExit == null) {
                    if (order.size() > 1) {
                        Track nx = ts.get(order.get(1));
                        rev = minDistToEnds(sp.get(0), nx) < minDistToEnds(sp.get(sp.size() - 1), nx);
                    } else {
                        rev = false;
                    }
                } else {
                    rev = dist(sp.get(sp.size() - 1), prevExit) < dist(sp.get(0), prevExit);
                }
                if (rev) Collections.reverse(sp);
                for (double[] p : sp) {
                    if (!pts.isEmpty() && dist(pts.get(pts.size() - 1), p) < 1e-6) continue;
                    pts.add(p);
                }
                prevExit = sp.get(sp.size() - 1);
            }
            if (pts.size() < 2) return null;
            return new Polyline(pts);
        }

        private static boolean touches(Track a, Track b) {
            for (double[] ea : a.ends) for (double[] eb : b.ends) if (dist(ea, eb) < 0.35) return true;
            return false;
        }

        private static double minDistToEnds(double[] p, Track t) {
            double d = Double.MAX_VALUE;
            for (double[] e : t.ends) d = Math.min(d, dist(p, e));
            return d;
        }

        private static double dist(double[] a, double[] b) {
            return Math.hypot(a[0] - b[0], a[1] - b[1]);
        }

        private static List<double[]> flatten(Shape s) {
            List<double[]> pts = new ArrayList<>();
            PathIterator it = s.getPathIterator(null, 0.02);
            double[] c = new double[6];
            while (!it.isDone()) {
                int type = it.currentSegment(c);
                if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) pts.add(new double[]{c[0], c[1]});
                it.next();
            }
            return pts;
        }
    }

    // ------------------------------------------------------------------------------------------
    // Rail geometry (in tiles, y points down like in Factorio)
    // ------------------------------------------------------------------------------------------

    static final class Geometry {
        final Path2D shape;
        final Point2D mid;
        final double[][] ends;
        final double[][] line;  // centre line as a polyline, ground end first on a ramp

        private Geometry(Path2D shape, Point2D mid, double[][] ends, double[][] line) {
            this.shape = shape;
            this.mid = mid;
            this.ends = ends;
            this.line = line;
        }

        /**
         * The shape of one rail piece, taken from the analysis' own geometry so that what is drawn
         * and what is reasoned about cannot disagree. A rail the model does not know is drawn as a
         * bare point rather than guessed at.
         */
        static Geometry of(String name, int dir, double x, double y) {
            if (name == null || !RailGeometry.isKnownOrientation(name, dir)) {
                Path2D.Double dot = new Path2D.Double();
                dot.moveTo(x, y);
                dot.lineTo(x, y);
                double[][] point = {{x, y}, {x, y}};
                return new Geometry(dot, new Point2D.Double(x, y), point, point);
            }
            double[][] line = RailGeometry.centreLine(name, dir, x, y);
            Path2D.Double p = new Path2D.Double();
            p.moveTo(line[0][0], line[0][1]);
            for (int i = 1; i < line.length; i++) p.lineTo(line[i][0], line[i][1]);

            double[] centre = line[line.length / 2];
            double[][] ends = new double[2][];
            RailEnd[] railEnds = RailGeometry.endsOf(name, dir, x, y);
            for (int i = 0; i < 2; i++) ends[i] = new double[]{railEnds[i].x(), railEnds[i].y()};
            return new Geometry(p, new Point2D.Double(centre[0], centre[1]), ends, line);
        }
    }

    // ------------------------------------------------------------------------------------------
    // Deadlock scenario (trains)
    // ------------------------------------------------------------------------------------------

    static final class Train {
        int index;
        int nodeId;
        int targetNode = -1;     // node the train wants to enter
        boolean waitingOnly;     // train at the entry node (not part of the cycle)
        boolean frontAtEnd;
        double s0, s1;           // occupied range (whole train) on the node's centre line
        double front, sign;      // position of the front and travel direction (+1 / -1 along the centre line)
        double locoMid;          // middle of the locomotive
        final List<double[]> cars = new ArrayList<>();   // {from, to} as distance from the front; car 0 = locomotive
        Polyline chain;
        Color color;
    }

    static final class Scenario {
        final List<Train> trains = new ArrayList<>();
        /** waits[i] = {train, index of the train that blocks it (or -1)}. */
        final List<int[]> waits = new ArrayList<>();
        String cycleText = "";
        String explanation = "No deadlock cycle could be reconstructed.";
    }

    private static final Color[] TRAIN_COLORS = {
            new Color(0xe8543f), new Color(0xf2a93b), new Color(0xb76ce8), new Color(0x4fb0e8),
            new Color(0x5fd08a), new Color(0xe86ca0)};
    private static final Color WAIT_TRAIN = new Color(0x8393a8);

    static Scenario buildScenario(Model m, Result r) {
        Scenario sc = new Scenario();
        if (r == null || !r.deadlock) {
            sc.explanation = "No deadlock in this result.";
            return sc;
        }
        List<Node> cand = new ArrayList<>();
        for (int id : r.path) {
            Node n = m.nodes.get(id);
            if (n != null && !n.outputOnly() && n.sectionId >= 0 && !cand.contains(n)) cand.add(n);
        }
        List<Node> chosen = new ArrayList<>(), targets = new ArrayList<>();
        boolean found = false;
        for (int k = 2; k <= Math.min(8, cand.size()) && !found; k++) {
            for (Node start : cand) {
                chosen.clear();
                targets.clear();
                chosen.add(start);
                if (search(m, cand, k, start, chosen, targets)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) return sc;

        // train objects
        int idx = 1;
        for (int i = 0; i < chosen.size(); i++) {
            Train t = makeTrain(m, chosen.get(i), targets.get(i), idx++);
            t.color = TRAIN_COLORS[i % TRAIN_COLORS.length];
            sc.trains.add(t);
        }
        // who blocks whom
        for (int i = 0; i < sc.trains.size(); i++) {
            Train t = sc.trains.get(i);
            int blocker = -1;
            int tsec = m.nodes.get(t.targetNode).sectionId;
            for (int j = 0; j < sc.trains.size(); j++) {
                if (m.nodes.get(sc.trains.get(j).nodeId).sectionId == tsec) blocker = j;
            }
            sc.waits.add(new int[]{i, blocker});
        }
        // train at the entry node, if it is not part of the cycle itself
        Node first = m.nodes.get(r.path.isEmpty() ? -1 : r.path.get(0));
        if (first != null && !first.outputOnly() && !chosen.contains(first)) {
            Set<Integer> secs = new HashSet<>();
            for (Node c : chosen) secs.add(c.sectionId);
            for (int nid : first.nextFlat()) {
                Node nn = m.nodes.get(nid);
                if (nn != null && secs.contains(nn.sectionId) && first.sectionId != nn.sectionId) {
                    Train t = makeTrain(m, first, nn, idx++);
                    t.waitingOnly = true;
                    t.color = WAIT_TRAIN;
                    sc.trains.add(t);
                    int blocker = -1;
                    for (int j = 0; j < chosen.size(); j++) if (chosen.get(j).sectionId == nn.sectionId) blocker = j;
                    sc.waits.add(new int[]{sc.trains.size() - 1, blocker});
                    break;
                }
            }
        }

        // text
        StringBuilder cyc = new StringBuilder();
        StringBuilder ex = new StringBuilder();
        for (int i = 0; i < chosen.size(); i++) {
            cyc.append("Train ").append(i + 1).append(" (N").append(chosen.get(i).id).append(") → ");
        }
        cyc.append("Train 1");
        sc.cycleText = cyc.toString();
        for (int[] w : sc.waits) {
            Train t = sc.trains.get(w[0]);
            Node tn = m.nodes.get(t.targetNode);
            ex.append("Train ").append(t.index).append(t.waitingOnly ? " (entry)" : "").append(" stands in node ").append(t.nodeId)
                    .append(" and wants to enter node ").append(tn.id).append(" (section ").append(tn.sectionId).append("). ");
            if (w[1] >= 0) ex.append("That section is occupied by train ").append(sc.trains.get(w[1]).index).append(".");
            ex.append("\n\n");
        }
        ex.append("The trains are reconstructed from the deadlock path (smallest waiting cycle) and show how the sections block each other. This is not a simulation.");
        sc.explanation = ex.toString();
        return sc;
    }

    private static boolean search(Model m, List<Node> cand, int k, Node start, List<Node> chosen, List<Node> targets) {
        Node cur = chosen.get(chosen.size() - 1);
        for (int nid : cur.nextFlat()) {
            Node nn = m.nodes.get(nid);
            if (nn == null || nn.outputOnly() || nn.sectionId < 0 || nn.sectionId == cur.sectionId) continue;
            if (chosen.size() == k) {
                if (nn.sectionId == start.sectionId) {
                    targets.add(nn);
                    return true;
                }
                continue;
            }
            boolean used = false;
            for (Node c : chosen) if (c.sectionId == nn.sectionId) used = true;
            if (used) continue;
            List<Node> zs = new ArrayList<>();
            if (cand.contains(nn)) zs.add(nn);
            for (Node z : cand) if (z.sectionId == nn.sectionId && !zs.contains(z)) zs.add(z);
            for (Node z : zs) {
                chosen.add(z);
                targets.add(nn);
                if (search(m, cand, k, start, chosen, targets)) return true;
                chosen.remove(chosen.size() - 1);
                targets.remove(targets.size() - 1);
            }
        }
        return false;
    }

    private static Train makeTrain(Model m, Node node, Node target, int index) {
        Train t = new Train();
        t.index = index;
        t.nodeId = node.id;
        t.targetNode = target.id;
        t.chain = m.chain(node);
        if (t.chain == null) return t;
        double L = t.chain.length;
        // front end = the end that is closer to the target node
        double d0 = 1e9, d1 = 1e9;
        for (int tid : target.trackIds) {
            Track tt = m.tracks.get(tid);
            if (tt == null) continue;
            for (double[] e : tt.ends) {
                d0 = Math.min(d0, Math.hypot(t.chain.x[0] - e[0], t.chain.y[0] - e[1]));
                d1 = Math.min(d1, Math.hypot(t.chain.x[t.chain.x.length - 1] - e[0], t.chain.y[t.chain.y.length - 1] - e[1]));
            }
        }
        t.frontAtEnd = d1 <= d0;
        double gap = Math.min(0.5, L * 0.08);
        double usable = L - 2 * gap;
        double locoLen = Math.min(5.4, usable);
        double wagonLen = 5.4, coupling = 0.4;
        t.cars.add(new double[]{0, locoLen});
        double d = locoLen;
        for (int i = 0; i < 2; i++) {
            if (d + coupling + wagonLen > usable) break;
            t.cars.add(new double[]{d + coupling, d + coupling + wagonLen});
            d += coupling + wagonLen;
        }
        t.front = t.frontAtEnd ? L - gap : gap;
        t.sign = t.frontAtEnd ? -1 : 1;
        double a = t.front, b = t.front + t.sign * d;
        t.s0 = Math.min(a, b);
        t.s1 = Math.max(a, b);
        t.locoMid = t.front + t.sign * locoLen / 2;
        return t;
    }

    // ------------------------------------------------------------------------------------------
    // JSON -> model
    // ------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    static Model load(String json) {
        Object rootObj = MiniJson.parse(json);
        if (!(rootObj instanceof Map)) throw new IllegalArgumentException("JSON root is not an object");
        Map<String, Object> root = (Map<String, Object>) rootObj;
        Model m = new Model();

        Object graph = root.get("graph");
        if (graph instanceof Map) {
            Object sections = ((Map<String, Object>) graph).get("sections");
            if (sections instanceof List) {
                for (Object so : (List<Object>) sections) {
                    Map<String, Object> sec = (Map<String, Object>) so;
                    int sid = (int) num(sec.get("sectionId"), -1);
                    Object nl = sec.get("nodes");
                    if (nl instanceof List) {
                        for (Object no : (List<Object>) nl) {
                            Node n = readNode(m, (Map<String, Object>) no);
                            n.sectionId = sid;
                        }
                    }
                }
            }
        }

        Object res = root.get("result");
        if (res instanceof List) {
            for (Object ro : (List<Object>) res) {
                Map<String, Object> r = (Map<String, Object>) ro;
                Result result = new Result();
                result.deadlock = Boolean.TRUE.equals(r.get("isDeadlock"));
                Object path = r.get("deadlockPath");
                if (path instanceof List) {
                    for (Object no : (List<Object>) path) {
                        Node n = readNode(m, (Map<String, Object>) no);
                        result.path.add(n.id);
                    }
                }
                m.results.add(result);
            }
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private static Node readNode(Model m, Map<String, Object> o) {
        int id = (int) num(o.get("nodeId"), -1);
        Node existing = m.nodes.get(id);
        if (existing != null) return existing;
        Node n = new Node(id, num(o.get("length"), 0),
                Boolean.TRUE.equals(o.get("isInput")), Boolean.TRUE.equals(o.get("isOutput")));
        Object tl = o.get("tracks");
        if (tl instanceof List) {
            for (Object to : (List<Object>) tl) {
                Map<String, Object> t = (Map<String, Object>) to;
                int tid = (int) num(t.get("trackId"), -1);
                Map<String, Object> pos = (Map<String, Object>) t.get("position");
                m.tracks.computeIfAbsent(tid, k -> new Track(tid, (String) t.get("name"),
                        (int) num(t.get("direction"), 0), num(pos.get("x"), 0), num(pos.get("y"), 0)));
                n.trackIds.add(tid);
            }
        }
        readIdLists(o.get("nextNodesIds"), n.next);
        readIdLists(o.get("protectedFromIds"), n.protectedFrom);
        m.nodes.put(id, n);
        return n;
    }

    @SuppressWarnings("unchecked")
    private static void readIdLists(Object o, List<List<Integer>> out) {
        if (!(o instanceof List)) return;
        for (Object l : (List<Object>) o) {
            List<Integer> ids = new ArrayList<>();
            if (l instanceof List) for (Object v : (List<Object>) l) ids.add((int) num(v, -1));
            out.add(ids);
        }
    }

    private static double num(Object o, double def) {
        return o instanceof Number ? ((Number) o).doubleValue() : def;
    }

    // ------------------------------------------------------------------------------------------
    // Drawing surface
    // ------------------------------------------------------------------------------------------

    static final class ViewPanel extends JPanel {
        private static final Color BG = new Color(0x12161b);
        private static final Color GLOW_BASE = new Color(0x181d23);
        // Ballast beds: ground keeps the neutral tone, the upper level gets a
        // cooler one, and a ramp sits between the two.
        private static final Color ELEV_BED = new Color(0x27405e);
        private static final Color SHADOW = new Color(0, 0, 0, 150);
        private static final Color TIES = new Color(0x4a3f35), TIES_DIM = new Color(0x2e2b2a);
        private static final Color RAIL = new Color(0xa4aebb), RAIL_DIM = new Color(0x4b5460);
        private static final Color DL_GLOW = new Color(255, 82, 72, 62), DL_RAIL = new Color(0xf0c2bd), DL_TIES = new Color(0x6e3a36);
        private static final Color NEXT_GLOW = new Color(61, 220, 132, 70), NEXT_RAIL = new Color(0xa6f3c6);
        private static final Color SEL_GLOW = new Color(76, 201, 240, 85), SEL_RAIL = new Color(0xb9ecfb);
        private static final Color IN_COL = new Color(0x2fbf71), OUT_COL = new Color(0xb084ff);
        private static final Color WAIT_ARROW = new Color(0xffd166);

        final Model model;
        int resultIndex = 0;
        int selectedNode = -1;
        boolean showTrains = true, showWaitArrows = true, showWeb = false, showAllLabels = false,
                showTrackIds = false, showOthers = true;
        double scale = 12, ox = 0, oy = 0;
        Runnable onSelectionChanged = () -> { };
        Scenario scenario = new Scenario();
        private String glowKey;
        private Shape dlGlow, nextGlow, selGlow;

        private Point dragStart;
        private double dragOx, dragOy;
        private final Map<Integer, Rectangle2D> badgeRects = new HashMap<>();

        ViewPanel(Model model) {
            this.model = model;
            setBackground(BG);
            setPreferredSize(new Dimension(900, 700));
            setResult(0);
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragStart = e.getPoint();
                    dragOx = ox;
                    dragOy = oy;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStart == null) return;
                    ox = dragOx + e.getX() - dragStart.x;
                    oy = dragOy + e.getY() - dragStart.y;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragStart != null && dragStart.distance(e.getPoint()) < 4) pick(e.getPoint());
                    dragStart = null;
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double f = Math.pow(1.15, -e.getPreciseWheelRotation());
                    double ns = Math.max(1.5, Math.min(80, scale * f));
                    f = ns / scale;
                    ox = e.getX() - (e.getX() - ox) * f;
                    oy = e.getY() - (e.getY() - oy) * f;
                    scale = ns;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) fit(false);
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
            addMouseWheelListener(ma);
        }

        void setResult(int idx) {
            resultIndex = Math.max(0, Math.min(idx, Math.max(0, model.results.size() - 1)));
            scenario = buildScenario(model, current());
        }

        Result current() {
            return resultIndex >= 0 && resultIndex < model.results.size() ? model.results.get(resultIndex) : null;
        }

        Set<Integer> deadlockNodes() {
            Result r = current();
            return r == null || !r.deadlock ? Collections.emptySet() : new LinkedHashSet<>(r.path);
        }

        Set<Integer> tracksOf(Collection<Integer> nodeIds) {
            Set<Integer> s = new LinkedHashSet<>();
            for (int id : nodeIds) {
                Node n = model.nodes.get(id);
                if (n != null) s.addAll(n.trackIds);
            }
            return s;
        }

        void fit(boolean onlyDeadlock) {
            Collection<Integer> ids = onlyDeadlock && !deadlockNodes().isEmpty()
                    ? tracksOf(deadlockNodes()) : model.tracks.keySet();
            Rectangle2D b = null;
            for (int id : ids) {
                Track t = model.tracks.get(id);
                Rectangle2D r = t.shape.getBounds2D();
                if (b == null) b = r; else b = b.createUnion(r);
            }
            if (b == null) return;
            int w = Math.max(getWidth(), 100), h = Math.max(getHeight(), 100);
            int mx = 110, top = 130, bottom = 175;
            double bw = Math.max(b.getWidth(), 4), bh = Math.max(b.getHeight(), 4);
            scale = Math.max(2, Math.min(40, Math.min((w - 2 * mx) / bw, (h - top - bottom) / bh)));
            ox = w / 2.0 - b.getCenterX() * scale;
            oy = top + (h - top - bottom) / 2.0 - b.getCenterY() * scale;
            repaint();
        }

        private void pick(Point p) {
            for (Map.Entry<Integer, Rectangle2D> e : badgeRects.entrySet()) {
                if (e.getValue().contains(p)) {
                    select(e.getKey());
                    return;
                }
            }
            double tx = (p.x - ox) / scale, ty = (p.y - oy) / scale;
            if (showTrains) {
                for (Train t : scenario.trains) {
                    if (t.chain != null && distToShape(t.chain.sub(t.s0, t.s1), tx, ty) < 1.1) {
                        select(t.nodeId);
                        return;
                    }
                }
            }
            int best = -1;
            double bestD = 1.2;
            for (Track t : model.tracks.values()) {
                double d = distToShape(t.shape, tx, ty);
                if (d < bestD) {
                    bestD = d;
                    best = t.id;
                }
            }
            if (best < 0) {
                select(-1);
                return;
            }
            Set<Integer> dl = deadlockNodes();
            int chosen = -1;
            for (Node n : model.nodes.values()) {
                if (!n.trackIds.contains(best)) continue;
                if (dl.contains(n.id)) {
                    chosen = n.id;
                    break;
                }
                if (chosen < 0) chosen = n.id;
            }
            select(chosen);
        }

        void select(int nodeId) {
            selectedNode = nodeId == selectedNode ? -1 : nodeId;
            onSelectionChanged.run();
            repaint();
        }

        private static double distToShape(Shape s, double x, double y) {
            PathIterator it = s.getPathIterator(null, 0.05);
            double[] c = new double[6];
            double px = 0, py = 0, best = Double.MAX_VALUE;
            while (!it.isDone()) {
                int type = it.currentSegment(c);
                if (type == PathIterator.SEG_MOVETO) {
                    px = c[0];
                    py = c[1];
                } else if (type == PathIterator.SEG_LINETO) {
                    best = Math.min(best, Line2D.ptSegDist(px, py, c[0], c[1], x, y));
                    px = c[0];
                    py = c[1];
                }
                it.next();
            }
            return best;
        }

        // ------------------------------------------------------------------ painting

        @Override
        protected void paintComponent(Graphics gr) {
            super.paintComponent(gr);
            Graphics2D g = (Graphics2D) gr.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setColor(BG);
            g.fillRect(0, 0, getWidth(), getHeight());
            drawGrid(g);

            Set<Integer> dlNodes = deadlockNodes();
            Set<Integer> dlTracks = tracksOf(dlNodes);
            Node sel = model.nodes.get(selectedNode);
            Set<Integer> nextIds = sel == null ? Collections.emptySet() : sel.nextFlat();
            Set<Integer> selTracks = sel == null ? Collections.emptySet() : new HashSet<>(sel.trackIds);
            Set<Integer> nextTracks = tracksOf(nextIds);

            AffineTransform world = new AffineTransform();
            world.translate(ox, oy);
            world.scale(scale, scale);
            AffineTransform screen = g.getTransform();
            g.transform(world);

            // 1) ballast bed + glow of the highlights. Lowest level first, so
            // that whatever is overhead is drawn over what runs beneath it.
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(GLOW_BASE);
            for (Track t : model.tracks.values()) g.draw(t.shape);
            String gk = resultIndex + ":" + selectedNode;
            if (!gk.equals(glowKey)) {
                glowKey = gk;
                dlGlow = glow(dlTracks);
                nextGlow = glow(nextTracks);
                selGlow = glow(selTracks);
            }
            g.setColor(DL_GLOW);
            g.fill(dlGlow);
            g.setColor(NEXT_GLOW);
            g.fill(nextGlow);
            g.setColor(SEL_GLOW);
            g.fill(selGlow);

            // The deck of anything overhead goes on top of the highlight glow,
            // so which level a piece is on stays readable even when it is part
            // of the deadlock. The glow is wider than the deck, so its halo
            // still shows around it.
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Track t : model.tracks.values()) {
                if (t.level != RAMP) continue;
                // A ramp's deck fades from the ground tone to the elevated one
                // along its length, which shows at a glance which way it climbs.
                List<double[]> pts = Track.resample(t.line, 16);
                for (int i = 0; i + 1 < pts.size(); i++) {
                    g.setColor(blend(GLOW_BASE, ELEV_BED, (i + 0.5) / (pts.size() - 1)));
                    g.draw(new Line2D.Double(pts.get(i)[0], pts.get(i)[1],
                            pts.get(i + 1)[0], pts.get(i + 1)[1]));
                }
            }
            g.setColor(ELEV_BED);
            for (Track t : model.tracks.values()) if (t.level == ELEVATED) g.draw(t.shape);

            // 2) ties and rails, ground level first. The shadows of everything
            // overhead go on once the ground is finished, so they fall across
            // it rather than being painted over by it.
            boolean detailed = scale >= 7;
            for (Track t : model.tracks.values()) {
                if (t.level == GROUND) drawTrack(g, t, detailed, dlTracks, nextTracks, selTracks);
            }
            g.setColor(SHADOW);
            for (Track t : model.tracks.values()) {
                Shape shadow = t.shadow();
                if (shadow != null) g.fill(shadow);
            }
            for (int level : new int[]{RAMP, ELEVATED}) {
                for (Track t : model.tracks.values()) {
                    if (t.level == level) drawTrack(g, t, detailed, dlTracks, nextTracks, selTracks);
                }
            }

            // 3) signals and trains (world coordinates)
            if (showTrains) {
                for (Train t : scenario.trains) if (t.chain != null) drawSignal(g, t);
                for (Train t : scenario.trains) if (t.chain != null) drawTrain(g, t, t.nodeId == selectedNode);
            }

            g.setTransform(screen);

            // inputs / outputs
            drawPorts(g, world);

            // Track-IDs
            if (showTrackIds && scale >= 9) {
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(8, (int) Math.min(11, scale * 0.7))));
                g.setColor(new Color(255, 255, 255, 150));
                for (Track t : model.tracks.values()) {
                    Point2D p = world.transform(t.mid, null);
                    String s = "t" + t.id;
                    g.drawString(s, (float) (p.getX() - g.getFontMetrics().stringWidth(s) / 2.0), (float) (p.getY() - 4));
                }
            }

            // prepare labels
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            badgeRects.clear();
            Map<Integer, Point2D> anchor = new HashMap<>();
            Map<Integer, String> text = new LinkedHashMap<>();
            Map<Integer, Color> fill = new HashMap<>();
            if (showTrains) {
                for (Train t : scenario.trains) {
                    if (t.chain == null) continue;
                    double[] c = t.chain.at(t.locoMid);
                    double nx = -c[3], ny = c[2];
                    if (ny > 0 || (ny == 0 && nx > 0)) { nx = -nx; ny = -ny; }   // nach oben
                    Point2D p = world.transform(new Point2D.Double(c[0] + nx * 2.1, c[1] + ny * 2.1), null);
                    anchor.put(t.nodeId, p);
                    text.put(t.nodeId, "Train " + t.index + "  ·  N" + t.nodeId);
                    fill.put(t.nodeId, t.color);
                }
            }
            LinkedHashSet<Integer> extra = new LinkedHashSet<>();
            if (sel != null) { extra.add(sel.id); extra.addAll(nextIds); }
            if (showAllLabels) extra.addAll(model.nodes.keySet());
            if (!showTrains) extra.addAll(dlNodes);
            for (int id : extra) {
                if (text.containsKey(id)) continue;
                Node n = model.nodes.get(id);
                Polyline ch = n == null ? null : model.chain(n);
                if (ch == null) continue;
                double[] c = ch.at(ch.length / 2);
                anchor.put(id, world.transform(new Point2D.Double(c[0], c[1]), null));
                text.put(id, "N" + id);
                fill.put(id, id == selectedNode ? new Color(0x4cc9f0) : nextIds.contains(id) ? new Color(0x3ddc84)
                        : dlNodes.contains(id) ? new Color(0xff6b63) : new Color(0x56627a));
            }
            for (Map.Entry<Integer, String> e : text.entrySet()) {
                double w = fm.stringWidth(e.getValue()) + 14, h = fm.getHeight() + 3;
                Point2D p = anchor.get(e.getKey());
                Rectangle2D r = new Rectangle2D.Double(p.getX() - w / 2, p.getY() - h / 2, w, h);
                for (int tries = 0; tries < 12 && collides(r); tries++) r.setRect(r.getX(), r.getY() - h - 2, w, h);
                badgeRects.put(e.getKey(), r);
            }

            // optional: all dependencies within the path (debug)
            if (showWeb) {
                g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int id : dlNodes) {
                    Node n = model.nodes.get(id);
                    Polyline ch = model.chain(n);
                    if (ch == null) continue;
                    double[] a = ch.at(ch.length / 2);
                    Point2D pa = world.transform(new Point2D.Double(a[0], a[1]), null);
                    for (int nx : n.nextFlat()) {
                        Node nn = model.nodes.get(nx);
                        Polyline cn = nn == null ? null : model.chain(nn);
                        if (cn == null || !dlNodes.contains(nx)) continue;
                        double[] b = cn.at(cn.length / 2);
                        arrow(g, pa, world.transform(new Point2D.Double(b[0], b[1]), null), new Color(255, 190, 70, 120), 3);
                    }
                }
            }

            // selection: arrows to the successors
            if (sel != null && anchor.containsKey(sel.id)) {
                for (int nx : nextIds) {
                    if (anchor.containsKey(nx)) {
                        Rectangle2D a = badgeRects.get(sel.id), b = badgeRects.get(nx);
                        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        arrow(g, new Point2D.Double(a.getCenterX(), a.getCenterY()),
                                new Point2D.Double(b.getCenterX(), b.getCenterY()), new Color(61, 220, 132, 210), 20);
                    }
                }
            }

            // "waits for" arrows between the trains
            if (showTrains && showWaitArrows) {
                for (int[] w : scenario.waits) {
                    if (w[1] < 0) continue;
                    Train a = scenario.trains.get(w[0]), b = scenario.trains.get(w[1]);
                    if (a.chain == null || b.chain == null) continue;
                    double[] ca = a.chain.at(a.locoMid), cb = b.chain.at(b.locoMid);
                    Point2D pa = world.transform(new Point2D.Double(ca[0], ca[1]), null);
                    Point2D pb = world.transform(new Point2D.Double(cb[0], cb[1]), null);
                    g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{9f, 6f}, 0f));
                    waitArrow(g, pa, pb, a.waitingOnly ? new Color(0xffd166, false).darker() : WAIT_ARROW, a.index, b.index);
                }
            }

            // draw labels
            for (Map.Entry<Integer, Rectangle2D> e : badgeRects.entrySet()) {
                Rectangle2D r = e.getValue();
                boolean isSel = e.getKey() == selectedNode;
                RoundRectangle2D rr = new RoundRectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight(), 12, 12);
                g.setColor(fill.get(e.getKey()));
                g.fill(rr);
                g.setColor(isSel ? Color.WHITE : new Color(0, 0, 0, 140));
                g.setStroke(new BasicStroke(isSel ? 2.2f : 1f));
                g.draw(rr);
                g.setColor(new Color(0x10151a));
                g.drawString(text.get(e.getKey()), (float) (r.getX() + 7), (float) (r.getY() + fm.getAscent() + 1.5));
            }

            drawOverlay(g);
            g.dispose();
        }

        private void drawTrack(Graphics2D g, Track t, boolean detailed,
                Set<Integer> dlTracks, Set<Integer> nextTracks, Set<Integer> selTracks) {
            boolean inDl = dlTracks.contains(t.id);
            boolean dim = !showOthers && !inDl && !nextTracks.contains(t.id) && !selTracks.contains(t.id);
            Color rail = selTracks.contains(t.id) ? SEL_RAIL : nextTracks.contains(t.id) ? NEXT_RAIL
                    : inDl ? DL_RAIL : dim ? RAIL_DIM : RAIL;
            Color ties = inDl ? DL_TIES : dim ? TIES_DIM : TIES;
            if (detailed) {
                g.setColor(ties);
                g.fill(t.ties());
                g.setColor(rail);
                g.fill(t.rails());
            } else {
                g.setColor(rail);
                g.setStroke(new BasicStroke(0.55f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                g.draw(t.shape);
            }
        }

        private Shape glow(Collection<Integer> ids) {
            Area a = new Area();
            BasicStroke st = new BasicStroke(2.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            for (int id : ids) a.add(new Area(st.createStrokedShape(model.tracks.get(id).shape)));
            return a;
        }

        private void drawGrid(Graphics2D g) {
            if (scale < 6) return;
            double step = 4;
            double x0 = Math.floor(-ox / scale / step) * step, y0 = Math.floor(-oy / scale / step) * step;
            g.setColor(new Color(255, 255, 255, 22));
            for (double wx = x0; wx * scale + ox < getWidth(); wx += step) {
                for (double wy = y0; wy * scale + oy < getHeight(); wy += step) {
                    g.fillRect((int) (wx * scale + ox) - 1, (int) (wy * scale + oy) - 1, 2, 2);
                }
            }
        }

        private void drawSignal(Graphics2D g, Train t) {
            double[] f = t.chain.at(t.front);
            double dx = t.frontAtEnd ? f[2] : -f[2], dy = t.frontAtEnd ? f[3] : -f[3];
            double nx = -dy, ny = dx;
            double fx = f[0] + dx * 0.25, fy = f[1] + dy * 0.25;
            double px = fx + nx * 1.75, py = fy + ny * 1.75;
            // Mast
            g.setStroke(new BasicStroke(0.16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(0x2b323b));
            g.draw(new Line2D.Double(fx + nx * 0.95, fy + ny * 0.95, px, py));
            // glow
            g.setPaint(new RadialGradientPaint(new Point2D.Double(px, py), 1.7f, new float[]{0f, 1f},
                    new Color[]{new Color(255, 60, 50, 150), new Color(255, 60, 50, 0)}));
            g.fill(new Ellipse2D.Double(px - 1.7, py - 1.7, 3.4, 3.4));
            // housing + lamp
            g.setColor(new Color(0x1b2027));
            g.fill(new Ellipse2D.Double(px - 0.55, py - 0.55, 1.1, 1.1));
            g.setColor(new Color(0x48525f));
            g.setStroke(new BasicStroke(0.09f));
            g.draw(new Ellipse2D.Double(px - 0.55, py - 0.55, 1.1, 1.1));
            g.setColor(new Color(0xff4a3d));
            g.fill(new Ellipse2D.Double(px - 0.34, py - 0.34, 0.68, 0.68));
            g.setColor(new Color(255, 255, 255, 190));
            g.fill(new Ellipse2D.Double(px - 0.18, py - 0.22, 0.2, 0.2));
        }

        private static Color blend(Color a, Color b, double t) {
            return new Color((int) (a.getRed() * (1 - t) + b.getRed() * t), (int) (a.getGreen() * (1 - t) + b.getGreen() * t),
                    (int) (a.getBlue() * (1 - t) + b.getBlue() * t));
        }

        /** Centre line of a car (capsule: ends are rounded, hence inset by half the width). */
        private Path2D carPath(Train t, double[] c, double inset) {
            double sa = t.front + t.sign * c[0], sb = t.front + t.sign * c[1];
            double lo = Math.min(sa, sb) + inset, hi = Math.max(sa, sb) - inset;
            return hi - lo < 0.2 ? null : t.chain.sub(lo, hi);
        }

        private Path2D span(Train t, double d0, double d1) {
            double sa = t.front + t.sign * d0, sb = t.front + t.sign * d1;
            return t.chain.sub(Math.min(sa, sb), Math.max(sa, sb));
        }

        private void drawTrain(Graphics2D g, Train t, boolean selected) {
            final double inset = 0.78;
            Color loco = t.color, wagon = new Color(0x6c7a8e);
            Color outline = selected ? Color.WHITE : new Color(0x0b0e12);
            AffineTransform keep = g.getTransform();

            // shadow
            for (double[] c : t.cars) {
                Path2D p = carPath(t, c, inset);
                if (p == null) continue;
                g.translate(0.10, 0.18);
                g.setStroke(new BasicStroke(1.95f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(0, 0, 0, 105));
                g.draw(p);
                g.setTransform(keep);
            }
            // couplings
            for (int i = 1; i < t.cars.size(); i++) {
                g.setStroke(new BasicStroke(0.26f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(0x0b0e12));
                g.draw(span(t, t.cars.get(i - 1)[1] - 0.05, t.cars.get(i)[0] + 0.05));
            }
            // cars and locomotive
            for (int i = t.cars.size() - 1; i >= 0; i--) {
                double[] c = t.cars.get(i);
                Path2D p = carPath(t, c, inset);
                if (p == null) continue;
                boolean isLoco = i == 0;
                g.setStroke(new BasicStroke(1.95f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(outline);
                g.draw(p);
                g.setStroke(new BasicStroke(1.55f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(isLoco ? loco : wagon);
                g.draw(p);
                if (isLoco) {
                    double len = c[1] - c[0];
                    // cab (rear, darker)
                    g.setColor(loco.darker());
                    g.draw(span(t, c[1] - inset - 1.25, c[1] - inset));
                    // windscreen
                    g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                    g.setColor(new Color(0xbfe6fa));
                    g.draw(span(t, Math.max(c[0] + 1.0, c[1] - 2.35), Math.max(c[0] + 1.3, c[1] - 1.9)));
                    // hood: light roof line
                    g.setStroke(new BasicStroke(0.30f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(new Color(255, 255, 255, 85));
                    Path2D roof = carPath(t, c, inset + 0.35);
                    if (roof != null && len > 2) g.draw(roof);
                } else {
                    // cargo: darker trough + stripe in the train colour
                    g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                    g.setColor(new Color(0x4f5b6c));
                    g.draw(span(t, c[0] + 0.9, c[1] - 0.9));
                    g.setStroke(new BasicStroke(0.34f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                    g.setColor(loco);
                    g.draw(span(t, c[0] + 0.9, c[1] - 0.9));
                }
            }
            // headlight
            double[] f = t.chain.at(t.front + t.sign * 0.55);
            g.setPaint(new RadialGradientPaint(new Point2D.Double(f[0], f[1]), 0.9f, new float[]{0f, 1f},
                    new Color[]{new Color(255, 244, 184, 170), new Color(255, 244, 184, 0)}));
            g.fill(new Ellipse2D.Double(f[0] - 0.9, f[1] - 0.9, 1.8, 1.8));
            g.setColor(new Color(0xfff4b8));
            g.fill(new Ellipse2D.Double(f[0] - 0.24, f[1] - 0.24, 0.48, 0.48));
        }

        private boolean collides(Rectangle2D r) {
            for (Rectangle2D o : badgeRects.values()) if (o.intersects(r)) return true;
            return false;
        }

        private void drawPorts(Graphics2D g, AffineTransform world) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            for (Node n : model.nodes.values()) {
                if (!n.input && !n.output) continue;
                Polyline ch = model.chain(n);
                if (ch == null) continue;
                double[] s = ch.at(0), e = ch.at(ch.length);
                double[][] ends = {{s[0], s[1], -s[2], -s[3]}, {e[0], e[1], e[2], e[3]}};
                for (double[] p : ends) {
                    if (model.endpointCount(p[0], p[1]) != 1) continue;   // nur offene Enden
                    String label = n.input && n.output ? "IN/OUT" : n.input ? "IN" : "OUT";
                    Color c = n.input ? IN_COL : OUT_COL;
                    Point2D tip = world.transform(new Point2D.Double(p[0] + p[2] * 0.6, p[1] + p[3] * 0.6), null);
                    Point2D lab = world.transform(new Point2D.Double(p[0] + p[2] * 3.4, p[1] + p[3] * 3.4), null);
                    double dir = n.input ? -1 : 1;
                    double vx = p[2] * dir, vy = p[3] * dir;
                    double sx = tip.getX() + p[2] * 8, sy = tip.getY() + p[3] * 8;
                    Path2D tri = new Path2D.Double();
                    double L = 9, W = 6;
                    tri.moveTo(sx + vx * L, sy + vy * L);
                    tri.lineTo(sx - vx * L * 0.2 - vy * W, sy - vy * L * 0.2 + vx * W);
                    tri.lineTo(sx - vx * L * 0.2 + vy * W, sy - vy * L * 0.2 - vx * W);
                    tri.closePath();
                    g.setColor(c);
                    g.fill(tri);
                    double w = fm.stringWidth(label) + 12, h = fm.getHeight() + 2;
                    RoundRectangle2D rr = new RoundRectangle2D.Double(lab.getX() - w / 2, lab.getY() - h / 2, w, h, 10, 10);
                    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
                    g.fill(rr);
                    g.setColor(c);
                    g.setStroke(new BasicStroke(1.2f));
                    g.draw(rr);
                    g.drawString(label, (float) (lab.getX() - w / 2 + 6), (float) (lab.getY() - h / 2 + fm.getAscent() + 1));
                }
            }
        }

        private void arrow(Graphics2D g, Point2D a, Point2D b, Color c, double trim) {
            if (a == null || b == null || a.distance(b) < 2 * trim + 6) return;
            double dx = b.getX() - a.getX(), dy = b.getY() - a.getY(), d = Math.hypot(dx, dy);
            double ux = dx / d, uy = dy / d, nx = -uy, ny = ux;
            double bend = Math.min(40, d * 0.12);
            Point2D s = new Point2D.Double(a.getX() + ux * trim, a.getY() + uy * trim);
            Point2D e = new Point2D.Double(b.getX() - ux * trim, b.getY() - uy * trim);
            Point2D ctl = new Point2D.Double((s.getX() + e.getX()) / 2 + nx * bend, (s.getY() + e.getY()) / 2 + ny * bend);
            g.setColor(c);
            g.draw(new QuadCurve2D.Double(s.getX(), s.getY(), ctl.getX(), ctl.getY(), e.getX(), e.getY()));
            head(g, e, Math.atan2(e.getY() - ctl.getY(), e.getX() - ctl.getX()), 9);
        }

        /** Dashed, curved "waits for" arrow. The arc bends sideways so that the arrow and its return arrow stay apart. */
        private void waitArrow(Graphics2D g, Point2D a, Point2D b, Color c, int from, int to) {
            double dx = b.getX() - a.getX(), dy = b.getY() - a.getY(), d = Math.hypot(dx, dy);
            if (d < 70) return;
            double ux = dx / d, uy = dy / d, nx = -uy, ny = ux;
            double bend = Math.min(70, d * 0.20);
            Point2D s = new Point2D.Double(a.getX() + ux * 34, a.getY() + uy * 34);
            Point2D e = new Point2D.Double(b.getX() - ux * 34, b.getY() - uy * 34);
            Point2D ctl = new Point2D.Double((s.getX() + e.getX()) / 2 + nx * bend, (s.getY() + e.getY()) / 2 + ny * bend);
            QuadCurve2D q = new QuadCurve2D.Double(s.getX(), s.getY(), ctl.getX(), ctl.getY(), e.getX(), e.getY());
            g.setColor(new Color(0, 0, 0, 110));
            Stroke keep = g.getStroke();
            g.setStroke(new BasicStroke(5.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(q);
            g.setStroke(keep);
            g.setColor(c);
            g.draw(q);
            g.setStroke(new BasicStroke(1f));
            head(g, e, Math.atan2(e.getY() - ctl.getY(), e.getX() - ctl.getX()), 13);
            // label at the apex
            double mx = 0.25 * s.getX() + 0.5 * ctl.getX() + 0.25 * e.getX(), my = 0.25 * s.getY() + 0.5 * ctl.getY() + 0.25 * e.getY();
            String t = "Train " + from + " waits for train " + to;
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            double w = fm.stringWidth(t) + 12, h = fm.getHeight() + 2;
            RoundRectangle2D rr = new RoundRectangle2D.Double(mx - w / 2, my - h / 2, w, h, 10, 10);
            g.setColor(new Color(0x12161b));
            g.fill(rr);
            g.setColor(c);
            g.draw(rr);
            g.drawString(t, (float) (mx - w / 2 + 6), (float) (my - h / 2 + fm.getAscent() + 1));
        }

        private void head(Graphics2D g, Point2D e, double ang, double size) {
            Path2D head = new Path2D.Double();
            head.moveTo(e.getX(), e.getY());
            head.lineTo(e.getX() - size * Math.cos(ang - 0.42), e.getY() - size * Math.sin(ang - 0.42));
            head.lineTo(e.getX() - size * Math.cos(ang + 0.42), e.getY() - size * Math.sin(ang + 0.42));
            head.closePath();
            g.fill(head);
        }

        private void drawOverlay(Graphics2D g) {
            Result r = current();
            String title;
            if (r == null) title = "No analysis result";
            else if (r.deadlock) title = "Deadlock " + (resultIndex + 1) + " of " + model.results.size();
            else title = "Result " + (resultIndex + 1) + ": no deadlock";
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            g.setColor(r != null && r.deadlock ? new Color(0xff6b63) : new Color(0x3ddc84));
            g.drawString(title, 18, 30);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(new Color(0xaab4c2));
            String sub = r != null && r.deadlock
                    ? (scenario.trains.isEmpty() ? r.path.size() + " nodes in path" : "Waiting cycle: " + scenario.cycleText + "   ·   " + r.path.size() + " nodes in path")
                    : "";
            g.drawString(sub, 18, 49);

            // legend
            Object[][] items = {
                    {"train", "Train (locomotive) stopped at red signal"},
                    {"dl", "Tracks in the deadlock path"},
                    {"sel", "Selected node (click)"},
                    {"next", "Successors of the selection"},
                    {"in", "Network input / output"},
                    {"elev", "Elevated track, with its shadow. A ramp fades in as it climbs"},
            };
            int x = 18, y = getHeight() - 18 - items.length * 21 + 8;
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(new Color(0, 0, 0, 130));
            g.fillRoundRect(x - 10, y - 20, 262, items.length * 21 + 14, 12, 12);
            for (int i = 0; i < items.length; i++) {
                int yy = y + i * 21;
                String k = (String) items[i][0];
                switch (k) {
                    case "train":
                        g.setColor(new Color(0xe8543f));
                        g.fillRoundRect(x, yy - 9, 22, 10, 5, 5);
                        g.setColor(new Color(0xff4638));
                        g.fillOval(x + 26, yy - 8, 8, 8);
                        break;
                    case "elev":
                        g.setColor(SHADOW);
                        g.fillRoundRect(x + 5, yy - 5, 22, 10, 4, 4);
                        for (int px = 0; px < 22; px++) {
                            g.setColor(blend(GLOW_BASE, ELEV_BED, px / 21.0));
                            g.fillRect(x + px, yy - 9, 1, 10);
                        }
                        g.setColor(RAIL);
                        g.fillRect(x + 2, yy - 7, 18, 1);
                        g.fillRect(x + 2, yy - 3, 18, 1);
                        break;
                    case "dl": g.setColor(new Color(255, 80, 70, 150)); g.fillRoundRect(x, yy - 9, 22, 10, 4, 4); break;
                    case "sel": g.setColor(new Color(76, 201, 240, 190)); g.fillRoundRect(x, yy - 9, 22, 10, 4, 4); break;
                    case "next": g.setColor(new Color(61, 220, 132, 190)); g.fillRoundRect(x, yy - 9, 22, 10, 4, 4); break;
                    default:
                        g.setColor(IN_COL);
                        g.fillRoundRect(x, yy - 9, 10, 10, 4, 4);
                        g.setColor(OUT_COL);
                        g.fillRoundRect(x + 12, yy - 9, 10, 10, 4, 4);
                }
                g.setColor(new Color(0xd6dde6));
                g.drawString((String) items[i][1], x + 42, yy);
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // Window
    // ------------------------------------------------------------------------------------------

    /** Shows the window for the JSON output of the analysis (may be called from any thread). */
    public static void show(String json) {
        final Model model = load(json);
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("DeadlockViewer: no graphical environment available (headless). "
                    + "Use --png <file> to export an image.");
            return;
        }
        SwingUtilities.invokeLater(() -> createFrame(model).setVisible(true));
    }

    public static void show(File jsonFile) throws IOException {
        show(new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8));
    }

    /** Renders a view without a window into a PNG file. */
    public static void exportPng(String json, int resultIndex, int selectedNode, File out, int width, int height) throws IOException {
        Model model = load(json);
        ViewPanel vp = new ViewPanel(model);
        vp.setResult(resultIndex);
        vp.selectedNode = selectedNode;
        vp.setSize(width, height);
        vp.fit(false);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        vp.paint(g);
        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // --- UI helpers ---------------------------------------------------------------------------

    private static final Color SIDE_BG = new Color(0xf1f4f8), CARD_BORDER = new Color(0xdbe2ea),
            MUTED = new Color(0x6b7787), TEXT = new Color(0x1f2733), ACCENT = new Color(0x2f6fed);

    /** Flat, rounded button. */
    static final class FlatButton extends JButton {
        private final boolean primary;

        FlatButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
            setForeground(primary ? Color.WHITE : TEXT);
            setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics gr) {
            Graphics2D g = (Graphics2D) gr.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = primary ? ACCENT : Color.WHITE;
            if (getModel().isRollover()) base = primary ? base.brighter() : new Color(0xe9eef5);
            if (getModel().isArmed()) base = base.darker();
            g.setColor(base);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            if (!primary) {
                g.setColor(CARD_BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
            g.dispose();
            super.paintComponent(gr);
        }
    }

    private static JPanel card(String title, JComponent body) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(10, 12, 12, 12)));
        JLabel l = new JLabel(title.toUpperCase(Locale.ROOT));
        l.setFont(l.getFont().deriveFont(Font.BOLD, 10.5f));
        l.setForeground(MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JCheckBox check(String text, boolean sel) {
        JCheckBox c = new JCheckBox(text, sel);
        c.setOpaque(false);
        c.setForeground(TEXT);
        c.setFocusPainted(false);
        return c;
    }

    private static JTextArea textArea(int rows, boolean mono) {
        JTextArea a = new JTextArea(rows, 24);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setOpaque(false);
        a.setForeground(TEXT);
        a.setFont(mono ? new Font(Font.MONOSPACED, Font.PLAIN, 11) : a.getFont().deriveFont(12.5f));
        a.setBorder(null);
        return a;
    }

    private static JScrollPane flatScroll(JComponent c, int height) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setPreferredSize(new Dimension(300, height));
        return sp;
    }

    private static JFrame createFrame(Model model) {
        JFrame f = new JFrame("Factorio Train Network - Deadlock Visualization");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ViewPanel view = new ViewPanel(model);

        class SidePanel extends JPanel implements Scrollable {
            public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
            public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
            public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
            public boolean getScrollableTracksViewportWidth() { return true; }
            public boolean getScrollableTracksViewportHeight() { return false; }
        }
        JPanel side = new SidePanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(SIDE_BG);
        side.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));

        JLabel head = new JLabel("Deadlock Analysis");
        head.setFont(head.getFont().deriveFont(Font.BOLD, 18f));
        head.setForeground(TEXT);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Factorio train network");
        sub.setFont(sub.getFont().deriveFont(12f));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(head);
        side.add(sub);
        side.add(Box.createVerticalStrut(14));

        // result
        JComboBox<String> combo = new JComboBox<>();
        for (int i = 0; i < model.results.size(); i++) {
            Result r = model.results.get(i);
            combo.addItem("Result " + (i + 1) + (r.deadlock ? ": deadlock (" : ": no deadlock (") + r.path.size() + " nodes)");
        }
        side.add(card("Result", combo));
        side.add(Box.createVerticalStrut(10));

        // explanation
        JTextArea explain = textArea(7, false);
        side.add(card("What is happening here?", flatScroll(explain, 170)));
        side.add(Box.createVerticalStrut(10));

        // display options
        JCheckBox cbTrains = check("Trains and signals", true);
        JCheckBox cbWait = check("\"Waits for\" arrows", true);
        JCheckBox cbOthers = check("Highlight rest of network", true);
        JCheckBox cbAll = check("Label all nodes", false);
        JCheckBox cbTracks = check("Track-IDs", false);
        JCheckBox cbWeb = check("Path dependencies (debug)", false);
        JPanel checks = new JPanel(new GridLayout(0, 1, 0, 2));
        checks.setOpaque(false);
        for (JCheckBox c : new JCheckBox[]{cbTrains, cbWait, cbOthers, cbAll, cbTracks, cbWeb}) checks.add(c);
        side.add(card("Display", checks));
        side.add(Box.createVerticalStrut(10));

        // table
        DefaultTableModel tm = new DefaultTableModel(new Object[]{"#", "Node", "Section", "Train", "Type"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(tm);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);
                l.setFont(l.getFont().deriveFont(Font.BOLD, 11f));
                l.setForeground(MUTED);
                l.setBackground(Color.WHITE);
                l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                        BorderFactory.createEmptyBorder(2, 6, 4, 6)));
                return l;
            }
        });
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, c);
                l.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                l.setBackground(sel ? new Color(0xdbe8ff) : r % 2 == 0 ? Color.WHITE : new Color(0xf6f8fb));
                l.setForeground(TEXT);
                l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
                String s = String.valueOf(v);
                if (c == 3 && s.startsWith("Train")) {
                    int ti = Integer.parseInt(s.substring(6).trim());
                    for (Train tr : view.scenario.trains) if (tr.index == ti) l.setForeground(tr.color.darker());
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                } else if (c == 4 && s.equals("Input")) {
                    l.setForeground(new Color(0x1d9a57));
                } else if (c == 4 && s.equals("Output")) {
                    l.setForeground(new Color(0x7b4fd6));
                }
                return l;
            }
        });
        int[] widths = {26, 48, 58, 56, 70};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JScrollPane tsp = new JScrollPane(table);
        tsp.setBorder(null);
        tsp.getViewport().setBackground(Color.WHITE);
        tsp.setPreferredSize(new Dimension(300, 30 + 24 * Math.max(4, Math.min(12, model.results.isEmpty() ? 4 : model.results.get(0).path.size()))));
        side.add(card("Nodes in path (output order)", tsp));
        side.add(Box.createVerticalStrut(10));

        // details
        JTextArea detail = textArea(7, true);
        side.add(card("Details of selected node", flatScroll(detail, 150)));
        side.add(Box.createVerticalStrut(12));

        // Buttons
        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 6));
        buttons.setOpaque(false);
        FlatButton bDl = new FlatButton("Zoom to deadlock", true);
        FlatButton bFit = new FlatButton("Fit whole network", false);
        FlatButton bPng = new FlatButton("Export as PNG ...", false);
        buttons.add(bDl);
        buttons.add(bFit);
        buttons.add(bPng);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(buttons);
        side.add(Box.createVerticalStrut(10));
        JLabel hint = new JLabel("<html><body style='width:300px'><small>Mouse wheel: zoom &nbsp;·&nbsp; Drag: pan &nbsp;·&nbsp; Click a track, train or label: select node &nbsp;·&nbsp; Double-click: fit view</small></body></html>");
        hint.setForeground(MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(hint);

        // cards fill the width but must not grow in height
        for (Component c : side.getComponents()) {
            if (c instanceof JPanel && c != buttons) {
                c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
            }
        }
        combo.setPreferredSize(new Dimension(300, 28));

        // --- logic
        final boolean[] updating = {false};
        Runnable refreshTable = () -> {
            updating[0] = true;
            tm.setRowCount(0);
            Result r = view.current();
            if (r != null) {
                int i = 1;
                for (int id : r.path) {
                    Node n = model.nodes.get(id);
                    String typ = n.input && n.output ? "In/Out" : n.input ? "Input" : n.output ? "Output" : "-";
                    String zug = "-";
                    for (Train tr : view.scenario.trains) if (tr.nodeId == id) zug = "Train " + tr.index;
                    tm.addRow(new Object[]{i++, "N" + id, n.sectionId, zug, typ});
                }
            }
            updating[0] = false;
            explain.setText(view.scenario.explanation);
            explain.setCaretPosition(0);
        };
        Runnable refreshDetail = () -> {
            Node n = model.nodes.get(view.selectedNode);
            if (n == null) {
                detail.setText("No node selected.\nClick a track, a train or a row in the table.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Node ").append(n.id).append("   Section ").append(n.sectionId)
                    .append(String.format(Locale.ROOT, "   Length %.1f%n", n.length));
            sb.append("Input: ").append(n.input).append("   Output: ").append(n.output).append('\n');
            sb.append("nextNodes: ").append(n.next).append('\n');
            sb.append("protectedFrom: ").append(n.protectedFrom).append("\n\n");
            sb.append("Tracks (id, name, dir, x/y in tiles):\n");
            for (int tid : n.trackIds) {
                Track t = model.tracks.get(tid);
                sb.append(String.format(Locale.ROOT, " t%-3d %-28s d%-2d %.0f/%.0f%n",
                        t.id, t.name, t.dir, t.x, t.y));
            }
            detail.setText(sb.toString());
            detail.setCaretPosition(0);
        };
        view.onSelectionChanged = () -> {
            refreshDetail.run();
            updating[0] = true;
            table.clearSelection();
            Result r = view.current();
            if (r != null) {
                int idx = r.path.indexOf(view.selectedNode);
                if (idx >= 0) table.setRowSelectionInterval(idx, idx);
            }
            updating[0] = false;
        };
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || updating[0]) return;
            int row = table.getSelectedRow();
            Result r = view.current();
            if (row >= 0 && r != null && row < r.path.size()) {
                view.selectedNode = r.path.get(row);
                refreshDetail.run();
                view.repaint();
            }
        });
        combo.addActionListener(e -> {
            view.setResult(Math.max(0, combo.getSelectedIndex()));
            view.selectedNode = -1;
            refreshTable.run();
            refreshDetail.run();
            view.repaint();
        });
        cbTrains.addActionListener(e -> { view.showTrains = cbTrains.isSelected(); view.repaint(); });
        cbWait.addActionListener(e -> { view.showWaitArrows = cbWait.isSelected(); view.repaint(); });
        cbAll.addActionListener(e -> { view.showAllLabels = cbAll.isSelected(); view.repaint(); });
        cbTracks.addActionListener(e -> { view.showTrackIds = cbTracks.isSelected(); view.repaint(); });
        cbOthers.addActionListener(e -> { view.showOthers = cbOthers.isSelected(); view.repaint(); });
        cbWeb.addActionListener(e -> { view.showWeb = cbWeb.isSelected(); view.repaint(); });
        bFit.addActionListener(e -> view.fit(false));
        bDl.addActionListener(e -> view.fit(true));
        bPng.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("deadlock.png"));
            if (fc.showSaveDialog(f) != JFileChooser.APPROVE_OPTION) return;
            BufferedImage img = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            view.paint(g);
            g.dispose();
            try {
                ImageIO.write(img, "png", fc.getSelectedFile());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(f, "Export failed: " + ex.getMessage());
            }
        });

        refreshTable.run();
        refreshDetail.run();

        JScrollPane sideScroll = new JScrollPane(side, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sideScroll.setBorder(null);
        sideScroll.getVerticalScrollBar().setUnitIncrement(16);
        sideScroll.setMinimumSize(new Dimension(370, 100));
        sideScroll.setPreferredSize(new Dimension(390, 100));
        view.setMinimumSize(new Dimension(300, 300));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, view, sideScroll);
        split.setResizeWeight(1.0);
        split.setBorder(null);
        split.setDividerSize(4);
        f.setContentPane(split);
        f.setSize(1400, 880);
        split.setDividerLocation(1400 - 410);
        f.setLocationRelativeTo(null);
        f.addComponentListener(new ComponentAdapter() {
            boolean first = true;

            @Override
            public void componentResized(ComponentEvent e) {
                if (first && view.getWidth() > 100) {
                    first = false;
                    view.fit(false);
                }
            }
        });
        return f;
    }

    // ------------------------------------------------------------------------------------------
    // Standalone start
    // ------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: DeadlockViewer <output.json> [--png out.png] [--deadlock N] [--select NODEID] [--size WxH]");
            System.exit(1);
        }
        String json = new String(Files.readAllBytes(new File(args[0]).toPath()), StandardCharsets.UTF_8);
        File png = null;
        int idx = 0, sel = -1, w = 1400, h = 900;
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--png") && i + 1 < args.length) png = new File(args[++i]);
            else if (args[i].equals("--deadlock") && i + 1 < args.length) idx = Integer.parseInt(args[++i]) - 1;
            else if (args[i].equals("--select") && i + 1 < args.length) sel = Integer.parseInt(args[++i]);
            else if (args[i].equals("--size") && i + 1 < args.length) {
                String[] p = args[++i].split("x");
                w = Integer.parseInt(p[0]);
                h = Integer.parseInt(p[1]);
            }
        }
        if (png != null) {
            exportPng(json, idx, sel, png, w, h);
            System.out.println("PNG written: " + png.getAbsolutePath());
        } else {
            show(json);
        }
    }
}
