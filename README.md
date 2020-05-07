# Lunar-lander

Lunar-lander wrangles your cassandra cluster. It is an environment agnostic automation tool built around ssh, cql, and bash. 


## Design Objectves

To help users tackle the diverse challenges of upgrading c* clusters, the tool:
- Is environment agnostic, relies only on ssh, bash shell, and cql to function.
- Has a designer UI that allows for quick testing and designing of upgrade paths (known as missions).
- Supports canary deployments and rolling deployments for missions.
- Provides real-time feedback for upgrades.

## Design Concepts

Users design, test, and deploy missions.

Missions are composed of reusable sequences of bash shell commands.

Missions can be deployed on clusters in a as canary deployments (one random node) or in as rolling deployments (the whole cluster where the concurrency is determined per sequence by the mission designer).

### Concurrency

Regardless of the Guarantees:

 - The commands inside a sequence are always guaranteed to run sequentially on a host
 - Multiple sequences are always guaranteed to run in order across all the hosts (i.e. no host will start sequence 2 until sequence 1 has completed on all hosts).

For each sequence, one of the following Concurrency Types can be selected:

 - NODE - the sequence will get run node by node
 - CLUSTER - the sequence will get run in parallel for all the nodes in the cluster

## User

Download the latest release from https://github.com/phact/lunar-lander/releases

## Dev

To run in dev (including hot loading for both java and javascript) run:

    ./mvnw compile quarkus:dev

and

    cd lunar-lander-ui && ./watch-generate.sh 

Changes to the source code are picked up automatically, it takes javascript changes a few seconds to propagate.

## Build jar

    ./mvnw package

## Build native image

    ./mvnw package -Pnative

## Native image build prereqs

https://quarkus.io/guides/building-native-image#prerequisites
