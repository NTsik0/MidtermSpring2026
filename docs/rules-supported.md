# UNO Rules Supported

## Deck Composition

- Four colors: Red (R), Yellow (Y), Green (G), Blue (B)
- Number cards 0-9 per color (0 appears once, 1-9 appear twice)
- Skip cards (2 per color)
- Reverse cards (2 per color)
- Draw Two cards (2 per color)
- Wild cards (4 total)
- Wild Draw Four cards (4 total)
- Total: 108 cards

## Legal Play Validation

- Match by color
- Match by number
- Match by action type (Skip on Skip, Reverse on Reverse, Draw Two on Draw Two)
- Wild and Wild Draw Four are always legal
- Called color rule enforced after Wild is played
- Illegal plays are rejected and player draws a penalty card

## Skip

- Next player loses their turn
- Two-player variant: acts as an extra turn for current player

## Reverse

- Reverses play direction for 3+ players
- Two-player variant: acts like Skip (current player goes again)

## Draw Two

- Next player draws two cards and loses their turn

## Wild

- Player chooses the next color (R, Y, G, B)
- Chosen color is enforced for next play

## Wild Draw Four

- Player chooses the next color
- Next player draws four cards and loses their turn

## Draw/Pass Behavior

- Player draws one card when no legal play is available
- If drawn card is legal, player may play it immediately
- If drawn card is not legal, turn passes to next player
- This differs from official UNO where player must pass after drawing

## UNO Call

- One-card state is detected and announced automatically
- Any player (human or bot) with one card when another player calls UNO draws 2 penalty cards
- Human UNO call is announced via output

## Round Scoring

- Number cards: face value
- Skip, Reverse, Draw Two: 20 points each
- Wild, Wild Draw Four: 50 points each
- Round winner receives points from all other players' hands

## Multi-Round Game

- Game continues until a player reaches 500 points
- First player to reach 500 points wins the game
- Each round resets hands and deals fresh cards
