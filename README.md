# Lunar-lander

Lunar-lander wrangles your cassandra cluster. It is an environment agnostic automation tool built around ssh, cql, and bash. 


## Design Objectves

To help users tackle the diverse challenges of upgrading c* clusters, the tool:
- Is environment agnostic, relies only on ssh, bash shell, and cql to function.
- Has a designer UI that allows for quick testing and designing of upgrade paths (known as missions).
- Supports canary deployments and rolling deployments for missions.
- Provides real-time feedback for upgrades.

## Design Concepts

Users design, test, and execute missions.

Missions are composed of reusable sequences of bash shell commands.

## User

Download the latest release from https://github.com/phact/lunar-lander/releases

## Dev

To run in dev (including hot loading for both java and javascript) run:

    ./mvnw compile quarkus

and

    cd lunar-lander-ui && ./watch-generate.sh 

Changes to the source code are picked up automatically, it takes javascript changes a few seconds to propagate.

## Build

    ./mvnw package -Pnative

