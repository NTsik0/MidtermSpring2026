# UNO CLI

A command-line UNO card game written in Java with full rule implementation,
multi-round scoring, database persistence, and comprehensive tests.

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

## Run Options
--bots N     Number of bot players (default: 3)

--games N    Number of multi-round games (default: 1)

--quiet      Suppress game output, show only logs

--human      Add a human player

--seed N     Set random seed for reproducibility

--stats      Show game history and statistics

## Examples

Run a full multi-round game to 500 points:
```bash
java -jar target/uno-cli.jar --bots 3 --games 1
```

Play as human against 2 bots:
```bash
java -jar target/uno-cli.jar --human --bots 2 --games 1
```

Run self-test:
```bash
java -jar target/uno-cli.jar --self-test
```

View game statistics:
```bash
java -jar target/uno-cli.jar --stats
```

## Docker Build

```bash
docker build -t uno-cli .
```

## Docker Run

```bash
docker run --rm uno-cli
```

## Card Format
R5    Red 5

YS    Yellow Skip

BR    Blue Reverse

G+2   Green Draw Two

W     Wild

W4    Wild Draw Four

draw  Draw a card

## Rules

See `docs/rules-supported.md` for full list of implemented rules.

## Logging

The game logs important events using `java.util.logging`:
- Game start and end
- Player turns
- Cards played and drawn
- Invalid input
- Round and game results

## Database

Game results are stored in H2 database automatically.
See `docs/database.md` for details.

## Documentation

- `docs/rules-supported.md` — implemented rules
- `docs/database.md` — database setup and usage
- `docs/final-report.md` — final project report