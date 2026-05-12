import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static ArrayList<String> playerNames = new ArrayList<String>();
    static ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    static ArrayList<ArrayList<String>> hands = new ArrayList<ArrayList<String>>();
    static ArrayList<String> deck = new ArrayList<String>();
    static ArrayList<String> discard = new ArrayList<String>();
    static int[] scores = new int[10];
    static int currentPlayer = 0;
    static int direction = 1;
    static String upCard = "";
    static String calledColor = "";
    static boolean quiet = false;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);

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
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
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
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(playerNames.get(i) + ": " + scores[i]);
        }
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<String>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<String>());
        }
    }

    static void playGame() {
        deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (int c = 0; c < colors.length; c++) {
            deck.add(colors[c] + "0");
            for (int n = 1; n <= 9; n++) {
                deck.add(colors[c] + n);
                deck.add(colors[c] + n);
            }
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "+2");
            deck.add(colors[c] + "+2");
        }
        for (int i = 0; i < 4; i++) {
            deck.add("W");
            deck.add("W4");
        }
        Collections.shuffle(deck, random);
        discard.clear();
        for (int i = 0; i < hands.size(); i++) {
            hands.get(i).clear();
        }
        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(draw());
            }
        }
        upCard = draw();
        while (upCard.startsWith("W")) {
            discard.add(upCard);
            upCard = draw();
        }
        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(playerNames.size());

        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = playerNames.get(currentPlayer);
            ArrayList<String> hand = hands.get(currentPlayer);

            if (!quiet) {
                System.out.println("\nUp card: " + upCard + (calledColor.equals("") ? "" : " called " + calledColor));
                System.out.println(name + " hand: " + join(hand));
            }

            int chosen = -1;
            if (humanPlayers.get(currentPlayer).booleanValue()) {
                chosen = askHuman(hand);
            } else {
                chosen = chooseBotCard(hand);
            }

            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);
                if (!quiet) {
                    System.out.println(name + " draws " + drawn);
                }
                if (isLegal(drawn, upCard, calledColor)) {
                    if (!humanPlayers.get(currentPlayer).booleanValue()) {
                        chosen = hand.size() - 1;
                    } else {
                        System.out.print("Play drawn card " + drawn + "? y/n: ");
                        String answer = scanner.nextLine();
                        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    if (!quiet) {
                        System.out.println(name + " selected an invalid index and draws a penalty card.");
                    }
                    hand.add(draw());
                    next();
                    continue;
                }

                String card = hand.get(chosen);
                boolean ok = false;
                String cardColor = color(card);
                String upColor = color(upCard);
                String cardRank = rank(card);
                String upRank = rank(upCard);

                if (card.startsWith("W")) {
                    ok = true;
                } else if (cardColor.equals(upColor)) {
                    ok = true;
                } else if (!calledColor.equals("") && cardColor.equals(calledColor)) {
                    ok = true;
                } else if (cardRank.equals(upRank) && !cardRank.equals("NUMBER")) {
                    ok = true;
                } else if (cardRank.equals("NUMBER") && upRank.equals("NUMBER") && number(card) == number(upCard)) {
                    ok = true;
                }

                if (!ok) {
                    if (!quiet) {
                        System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
                    }
                    hand.add(draw());
                    next();
                    continue;
                }

                hand.remove(chosen);
                discard.add(upCard);
                upCard = card;
                calledColor = "";
                if (!quiet) {
                    System.out.println(name + " plays " + card);
                }

                if (card.equals("W") || card.equals("W4")) {
                    if (humanPlayers.get(currentPlayer).booleanValue()) {
                        calledColor = askColor();
                    } else {
                        calledColor = chooseBotColor(hand);
                    }
                    if (!quiet) {
                        System.out.println(name + " calls " + calledColor);
                    }
                }

                if (hand.size() == 1 && !quiet) {
                    System.out.println(name + " says UNO!");
                }

                if (hand.size() == 0) {
                    int points = 0;
                    for (int i = 0; i < hands.size(); i++) {
                        if (i != currentPlayer) {
                            for (int j = 0; j < hands.get(i).size(); j++) {
                                points += points(hands.get(i).get(j));
                            }
                        }
                    }
                    scores[currentPlayer] += points;
                    if (!quiet) {
                        System.out.println(name + " wins and scores " + points);
                    }
                    return;
                }

                if (rank(card).equals("SKIP")) {
                    next();
                    next();
                } else if (rank(card).equals("REVERSE")) {
                    direction = direction * -1;
                    if (playerNames.size() == 2) {
                        next();
                        next();
                    } else {
                        next();
                    }
                } else if (rank(card).equals("DRAW_TWO")) {
                    next();
                    hands.get(currentPlayer).add(draw());
                    hands.get(currentPlayer).add(draw());
                    if (!quiet) {
                        System.out.println(playerNames.get(currentPlayer) + " draws two.");
                    }
                    next();
                } else if (rank(card).equals("WILD_DRAW_FOUR")) {
                    next();
                    for (int i = 0; i < 4; i++) {
                        hands.get(currentPlayer).add(draw());
                    }
                    if (!quiet) {
                        System.out.println(playerNames.get(currentPlayer) + " draws four.");
                    }
                    next();
                } else {
                    next();
                }
            } else {
                next();
            }
        }
        if (!quiet) {
            System.out.println("Game stopped at safety limit.");
        }
    }

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

    static int chooseBotCard(ArrayList<String> hand) {
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = false;
            if (card.startsWith("W")) ok = true;
            else if (color(card).equals(color(upCard))) ok = true;
            else if (!calledColor.equals("") && color(card).equals(calledColor)) ok = true;
            else if (rank(card).equals(rank(upCard)) && !rank(card).equals("NUMBER")) ok = true;
            else if (rank(card).equals("NUMBER") && rank(upCard).equals("NUMBER") && number(card) == number(upCard)) ok = true;
            if (rank(card).equals("DRAW_TWO") && ok) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = false;
            if (card.startsWith("W")) ok = true;
            else if (color(card).equals(color(upCard))) ok = true;
            else if (!calledColor.equals("") && color(card).equals(calledColor)) ok = true;
            else if (rank(card).equals(rank(upCard)) && !rank(card).equals("NUMBER")) ok = true;
            else if (rank(card).equals("NUMBER") && rank(upCard).equals("NUMBER") && number(card) == number(upCard)) ok = true;
            if (rank(card).equals("SKIP") && ok) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            boolean ok = false;
            if (card.startsWith("W")) ok = true;
            else if (color(card).equals(color(upCard))) ok = true;
            else if (!calledColor.equals("") && color(card).equals(calledColor)) ok = true;
            else if (rank(card).equals(rank(upCard)) && !rank(card).equals("NUMBER")) ok = true;
            else if (rank(card).equals("NUMBER") && rank(upCard).equals("NUMBER") && number(card) == number(upCard)) ok = true;
            if (rank(card).equals("NUMBER") && ok) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) {
                return i;
            }
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
            String c = color(hand.get(i));
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
        if (card.startsWith("W")) {
            return true;
        }
        if (color(card).equals(color(up))) {
            return true;
        }
        if (!call.equals("") && color(card).equals(call)) {
            return true;
        }
        if (rank(card).equals(rank(up)) && !rank(card).equals("NUMBER")) {
            return true;
        }
        if (rank(card).equals("NUMBER") && rank(up).equals("NUMBER") && number(card) == number(up)) {
            return true;
        }
        return false;
    }

    static String color(String card) {
        if (card.startsWith("R")) {
            return "R";
        }
        if (card.startsWith("Y")) {
            return "Y";
        }
        if (card.startsWith("G")) {
            return "G";
        }
        if (card.startsWith("B")) {
            return "B";
        }
        return "";
    }

    static String rank(String card) {
        if (card.equals("W")) {
            return "WILD";
        }
        if (card.equals("W4")) {
            return "WILD_DRAW_FOUR";
        }
        if (card.endsWith("S")) {
            return "SKIP";
        }
        if (card.endsWith("R")) {
            return "REVERSE";
        }
        if (card.endsWith("+2")) {
            return "DRAW_TWO";
        }
        return "NUMBER";
    }

    static int number(String card) {
        if (rank(card).equals("NUMBER")) {
            return Integer.parseInt(card.substring(1));
        }
        return -1;
    }

    static int points(String card) {
        String r = rank(card);
        if (r.equals("NUMBER")) {
            return number(card);
        }
        if (r.equals("SKIP") || r.equals("REVERSE") || r.equals("DRAW_TWO")) {
            return 20;
        }
        if (r.equals("WILD") || r.equals("WILD_DRAW_FOUR")) {
            return 50;
        }
        return 0;
    }

    static void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    static String join(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) {
                out += " ";
            }
        }
        return out;
    }

    static void selfTest() {
        int passed = 0;
        if (color("R5").equals("R")) passed++; else fail("color R5");
        if (rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (points("W4") == 50) passed++; else fail("wild points");
        if (isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");
        // here I am adding color() tests
        if (color("Y5").equals("Y")) passed++; else fail("color Y5");
        if (color("G+2").equals("G")) passed++; else fail("color G+2");
        if (color("BS").equals("B")) passed++; else fail("color BS");
        if (color("W").equals("")) passed++; else fail("color W is empty");
        if (color("W4").equals("")) passed++; else fail("color W4 is empty");
        // we should also add rank() tests
        if(rank("RS").equals("SKIP")) passed++; else fail("rank RS");
        if(rank("BR").equals("REVERSE")) passed++; else fail("rank BR");
        if(rank("R+2").equals("DRAW_TWO")) passed++; else fail("rank R+2");
        if(rank("W").equals("WILD")) passed++; else fail("rank W");
        if(rank("W4").equals("WILD_DRAW_FOUR")) passed++; else fail("rank W4");
        if(rank("R0").equals("NUMBER")) passed++; else fail("rank R0");
        if(rank("G7").equals("NUMBER")) passed++; else fail("rank G7");
        // now number() tests
        if(number("R0") == 0) passed++; else fail("number R0");
        if(number("G7") == 7) passed++; else fail("number G7");
        if(number("B9") == 9) passed++; else fail("number B9");
        if(number("RS") == -1) passed++; else fail("number of non-number card");
        // and point() tests
        if(points("R5") == 5) passed++; else fail("points R5");
        if(points("G0") == 0) passed++; else fail("points G0");
        if(points("BS") == 20) passed++; else fail("points BS");
        if(points("YR") == 20) passed++; else fail("points YR");
        if(points("R+2") == 20) passed++; else fail("points R+2");
        if(points("W") == 50) passed++; else fail("points W");
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
        upCard = "R7"; calledColor = "";
        if (chooseBotCard(botHand1) == 0) passed++; else fail("bot prefers draw two");

        ArrayList<String> botHand2 = new ArrayList<String>();
        botHand2.add("RS");    // skip - should be chosen (no draw two available)
        botHand2.add("R5");    // number
        botHand2.add("W");     // wild
        upCard = "R7"; calledColor = "";
        if (chooseBotCard(botHand2) == 0) passed++; else fail("bot prefers skip over number");

        ArrayList<String> botHand3 = new ArrayList<String>();
        botHand3.add("R5");    // number - should be chosen (no skip or draw two)
        botHand3.add("W");     // wild
        upCard = "R7"; calledColor = "";
        if (chooseBotCard(botHand3) == 0) passed++; else fail("bot prefers number over wild");

        ArrayList<String> botHand4 = new ArrayList<String>();
        botHand4.add("G3");    // illegal
        botHand4.add("W");     // wild - only legal option
        upCard = "R7"; calledColor = "";
        if (chooseBotCard(botHand4) == 1) passed++; else fail("bot falls back to wild");

        ArrayList<String> botHand5 = new ArrayList<String>();
        botHand5.add("G3");    // illegal
        botHand5.add("B7");    // illegal
        upCard = "R5"; calledColor = "";
        if (chooseBotCard(botHand5) == -1) passed++; else fail("bot returns -1 when no legal card");

        // now what color bot chooses: picks the color it has most one of all colors
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
        if (join(joinTest).equals("0:R5 1:W")) passed++; else fail("join format");


        ArrayList<String> tieHand = new ArrayList<String>();
        tieHand.add("B1"); tieHand.add("Y1"); // tie - R wins per the if-chain
        if (chooseBotColor(tieHand).equals("Y")) passed++; else fail("bot color tie: Y wins if equal");


        ArrayList<String> h = new ArrayList<String>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        upCard = "R9";
        calledColor = "";
        if (chooseBotCard(h) == 1) passed++; else fail("bot normal before wild");

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
