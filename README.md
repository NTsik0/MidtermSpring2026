# UNO CLI

A command-line UNO card game written in Java.

## Requirements

- Java 11+
- Maven 3.9+
- Docker (optional)

## Local Build

```bash
mvn compile
```

## Local Test

```bash
mvn test
```

## Local Run

```bash
mvn package -DskipTests
java -jar target/uno-cli.jar --bots 3 --games 1
```

## Package Creation

```bash
mvn package
```

This creates `target/uno-cli.jar`.

## Run Options
--bots N     Number of bot players (default: 3)

--games N    Number of games to play (default: 1)

--quiet      Suppress game output, show only logs

--human      Add a human player

--seed N     Set random seed for reproducibility

## Examples

Run 5 quiet bot games:
```bash
java -jar target/uno-cli.jar --bots 3 --games 5 --quiet
```

Play as human against 2 bots:
```bash
java -jar target/uno-cli.jar --human --bots 2 --games 1
```

Run self-test:
```bash
java -jar target/uno-cli.jar --self-test
```

## Docker Build

```bash
docker build -t uno-cli .
```

## Docker Run

```bash
docker run --rm uno-cli
```

Run with custom options:
```bash
docker run --rm uno-cli --bots 3 --games 5 --quiet
```

## Card Format
R5    Red 5

YS    Yellow Skip

BR    Blue Reverse

G+2   Green Draw Two

W     Wild

W4    Wild Draw Four

draw  Draw a card

## Logging

The game logs important events using `java.util.logging`:
- Game start
- Player turns
- Cards played and drawn
- Invalid input
- Round and game end