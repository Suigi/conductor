all: build run

build:
    ./mvnw package

run:
    java -jar ./target/conductor-0.0.1-SNAPSHOT.jar
