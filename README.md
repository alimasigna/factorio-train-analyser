# factorio-train-analyser
A tool to analyse train blueprints from factorio to recognize deadlocks and performance issues. For now this tool can only analyse wether a deadlock is possible inside a given train section or not.
If you are trying to understand the alogrithm behind this, you can read our [paper](./docs/Studienarbeit.pdf) inside the docs folder.

## Factorio version support
Blueprints from Factorio 2.0 and later are supported, including the rail shapes
the update introduced (`half-diagonal-rail`, `curved-rail-a`, `curved-rail-b`)
and the `legacy-` rails that old designs import as. Blueprints exported before
2.0 are converted on the fly, the same way the game converts them.

Elevated rails are modelled too, including the `rail-ramp` that joins the two
levels. A flyover never shares a block with the track beneath it, while a block
can still run from the ground up a ramp onto elevated track. See
[the rail model](./docs/RAIL_MODEL_2.0.md) for how connections, signals and
crossings are worked out and what has been verified against the game. The paper
describes the 1.1 implementation, which that document supersedes for the
blueprint-reading half of the tool.
## Requirements
The project has been build and developed using the following versions.
- Java 25 (the current LTS)
- Gradle 9.8.2 (provided by the Gradle wrapper, no local install needed)

Neither has to be installed by hand. The Gradle wrapper (`./gradlew`) downloads
the pinned Gradle version on first use, and the Java toolchain declared in
`app/build.gradle.kts` makes Gradle fetch a matching JDK via the foojay resolver
if no suitable one is already present on the machine.
## Start-Up
There are multiple ways how you can run the application. We recommend the first one if you are just trying to run this application
### Build and run
In the first step, we will build the app from the sources.
- ```./gradlew build```
After that we run the jar-file and add a txt-file which includes the blueprint you want to analyze.
- ```java -jar ./app/build/libs/app.jar <path/to/blueprint.txt>```
The program will generate a output.json which includes all the deadlock combinations and the graph data structure that has been generated. It will also tell you inside the console whether a deadlock was found or not.
### Run in debug mode
If you are trying to debug the application you might need to use this mode. The diffrence to the normal mode is that the blueprint is inside tha App.java as a variable.
- change inside the App.java the isInDebug Flag to true
- replace the placeholder debugBlueprint String with yours
- ```./gradlew run```

Alternatively you can pass a blueprint file straight to the run task, which does
not require touching the sources:
- ```./gradlew run --args="<path/to/blueprint.txt>"```
### Run
If you dont want to build it yourself, you can download the binaries from the release page.
To run it execute:
- ```java -jar ./app/build/libs/app.jar <path/to/blueprint.txt>```
## Example
![Train intersection with a deadlock](./docs/deadlock.png)
