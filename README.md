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

## Build jar

    ./mvnw package

## Build native image

    ./mvnw package -Pnative


Followed by manual native image build when it fails (until https://github.com/quarkusio/quarkus/issues/6598 gets resolved):

    /opt/graalvm-ce-java11-19.3.1/bin/native-image -J-Dsun.nio.ch.maxUpdateArraySize=100 -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager -J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory -J-Dvertx.disableDnsResolver=true -J-Dio.netty.leakDetection.level=DISABLED -J-Dio.netty.allocator.maxOrder=1 -H:ResourceConfigurationFiles=resources-config.json -H:Log=registerResource  -H:ReflectionConfigurationFiles=../../reflection-config/reflect-config.json --initialize-at-build-time= -H:InitialCollectionPolicy='com.oracle.svm.core.genscavenge.CollectionPolicy$BySpaceAndTime' -jar lunar-lander-1.0.0-SNAPSHOT-runner.jar -J-Djava.util.concurrent.ForkJoinPool.common.parallelism=1 -H:FallbackThreshold=0 -H:+ReportExceptionStackTraces -H:+AddAllCharsets -H:EnableURLProtocols=http -H:NativeLinkerOption=-no-pie -H:+JNI -H:JNIConfigurationFiles=../../reflection-config/jni-config.json --no-server -H:-UseServiceLoaderFeature -H:+StackTrace lunar-lander-1.0.0-SNAPSHOT-runner

