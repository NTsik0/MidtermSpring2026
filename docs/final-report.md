# Final Project Report

## Implemented UNO Rules

All major UNO rules are implemented:

- Correct deck composition (108 cards, four colors, all card types)
- Legal play validation (color, number, action type matching, wild rules)
- Skip (next player loses turn, two-player variant handled)
- Reverse (direction changes, two-player variant handled)
- Draw Two (next player draws two and loses turn)
- Wild (player chooses color, enforced on next play)
- Wild Draw Four (player chooses color, next player draws four and loses turn)
- Draw/Pass behavior (draw one card, may play if legal)
- UNO call detection and missed-UNO penalty for all players (bots and human)
- Round scoring (number=face value, action=20, wild=50)
- Multi-round game to target score of 500 points

## How To Play From The CLI

Build and run:

```bash
mvn package -DskipTests
java -jar target/uno-cli.jar --bots 3 --games 1
```

Play as human against bots:

```bash
java -jar target/uno-cli.jar --human --bots 2 --games 1
```

During your turn, type a card code or index to play it, or type `draw` to draw a card.

Card format examples:
- `R5` = Red 5
- `GS` = Green Skip
- `BR` = Blue Reverse
- `G+2` = Green Draw Two
- `W` = Wild
- `W4` = Wild Draw Four

View game statistics:

```bash
java -jar target/uno-cli.jar --stats
```

## Architecture

Game logic is separated from CLI interaction:

- `GameEngine.java` — core game loop, turn logic, effect application, scoring, UNO detection. Fully testable without console input.
- `Card.java` — card utilities: color, rank, number, points, legality
- `Deck.java` — deck building, drawing, discarding, reshuffling
- `Player.java` — player state, bot card selection, bot color selection
- `GameView.java` — all console output and human input
- `Main.java` — CLI entry point, argument parsing, multi-round orchestration
- `persistence/` — MyBatis ORM, H2 database, game result storage and queries

The CLI does not contain game rules. Rules live in `GameEngine` and `Card`. Tests call `GameEngine` directly without any console interaction.

## Tests Added

- `UnoTest.java` — 48 characterization tests from midterm (card rules, bot behavior, scoring)
- `UnoRulesTest.java` — 41 new tests covering all rule features (deck composition, legal play, Skip, Reverse, Draw Two, Wild, Wild Draw Four, draw/pass, UNO penalty, scoring, multi-round target)
- `PersistenceTest.java` — 5 tests for database persistence layer

Total: 94 tests, all passing.

## Limitations

- Wild Draw Four has no challenge mechanism
- No official UNO stacking rule (Draw Two on Draw Two)
- No official UNO challenge mechanism for Wild Draw Four
- No network/multiplayer support
- Bot strategy is simple (prefers Draw Two > Skip > Number > Wild)
