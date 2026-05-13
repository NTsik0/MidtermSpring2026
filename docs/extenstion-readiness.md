# Extension Readiness

## Which extension would your design support best?

Variable bot difficulty. The current bot always follows the same priority
order: DRAW_TWO > SKIP > NUMBER > WILD. A natural extension would be adding
different strategies, for example an aggressive bot that prefers Wild Draw Four,
a passive bot that avoids action cards, or a random bot for baseline testing.

## Where would that change be implemented?

chooseCard() and chooseColor() are now instance methods on Player with a clean
signature that takes only hand, upCard, and calledColor. Adding a new strategy
would mean adding a strategy field to Player and branching inside those two
methods based on it. takeTurn() already dispatches on player.human, so adding
a strategy check there is one more condition in a place that already handles
dispatch. No other class would need to change.

## What part of your design still makes that change difficult?

Player has no strategy concept yet so any extension requires touching Player,
setupPlayers(), and the argument parser in main() all at once. There is also
no way to test a new strategy in isolation because tests call chooseCard()
directly on Player, which means all strategies would share the same test path.
A BotStrategy interface would let each strategy be tested and swapped
independently, but that restructure was not done here.