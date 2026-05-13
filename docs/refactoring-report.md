# Refactoring Report

## What behavior did you characterize before refactoring?

I expanded checks for selfTest() from 9 checks to 56 checks with including:
- color(), rank(), number(), points() for all card types including wilds
- isLegal() for color , number match and action type match,
  wild always legal, called color rule, and illegal mismatches
- Bot card preference order: DRAW_TWO > SKIP > NUMBER > WILD > draw
- Bot color selection: picks most common color in hand
- Scoring math: number=face value, action=20, wild=50
- join() format ("index:card index:card")
- draw() fallback when both piles are empty returns "W"

## What were the worst design problems you found?

1. Duplicated isLegal logic: the five-condition legality check
   was copy-pasted four times inside chooseBotCard() AND existed
   as a standalone isLegal() method. Any rule change required
   five edits in different places.

2. Mixed responsibilities in playGame(): deck building, turn flow,
   console output, scoring, and card effect handling all was in
   one 130-line method with no boundaries at all.

3. Primitive card representation: cards were bare Strings with
   no type. Bugs like color("W4") returning "" were invisible
   until it was tested.

4. Global mutable state: 9 static fields meant any method could
   change any state silently with no clear ownership.

5. Three parallel lists (playerNames, humanPlayers, hands) that
   had to stay manually in sync.

## Which refactorings did you perform?

1. Extracted Class: Card.java - moved color/rank/number/points/isLegal
2. Centralized isLegal: removed duplication in chooseBotCard
3. Extracted Class: Deck.java - deck building, draw, discard in one place
4. Extracted Class: Player.java - replaced three parallel lists with one
5. Extracted Class: GameView.java - all System.out calls moved here,
   quiet flag now lives in GameView not scattered across playGame()
6. Extract Method: applyEffect() - seperated card effect logic from
   the main turn loop

Each step was committed separately. Tests were run after each step.
Checked one by one after doing refactorings.

## What behavior did you intentionally preserve?

- Humans can type "draw" even when they hold a legal card
- Illegal index input causes a penalty card and turn loss
- Bot players automatically play a drawn card when legal
- All hands are visible in the terminal
- 2-player reverse acts as a skip
- W fallback card when both piles are empty
- Safety limit of 3000 turns that prints a message and stops

## What risks remain?

- selfTest() still uses global state (upCard, calledColor) directly,
  so tests can interfere with each other if order changes.
- playGame() is still long. A Game class extraction would be the
  next step.
- The bot strategy is still coupled to global state via static fields.
- The three old lists (playerNames, humanPlayers, hands) are still
  kept for selfTest() compatibility, which is confusing.