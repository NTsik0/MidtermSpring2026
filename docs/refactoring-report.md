# Refactoring Report

## What behavior did you characterize before refactoring?

I expanded selfTest() from 9 checks to 56 checks including:
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
   
Each step was done separately and tests were run after each one to confirm
   nothing broke before moving on.

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

- selfTest() still uses the old static deck/discard lists and the legacy
  draw() method, so the deck fallback is not tested through Deck.java.
- Global state (currentPlayer, direction, upCard, calledColor) is still in
  Main. I could have created a seperated, new Game class that would fix this 
  but it would have been a really big restructure that needs a lot more test coverages.
- Scoring is still mixed with game completion inside takeTurn(). Separating it would require the same things that
  I previously said, creating a new Game class.
- applyEffect() is still a chain of if/else if blocks. Replacing it with a cleaner structure would need 
  more tests around each effect first.
- Human input (askYesNo, askColor) cannot be tested without live Scanner input.n, so live scanner is in action.