import java.util.ArrayList;
import java.util.Random;

public class GameSession {
    ArrayList<Player> players;
    Deck deck;
    Random random;
    GameView view;
    String upCard;
    String calledColor;
    int currentPlayer;
    int direction;
    int[] scores;

    public GameSession(ArrayList<Player> players, Deck deck, Random random, GameView view) {
        this.players = players;
        this.deck = deck;
        this.random = random;
        this.view = view;
        this.scores = new int[players.size()];
        this.direction = 1;
        this.currentPlayer = 0;
        this.upCard = "";
        this.calledColor = "";
    }

    public void setupRound() {
        deck.build();
        for (Player p : players) {
            p.hand.clear();
            for (int j = 0; j < 7; j++) p.hand.add(deck.draw());
        }
        upCard = deck.draw();
        while (upCard.startsWith("W")) {
            deck.discard(upCard);
            upCard = deck.draw();
        }
        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(players.size());
    }

    public boolean takeTurn() {
        String name = players.get(currentPlayer).name;
        ArrayList<String> hand = players.get(currentPlayer).hand;

        view.showUpCard(upCard, calledColor);
        view.showHand(name, hand);

        int chosen = players.get(currentPlayer).human
                ? view.askHuman(hand, upCard, calledColor)
                : players.get(currentPlayer).chooseCard(upCard, calledColor);

        if (chosen == -1) {
            String drawn = deck.draw();
            hand.add(drawn);
            view.showDraw(name, drawn);
            if (Card.isLegal(drawn, upCard, calledColor)) {
                if (!players.get(currentPlayer).human) {
                    chosen = hand.size() - 1;
                } else {
                    if (view.askYesNo()) chosen = hand.size() - 1;
                }
            } else {
                next();
                return false;
            }
        }

        if (chosen >= 0) {
            if (chosen >= hand.size()) {
                view.showBadIndex(name);
                hand.add(deck.draw());
                next();
                return false;
            }
            String card = hand.get(chosen);
            if (!Card.isLegal(card, upCard, calledColor)) {
                view.showPenalty(name);
                hand.add(deck.draw());
                next();
                return false;
            }
            hand.remove(chosen);
            deck.discard(upCard);
            upCard = card;
            calledColor = "";
            view.showPlay(name, card);

            if (card.equals("W") || card.equals("W4")) {
                calledColor = players.get(currentPlayer).human
                        ? view.askColor()
                        : players.get(currentPlayer).chooseColor();
                view.showColorCall(name, calledColor);
            }

            if (hand.size() == 1) view.showUno(name);

            if (hand.size() == 0) {
                int points = countPoints();
                scores[currentPlayer] += points;
                view.showWin(name, points);
                return true;
            }

            applyEffect(card);
        } else {
            next();
        }
        return false;
    }

    int countPoints() {
        int points = 0;
        for (int i = 0; i < players.size(); i++) {
            if (i != currentPlayer) {
                for (String c : players.get(i).hand) points += Card.points(c);
            }
        }
        return points;
    }

    void applyEffect(String card) {
        String r = Card.rank(card);
        if (r.equals("SKIP")) {
            next();
            next();
        } else if (r.equals("REVERSE")) {
            direction = direction * -1;
            if (players.size() == 2) {
                next();
                next();
            } else {
                next();
            }
        } else if (r.equals("DRAW_TWO")) {
            next();
            players.get(currentPlayer).hand.add(deck.draw());
            players.get(currentPlayer).hand.add(deck.draw());
            view.showDrawTwo(players.get(currentPlayer).name);
            next();
        } else if (r.equals("WILD_DRAW_FOUR")) {
            next();
            for (int i = 0; i < 4; i++) {
                players.get(currentPlayer).hand.add(deck.draw());
            }
            view.showDrawFour(players.get(currentPlayer).name);
            next();
        } else {
            next();
        }
    }

    void next() {
        currentPlayer += direction;
        if (currentPlayer >= players.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = players.size() - 1;
    }

    int[] getScores() { return scores; }
}
