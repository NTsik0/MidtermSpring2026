import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static ArrayList<Player> players = new ArrayList<Player>();
    // i will keep these 2 and the draw() method for selfTest() because test uses them
    static ArrayList<String> deck = new ArrayList<String>();
    static ArrayList<String> discard = new ArrayList<String>();
    // I have updated it, the size now matches actual player count that is initialized in setupPlayers()
    static int[] scores;
    static int currentPlayer = 0;
    static int direction = 1;
    static String upCard = "";
    static String calledColor = "";
    static boolean quiet = false;
    static Random random = new Random();
    static Deck gameDeck;
    static Scanner scanner = new Scanner(System.in);
    static GameView view;

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                games = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--self-test")) {
                selfTest();
                return;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        random = new Random(seed);
        gameDeck = new Deck(random);
        view = new GameView(quiet);
        setupPlayers(bots, human);

        if (players.size() < 2 || players.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        for (int g = 1; g <= games; g++) {
            if (!quiet) {
                System.out.println("\n=== Game " + g + " ===");
            }
            playGame();
        }

        System.out.println("\nFinal scores:");
        for (int i = 0; i < players.size(); i++) {
            System.out.println(players.get(i).name + ": " + scores[i]);
        }
    }

    static void setupPlayers(int bots, boolean human) {
        players.clear();
        if (human) players.add(new Player("You", true));
        for (int i = 1; i <= bots; i++) players.add(new Player("Bot" + i, false));
        scores = new int[players.size()]; // this is now correctly sized
        //  this were used when there were no player class now we have player class so list became redundant.
        //  hands.clear();
        //  for (Player p : players) hands.add(p.hand);
        //  playerNames.clear();
        //  for (Player p : players) playerNames.add(p.name);
        //  humanPlayers.clear();
        //  for (Player p : players) humanPlayers.add(p.human);
    }
    // I rewrote the code, made it shorter, it keeps only the setup and the loop, evrything that was inside here the while ( guard < 3000) loop moves into takeTurn()
    static void playGame() {
        gameDeck.build();
        for (int i = 0; i < players.size(); i++) {
            players.get(i).hand.clear();
            for (int j = 0; j < 7; j++) players.get(i).hand.add(gameDeck.draw());
        }
        upCard = gameDeck.draw();
        while (upCard.startsWith("W")) {
            gameDeck.discard(upCard);
            upCard = gameDeck.draw();
        }
        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(players.size());

        int guard = 0;
        while (guard < 3000) {
            guard++;
            if (takeTurn()) return;
        }
        view.showSafetyLimit();
    }
    // added takeTurn, bacause it returns true when someone wins, false to keep playing,
    static boolean takeTurn() {
        String name = players.get(currentPlayer).name;
        ArrayList<String> hand = players.get(currentPlayer).hand;

        view.showUpCard(upCard, calledColor);
        view.showHand(name, hand);

        int chosen = players.get(currentPlayer).human
                ? askHuman(hand)
                : chooseBotCard(hand, upCard, calledColor);

        if (chosen == -1) {
            String drawn = gameDeck.draw();
            hand.add(drawn);
            view.showDraw(name, drawn);
            if (Card.isLegal(drawn, upCard, calledColor)) {
                if (!players.get(currentPlayer).human) {
                    chosen = hand.size() - 1;
                } else {
                    System.out.print("Play drawn card " + drawn + "? y/n: ");
                    if (scanner.nextLine().equalsIgnoreCase("y")) chosen = hand.size() - 1;
                }
            }
        }

        if (chosen >= 0) {
            if (chosen >= hand.size()) {
                view.showBadIndex(name);
                hand.add(gameDeck.draw());
                next();
                return false;
            }
            String card = hand.get(chosen);
            if (!Card.isLegal(card, upCard, calledColor)) {
                view.showPenalty(name);
                hand.add(gameDeck.draw());
                next();
                return false;
            }
            hand.remove(chosen);
            gameDeck.discard(upCard);
            upCard = card;
            calledColor = "";
            view.showPlay(name, card);

            if (card.equals("W") || card.equals("W4")) {
                calledColor = players.get(currentPlayer).human
                        ? askColor()
                        : chooseBotColor(hand);
                view.showColorCall(name, calledColor);
            }

            if (hand.size() == 1) view.showUno(name);

            if (hand.size() == 0) {
                int points = 0;
                for (int i = 0; i < players.size(); i++) {
                    if (i != currentPlayer) {
                        for (String c : players.get(i).hand) points += Card.points(c);
                    }
                }
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

    static void applyEffect(String card) {
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
            players.get(currentPlayer).hand.add(gameDeck.draw());
            players.get(currentPlayer).hand.add(gameDeck.draw());
            view.showDrawTwo(players.get(currentPlayer).name);
            next();
        } else if (r.equals("WILD_DRAW_FOUR")) {
            next();
            for (int i = 0; i < 4; i++) {
                players.get(currentPlayer).hand.add(gameDeck.draw());
            }
            view.showDrawFour(players.get(currentPlayer).name);
            next();
        } else {
            next();
        }
    }
    // used by test
    static String draw() {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) {
            return "W";
        }
        return deck.remove(0);
    }
    //this down here was repeated 4 times, we rewrote if - else if - else if .... logic like this
    static int chooseBotCard(ArrayList<String> hand, String upCard, String calledColor) {
        // prefer draw_two
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = Card.isLegal(card, upCard, calledColor);
            if (rank(card).equals("DRAW_TWO") && ok) return i;
        }
        // prefer skip
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = Card.isLegal(card, upCard, calledColor);
            if (rank(card).equals("SKIP") && ok) return i;
        }
        // prefer number
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = Card.isLegal(card, upCard, calledColor);
            if (rank(card).equals("NUMBER") && ok) return i;
        }
        // fall back to wild
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) return i;
        }
        return -1;
    }

    static int askHuman(ArrayList<String> hand) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) {
                return -1;
            }
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) {
                    return index;
                }
            } catch (Exception ignored) {
            }
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (isLegal(hand.get(i), upCard, calledColor)) {
                        return i;
                    }
                    System.out.println("That card is not legal.");
                }
            }
            System.out.println("Card not found.");
        }
    }

    static String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R")) {
                return "R";
            }
            if (input.equals("Y")) {
                return "Y";
            }
            if (input.equals("G")) {
                return "G";
            }
            if (input.equals("B")) {
                return "B";
            }
            System.out.println("Bad color.");
        }
    }

    static String chooseBotColor(ArrayList<String> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < hand.size(); i++) {
            String c = Card.color(hand.get(i));
            if (c.equals("R")) {
                r++;
            } else if (c.equals("Y")) {
                y++;
            } else if (c.equals("G")) {
                g++;
            } else if (c.equals("B")) {
                b++;
            }
        }
        if (r >= y && r >= g && r >= b) {
            return "R";
        } else if (y >= r && y >= g && y >= b) {
            return "Y";
        } else if (g >= r && g >= y && g >= b) {
            return "G";
        } else {
            return "B";
        }
    }

    static boolean isLegal(String card, String up, String call) {
        return Card.isLegal(card, up, call);
    }

    static String color(String card) {
        return Card.color(card);
    }

    static String rank(String card) {
        return Card.rank(card);
    }

    static int number(String card) {
        return Card.number(card);
    }

    static int points(String card) {
        return Card.points(card);
    }

    static void next() {
        currentPlayer += direction;
        if (currentPlayer >= players.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = players.size() - 1;
    }
    // removed join method and I will add this to selfTest() that calls join()

    static void selfTest() {
        int passed = 0;
        if (Card.color("R5").equals("R")) passed++; else fail("color R5");
        if (Card.rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (points("W4") == 50) passed++; else fail("wild points");
        if (isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");
        // here I am adding color() tests
        if (Card.color("Y5").equals("Y")) passed++; else fail("color Y5");
        if (Card.color("G+2").equals("G")) passed++; else fail("color G+2");
        if (Card.color("BS").equals("B")) passed++; else fail("color BS");
        if (Card.color("W").equals("")) passed++; else fail("color W is empty");
        if (Card.color("W4").equals("")) passed++; else fail("color W4 is empty");
        // we should also add rank() tests
        if(Card.rank("RS").equals("SKIP")) passed++; else fail("rank RS");
        if(Card.rank("BR").equals("REVERSE")) passed++; else fail("rank BR");
        if(Card.rank("R+2").equals("DRAW_TWO")) passed++; else fail("rank R+2");
        if(Card.rank("W").equals("WILD")) passed++; else fail("rank W");
        if(Card.rank("W4").equals("WILD_DRAW_FOUR")) passed++; else fail("rank W4");
        if(Card.rank("R0").equals("NUMBER")) passed++; else fail("rank R0");
        if(Card.rank("G7").equals("NUMBER")) passed++; else fail("rank G7");
        // now number() tests
        if(Card.number("R0") == 0) passed++; else fail("number R0");
        if(Card.number("G7") == 7) passed++; else fail("number G7");
        if(Card.number("B9") == 9) passed++; else fail("number B9");
        if(Card.number("RS") == -1) passed++; else fail("number of non-number card");
        // and point() tests
        if(Card.points("R5") == 5) passed++; else fail("points R5");
        if(Card.points("G0") == 0) passed++; else fail("points G0");
        if(Card.points("BS") == 20) passed++; else fail("points BS");
        if(Card.points("YR") == 20) passed++; else fail("points YR");
        if(Card.points("R+2") == 20) passed++; else fail("points R+2");
        if(Card.points("W") == 50) passed++; else fail("points W");
        // now we have to add tests for isLegal() by 3 things, match by color, number, action type (same rank), wilds are always legal, called color rule (this is after a wild is played)
        // match by color
        if (isLegal("R9","R5","")) passed++; else fail("legal:same color");
        if (isLegal("RS","R5","")) passed++; else fail("legal:same color action");
        if (!isLegal("G5","R7","")) passed++; else fail("illegal: different color different number");
        // match by number
        if (isLegal("G5","R5","")) passed++; else fail("legal:same number");
        if (isLegal("B5","Y5","")) passed++; else fail("legal:same number different color");
        if (!isLegal("R4","G7","")) passed++; else fail("illegal:different number different color");
        // match by action type (same rank)
        if (isLegal("GS", "RS", "")) passed++; else fail("legal: skip on skip");
        if (isLegal("BR", "YR", "")) passed++; else fail("legal: reverse on reverse");
        if (isLegal("Y+2", "R+2", "")) passed++; else fail("legal: draw two on draw two");
        // wilds are always legal
        if (isLegal("W", "R5", "")) passed++; else fail("legal: wild always");
        if (isLegal("W4", "G9", "")) passed++; else fail("legal: W4 always");
        if (isLegal("W", "W4", "")) passed++; else fail("legal: wild on W4");
        // called color rule (after a wild is played)
        if (isLegal("G3", "W", "G")) passed++; else fail("legal: called color match");
        if (isLegal("GS", "W4", "G")) passed++; else fail("legal: action on called color");
        if (!isLegal("R3", "W", "G")) passed++; else fail("illegal: wrong color on called color");

        // here we gotta add tests for bot behaviour quirks
        // bot prefers draw_two over skip over number over wild then
        ArrayList<String> botHand1 = new ArrayList<String>();
        botHand1.add("R+2");   // draw two - should be chosen first
        botHand1.add("RS");    // skip
        botHand1.add("R5");    // number
        botHand1.add("W");     // wild
        if (chooseBotCard(botHand1, "R7", "") == 0) passed++; else fail("bot prefers draw two");

        ArrayList<String> botHand2 = new ArrayList<String>();
        botHand2.add("RS");    // skip - should be chosen (no draw two available)
        botHand2.add("R5");    // number
        botHand2.add("W");     // wild
        if (chooseBotCard(botHand2, "R7", "") == 0) passed++; else fail("bot prefers skip over number");

        ArrayList<String> botHand3 = new ArrayList<String>();
        botHand3.add("R5");    // number - should be chosen (no skip or draw two)
        botHand3.add("W");     // wild
        if (chooseBotCard(botHand3, "R7", "") == 0) passed++; else fail("bot prefers number over wild");

        ArrayList<String> botHand4 = new ArrayList<String>();
        botHand4.add("G3");    // illegal
        botHand4.add("W");     // wild - only legal option
        if (chooseBotCard(botHand4, "R7", "") == 1) passed++; else fail("bot falls back to wild");

        ArrayList<String> botHand5 = new ArrayList<String>();
        botHand5.add("G3");    // illegal
        botHand5.add("B7");    // illegal
        if (chooseBotCard(botHand5, "R5", "") == -1) passed++; else fail("bot returns -1 when no legal card");

        // now what color bot chooses: picks the color it has most of
        ArrayList<String> colorHand = new ArrayList<String>();
        colorHand.add("R1"); colorHand.add("R2"); colorHand.add("R3"); // 3 reds
        colorHand.add("G1"); colorHand.add("G2");                       // 2 greens
        if (chooseBotColor(colorHand).equals("R")) passed++; else fail("bot color: most common");

        // now I add quirk tests as asked to test quirks.
        // quirk: illegal index input (not code) causes penalty + turn loss
        // test this through isLegal indirectly - the game loop penalize, a valid index that points to an illegal card.
        // test the scoring math directly, if opponent holds R5(5) + GS(20) + W(50) = 75 points
        ArrayList<String> loserHand = new ArrayList<String>();
        loserHand.add("R5");
        loserHand.add("GS");
        loserHand.add("W");
        int totalPoints = 0;
        for (String c : loserHand) totalPoints += points(c);
        if (totalPoints == 75) passed++; else fail("scoring: 5+20+50=75");

        // quirk: deck fallback - if both deck and discard are empty, returns "W"
        deck.clear(); discard.clear();
        String fallback = draw();
        if (fallback.equals("W")) passed++; else fail("draw fallback returns W");
        // restore state
        deck.clear(); discard.clear();

        // quirk: join() format is "index:card index:card"
        ArrayList<String> joinTest = new ArrayList<String>();
        joinTest.add("R5"); joinTest.add("W");
        if (GameView.join(joinTest).equals("0:R5 1:W")) passed++; else fail("join format");


        ArrayList<String> tieHand = new ArrayList<String>();
        tieHand.add("B1"); tieHand.add("Y1"); // tie - R wins per the if-chain
        if (chooseBotColor(tieHand).equals("Y")) passed++; else fail("bot color tie: Y wins if equal");


        ArrayList<String> h = new ArrayList<String>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        if (chooseBotCard(h, "R9", "") == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<String>();
        h2.add("B1");
        h2.add("B2");
        h2.add("R3");
        if (chooseBotColor(h2).equals("B")) passed++; else fail("bot color");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
