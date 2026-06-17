import java.util.logging.Logger;
import persistence.Database;
import persistence.GameRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Main {
    static final Logger logger = Logger.getLogger(Main.class.getName());
    static ArrayList<Player> players = new ArrayList<Player>();
    static ArrayList<String> deck = new ArrayList<String>();
    static ArrayList<String> discard = new ArrayList<String>();
    static int[] scores;
    static boolean quiet = false;
    static Random random = new Random();
    static Deck gameDeck;
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
            } else if (args[i].equals("--stats")) {
                Database.getFactory();
                showStats();
                return;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: java -jar uno-cli.jar [--bots N] [--games N] [--human] [--quiet] [--seed N] [--stats]");
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

        logger.info("Starting UNO with " + games + " game(s) and " + players.size() + " players.");
        Database.getFactory();

        for (int g = 1; g <= games; g++) {
            view.showGameHeader(g);
            playMultiRoundGame();
        }

        logger.info("All games finished.");
        view.showFinalScores(players, scores);
    }

    static void setupPlayers(int bots, boolean human) {
        players.clear();
        if (human) players.add(new Player("You", true));
        for (int i = 1; i <= bots; i++) players.add(new Player("Bot" + i, false));
        scores = new int[players.size()];
    }

    static void playMultiRoundGame() {
        java.sql.Timestamp startedAt = new java.sql.Timestamp(System.currentTimeMillis());
        GameEngine engine = new GameEngine(players, gameDeck, random, view);
        int totalRounds = 0;

        while (!engine.isGameOver()) {
            logger.info("Starting new round. Scores: " + java.util.Arrays.toString(engine.getScores()));
            engine.setupRound();

            int guard = 0;
            while (guard < 3000) {
                guard++;
                if (engine.takeTurn()) break;
            }
            totalRounds++;
            scores = engine.getScores();
            view.showRoundScores(players, scores);
        }

        int winnerIdx = engine.getWinnerIndex();
        String winner = players.get(winnerIdx).name;
        logger.info("Game over! Winner: " + winner + " with " + scores[winnerIdx] + " points.");
        System.out.println("\n" + winner + " wins the game with " + scores[winnerIdx] + " points!");
        saveGameResult(startedAt, totalRounds, winner, scores);
    }

    static void saveGameResult(java.sql.Timestamp startedAt, int rounds, String winner, int[] finalScores) {
        try {
            GameRepository repo = new GameRepository();
            java.sql.Timestamp endedAt = new java.sql.Timestamp(System.currentTimeMillis());
            int gameId = repo.saveGame(startedAt, endedAt, rounds, winner);
            for (int i = 0; i < players.size(); i++) {
                repo.saveScore(gameId, players.get(i).name, finalScores[i]);
            }
            logger.info("Game result saved. Winner: " + winner);
        } catch (Exception e) {
            logger.warning("Could not save game result: " + e.getMessage());
        }
    }

    static void showStats() {
        GameRepository repo = new GameRepository();
        System.out.println("\n=== Recent Games ===");
        for (java.util.Map<String, Object> row : repo.getRecentGames()) {
            System.out.println("Game #" + row.get("ID") +
                " | Winner: " + row.get("WINNER") +
                " | Rounds: " + row.get("ROUNDS") +
                " | Ended: " + row.get("ENDED_AT"));
        }
        System.out.println("\n=== Player Win Count ===");
        for (java.util.Map<String, Object> row : repo.getPlayerWinCount()) {
            System.out.println(row.get("WINNER") + ": " + row.get("WINS") + " wins");
        }
        System.out.println("\n=== Highest Scores ===");
        for (java.util.Map<String, Object> row : repo.getHighestScores()) {
            System.out.println(row.get("PLAYER") + ": " + row.get("TOTAL_SCORE") + " points");
        }
    }

    // kept for selfTest only
    static String draw() {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) return "W";
        return deck.remove(0);
    }

    static boolean isLegal(String card, String up, String call) { return Card.isLegal(card, up, call); }
    static String color(String card) { return Card.color(card); }
    static String rank(String card) { return Card.rank(card); }
    static int number(String card) { return Card.number(card); }
    static int points(String card) { return Card.points(card); }

    static void selfTest() {
        int passed = 0;
        if (Card.color("R5").equals("R")) passed++; else fail("color R5");
        if (Card.rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (points("W4") == 50) passed++; else fail("wild points");
        if (isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");
        if (Card.color("Y5").equals("Y")) passed++; else fail("color Y5");
        if (Card.color("G+2").equals("G")) passed++; else fail("color G+2");
        if (Card.color("BS").equals("B")) passed++; else fail("color BS");
        if (Card.color("W").equals("")) passed++; else fail("color W is empty");
        if (Card.color("W4").equals("")) passed++; else fail("color W4 is empty");
        if (Card.rank("RS").equals("SKIP")) passed++; else fail("rank RS");
        if (Card.rank("BR").equals("REVERSE")) passed++; else fail("rank BR");
        if (Card.rank("R+2").equals("DRAW_TWO")) passed++; else fail("rank R+2");
        if (Card.rank("W").equals("WILD")) passed++; else fail("rank W");
        if (Card.rank("W4").equals("WILD_DRAW_FOUR")) passed++; else fail("rank W4");
        if (Card.rank("R0").equals("NUMBER")) passed++; else fail("rank R0");
        if (Card.rank("G7").equals("NUMBER")) passed++; else fail("rank G7");
        if (Card.number("R0") == 0) passed++; else fail("number R0");
        if (Card.number("G7") == 7) passed++; else fail("number G7");
        if (Card.number("B9") == 9) passed++; else fail("number B9");
        if (Card.number("RS") == -1) passed++; else fail("number of non-number card");
        if (Card.points("R5") == 5) passed++; else fail("points R5");
        if (Card.points("G0") == 0) passed++; else fail("points G0");
        if (Card.points("BS") == 20) passed++; else fail("points BS");
        if (Card.points("YR") == 20) passed++; else fail("points YR");
        if (Card.points("R+2") == 20) passed++; else fail("points R+2");
        if (Card.points("W") == 50) passed++; else fail("points W");
        if (isLegal("R9","R5","")) passed++; else fail("legal:same color");
        if (isLegal("RS","R5","")) passed++; else fail("legal:same color action");
        if (!isLegal("G5","R7","")) passed++; else fail("illegal: different color different number");
        if (isLegal("G5","R5","")) passed++; else fail("legal:same number");
        if (isLegal("B5","Y5","")) passed++; else fail("legal:same number different color");
        if (!isLegal("R4","G7","")) passed++; else fail("illegal:different number different color");
        if (isLegal("GS", "RS", "")) passed++; else fail("legal: skip on skip");
        if (isLegal("BR", "YR", "")) passed++; else fail("legal: reverse on reverse");
        if (isLegal("Y+2", "R+2", "")) passed++; else fail("legal: draw two on draw two");
        if (isLegal("W", "R5", "")) passed++; else fail("legal: wild always");
        if (isLegal("W4", "G9", "")) passed++; else fail("legal: W4 always");
        if (isLegal("W", "W4", "")) passed++; else fail("legal: wild on W4");
        if (isLegal("G3", "W", "G")) passed++; else fail("legal: called color match");
        if (isLegal("GS", "W4", "G")) passed++; else fail("legal: action on called color");
        if (!isLegal("R3", "W", "G")) passed++; else fail("illegal: wrong color on called color");

        Player bot1 = new Player("test", false);
        bot1.hand.add("R+2"); bot1.hand.add("RS"); bot1.hand.add("R5"); bot1.hand.add("W");
        if (bot1.chooseCard("R7", "") == 0) passed++; else fail("bot prefers draw two");

        Player bot2 = new Player("test", false);
        bot2.hand.add("RS"); bot2.hand.add("R5"); bot2.hand.add("W");
        if (bot2.chooseCard("R7", "") == 0) passed++; else fail("bot prefers skip over number");

        Player bot3 = new Player("test", false);
        bot3.hand.add("R5"); bot3.hand.add("W");
        if (bot3.chooseCard("R7", "") == 0) passed++; else fail("bot prefers number over wild");

        Player bot4 = new Player("test", false);
        bot4.hand.add("G3"); bot4.hand.add("W");
        if (bot4.chooseCard("R7", "") == 1) passed++; else fail("bot falls back to wild");

        Player bot5 = new Player("test", false);
        bot5.hand.add("G3"); bot5.hand.add("B7");
        if (bot5.chooseCard("R5", "") == -1) passed++; else fail("bot returns -1 when no legal card");

        Player colorBot1 = new Player("test", false);
        colorBot1.hand.add("R1"); colorBot1.hand.add("R2"); colorBot1.hand.add("R3");
        colorBot1.hand.add("G1"); colorBot1.hand.add("G2");
        if (colorBot1.chooseColor().equals("R")) passed++; else fail("bot color: most common");

        ArrayList<String> loserHand = new ArrayList<String>();
        loserHand.add("R5"); loserHand.add("GS"); loserHand.add("W");
        int totalPoints = 0;
        for (String c : loserHand) totalPoints += points(c);
        if (totalPoints == 75) passed++; else fail("scoring: 5+20+50=75");

        deck.clear(); discard.clear();
        String fallback = draw();
        if (fallback.equals("W")) passed++; else fail("draw fallback returns W");
        deck.clear(); discard.clear();

        ArrayList<String> joinTest = new ArrayList<String>();
        joinTest.add("R5"); joinTest.add("W");
        if (GameView.join(joinTest).equals("0:R5 1:W")) passed++; else fail("join format");

        Player tieBot = new Player("test", false);
        tieBot.hand.add("B1"); tieBot.hand.add("Y1");
        if (tieBot.chooseColor().equals("Y")) passed++; else fail("bot color tie: Y wins if equal");

        Player normalBot = new Player("test", false);
        normalBot.hand.add("B3"); normalBot.hand.add("R4"); normalBot.hand.add("W");
        if (normalBot.chooseCard("R9", "") == 1) passed++; else fail("bot normal before wild");

        Player colorBot2 = new Player("test", false);
        colorBot2.hand.add("B1"); colorBot2.hand.add("B2"); colorBot2.hand.add("R3");
        if (colorBot2.chooseColor().equals("B")) passed++; else fail("bot color");

        Deck testDeck = new Deck(new Random(42));
        if (testDeck.draw().equals("W")) passed++; else fail("Deck.draw fallback returns W");

        Player wildBot = new Player("test", false);
        wildBot.hand.add("G3"); wildBot.hand.add("W");
        if (wildBot.chooseCard("R5", "") == 1) passed++;
        else fail("bot wild quirk: no isLegal guard on wild");

        // GameEngine: applyEffect skip skips the next player
        {
            ArrayList<Player> ps = new ArrayList<Player>();
            ps.add(new Player("A", false)); ps.add(new Player("B", false)); ps.add(new Player("C", false));
            GameEngine ge = new GameEngine(ps, new Deck(new Random(1)), new Random(1), new GameView(true));
            ge.currentPlayer = 0; ge.direction = 1;
            ge.applyEffect("RS");
            if (ge.currentPlayer == 2) passed++; else fail("applyEffect skip: lands on player after next");
        }

        // GameEngine: applyEffect reverse flips direction in 3-player game
        {
            ArrayList<Player> ps = new ArrayList<Player>();
            ps.add(new Player("A", false)); ps.add(new Player("B", false)); ps.add(new Player("C", false));
            GameEngine ge = new GameEngine(ps, new Deck(new Random(1)), new Random(1), new GameView(true));
            ge.currentPlayer = 0; ge.direction = 1;
            ge.applyEffect("RR");
            if (ge.direction == -1 && ge.currentPlayer == 2) passed++; else fail("applyEffect reverse 3p: direction -1, goes to last player");
        }

        // GameEngine: applyEffect reverse with 2 players acts as skip
        {
            ArrayList<Player> ps = new ArrayList<Player>();
            ps.add(new Player("A", false)); ps.add(new Player("B", false));
            GameEngine ge = new GameEngine(ps, new Deck(new Random(1)), new Random(1), new GameView(true));
            ge.currentPlayer = 0; ge.direction = 1;
            ge.applyEffect("RR");
            if (ge.direction == -1 && ge.currentPlayer == 0) passed++; else fail("applyEffect reverse 2p: acts as skip");
        }

        // GameEngine: applyEffect draw_two gives next player 2 cards and skips them
        {
            ArrayList<Player> ps = new ArrayList<Player>();
            ps.add(new Player("A", false)); ps.add(new Player("B", false));
            Deck d = new Deck(new Random(1)); d.build();
            GameEngine ge = new GameEngine(ps, d, new Random(1), new GameView(true));
            ge.currentPlayer = 0; ge.direction = 1;
            ge.applyEffect("R+2");
            if (ps.get(1).hand.size() == 2 && ge.currentPlayer == 0) passed++; else fail("applyEffect draw_two: B gets 2 cards, turn skips back to A");
        }

        // GameEngine: applyEffect wild_draw_four gives next player 4 cards and skips them
        {
            ArrayList<Player> ps = new ArrayList<Player>();
            ps.add(new Player("A", false)); ps.add(new Player("B", false));
            Deck d = new Deck(new Random(1)); d.build();
            GameEngine ge = new GameEngine(ps, d, new Random(1), new GameView(true));
            ge.currentPlayer = 0; ge.direction = 1;
            ge.applyEffect("W4");
            if (ps.get(1).hand.size() == 4 && ge.currentPlayer == 0) passed++; else fail("applyEffect wild_draw_four: B gets 4 cards, turn skips back to A");
        }

        // GameEngine: countPoints sums all hands except the winner
        {
            ArrayList<Player> ps = new ArrayList<Player>();
            ps.add(new Player("A", false)); ps.add(new Player("B", false)); ps.add(new Player("C", false));
            ps.get(1).hand.add("R5"); ps.get(2).hand.add("GS");
            GameEngine ge = new GameEngine(ps, new Deck(new Random(1)), new Random(1), new GameView(true));
            ge.currentPlayer = 0;
            if (ge.countPoints() == 25) passed++; else fail("countPoints: 5+20=25 from losers");
        }

        // GameView: askColor with valid input
        {
            GameView tv = new GameView(true, new java.util.Scanner("R\n"));
            if (tv.askColor().equals("R")) passed++; else fail("askColor returns R");
        }

        // GameView: askColor retries on bad input then returns valid
        {
            GameView tv = new GameView(true, new java.util.Scanner("X\nY\n"));
            if (tv.askColor().equals("Y")) passed++; else fail("askColor skips bad input and returns Y");
        }

        // GameView: askYesNo returns true for y
        {
            GameView tv = new GameView(true, new java.util.Scanner("y\n"));
            if (tv.askYesNo()) passed++; else fail("askYesNo y returns true");
        }

        // GameView: askYesNo returns false for n
        {
            GameView tv = new GameView(true, new java.util.Scanner("n\n"));
            if (!tv.askYesNo()) passed++; else fail("askYesNo n returns false");
        }

        // GameView: askHuman DRAW returns -1
        {
            ArrayList<String> h = new ArrayList<String>();
            h.add("R5"); h.add("W");
            GameView tv = new GameView(true, new java.util.Scanner("DRAW\n"));
            if (tv.askHuman(h, "R9", "") == -1) passed++; else fail("askHuman DRAW returns -1");
        }

        // GameView: askHuman legal card code returns its index
        {
            ArrayList<String> h = new ArrayList<String>();
            h.add("R5"); h.add("W");
            GameView tv = new GameView(true, new java.util.Scanner("R5\n"));
            if (tv.askHuman(h, "R9", "") == 0) passed++; else fail("askHuman legal code returns index");
        }

        // GameView: askHuman index input returns that index
        {
            ArrayList<String> h = new ArrayList<String>();
            h.add("R5"); h.add("W");
            GameView tv = new GameView(true, new java.util.Scanner("1\n"));
            if (tv.askHuman(h, "R9", "") == 1) passed++; else fail("askHuman index input returns index");
        }

        // safety limit characterization: scores stay 0 when no winner within guard
        {
            ArrayList<Player> ps = new ArrayList<Player>();
            ps.add(new Player("A", false)); ps.add(new Player("B", false));
            Deck d = new Deck(new Random(99));
            GameEngine ge = new GameEngine(ps, d, new Random(99), new GameView(true));
            ge.setupRound();
            int guard = 0; boolean won = false;
            while (guard < 5) { guard++; if (ge.takeTurn()) { won = true; break; } }
            if (!won) {
                int[] sc = ge.getScores();
                if (sc[0] == 0 && sc[1] == 0) passed++; else fail("safety limit: scores stay 0 when no winner");
            } else { passed++; }
        }

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}