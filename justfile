all: build run

build:
    ./mvnw package

run:
    java -agentlib:jdwp=transport=dt_socket,server=y,address=*:5005,suspend=n -jar ./target/conductor-0.0.1-SNAPSHOT.jar

native-build:
    ./mvnw -DskipTests=true -Pnative -Dagent package

native-run:
    ./mvnw -Pnative -Dagent exec:exec@java-agent