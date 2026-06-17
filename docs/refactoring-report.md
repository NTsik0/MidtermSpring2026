# Refactoring Report

## What behavior did you characterize before refactoring?

I expanded selfTest() from 9 checks to 58 checks including:
- color(), rank(), number(), points() for all card types including wilds and action cards
- isLegal() for color match, number match, action type match,
  wild always legal, called color rule, and illegal mismatches
- Bot card preference order: DRAW_TWO > SKIP > NUMBER > WILD > draw(-1)
- Bot color selection: picks most common color in hand, tie-break behavior
- Scoring math: number=face value, action=20, wild=50
- join() format ("0:R5 1:W")
- draw() fallback when both piles empty returns "W"
- Penalty draw when bot plays an out-of-bounds index

## What were the worst design problems you found?

1. Duplicated isLegal logic: the legality check was literally copy-pasted four
   times inside chooseBotCard() and existed separately as isLegal().
   Any rule change needed five edits in different places. So it was one of the main design problems.
2. Mixed responsibilities in playGame(): deck building, turn flow,
   console output, scoring, and effect handling all in one 130+ line
   method without any separation was also a design problem.
3. Primitive card representation: cards were bare Strings with no type at all.
   Bugs like color("W4") returning "" were invisible until they were actually tested.
4. Global mutable state: 9 static fields meant any method could change any state 
   silently with no clear ownership.
5. Three parallel lists (playerNames, humanPlayers, hands) that had to
   stay manually in sync whenever a player was added or removed was also design problem.
6. Bot logic and human input both sitting in Main with no clear home.

## Which refactorings did you perform?

1. Extracted Class: Card.java - color/rank/number/points/isLegal in one place
2. Centralized isLegal: removed the four duplicates inside chooseBotCard()
3. Extracted Class: Deck.java - build, draw, discard, and reshuffle logic
4. Extracted Class: Player.java - replaced three parallel lists with one object
5. Extracted Class: GameView.java - all sout calls moved here,  quiet flag now is in the view instead of being used 
   through playGame()
6. Extract Method: takeTurn() - separated single turn logic from the game loop
7. Move Method: chooseCard() and chooseColor() moved from Main to Player.java
8. Move Method: askHuman(), askColor(), askYesNo() moved from Main to GameView.java
9. Extracted method: countPoints() - I have seperated scoring calculation from game completion detection inside takeTurn()
   They were written in the same block, and it was kind of unclear where scoring ended and where was win detection beginning.
10. Added two new tests: Deck.draw() fallback now tested through a Deck directly,
   previously done through static draw() in Main, and the bot wild quirk where
   the wild fallback loop has no isLegal guard.
11. Removed chooseBotCard() and chooseBotColor() from Main.java entirely. selfTest() now
   calls bot logic through Player instances so Player methods are directly tested.
12. Extracted Class: GameSession.java - upCard, calledColor, currentPlayer, direction,
   scores, takeTurn(), applyEffect(), countPoints(), and next() all moved out of Main.
   Main.playGame() now creates a GameSession, calls setupRound(), runs the guard loop,
   and merges scores back. This removes all turn-level mutable state from Main.
13. Added injectable Scanner to GameView: a second constructor GameView(boolean, Scanner)
   lets tests pass string-backed scanners instead of reading from System.in.
14. Added 14 new characterization tests: applyEffect for skip, reverse (2p and 3p),
   draw_two, and wild_draw_four; countPoints excluding the winner; askColor valid and
   retry; askYesNo y and n; askHuman with DRAW, card code, and index input; and a safety
   limit characterization confirming scores stay 0 when no winner within the guard.

Each step was done separately and tests were run after each one to confirm
nothing broke before moving on. Total checks grew from 58 to 72.

## What behavior did you intentionally preserve?

- Humans can type "draw" even when they hold a legal card (no must-play rule)
- Typing an index for an illegal card causes penalty draw and turn loss, also typing a card code for an 
  illegal card just re-prompts
- Bot players automatically play a drawn card if it is legal
- All hands are visible to everyone in the terminal
- 2-player reverse acts as a skip, logically it goes to same move what should have happend after 2 reverses
- "W" fallback card when both piles are empty
- Safety limit of 3000 turns prints a message and stops the game
- 
## What risks remain?

- applyEffect() is still a chain of if/else if blocks inside GameSession.
  Each branch is now tested, but replacing the chain with a strategy or
  command pattern would require a larger restructure.
- GameSession fields (upCard, calledColor, currentPlayer, direction) are
  package-visible so selfTest() can access them directly. Making them private
  would require adding more accessor methods.
- The safety limit (3000 turns per game) is a documented guard. A bot-only
  game can reach it with certain seeds. When it fires, scores correctly stay
  at zero and the message is printed. This is now characterized in selfTest().