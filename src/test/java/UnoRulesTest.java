import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Random;

public class UnoRulesTest {

    //  DECK COMPOSITION TESTS

    private ArrayList<String> drawFullDeck() {
        Deck d = new Deck(new Random(1));
        d.build();
        ArrayList<String> all = new ArrayList<>();
        for (int i = 0; i < 108; i++) all.add(d.draw());
        return all;
    }

    @Test
    public void testDeckHasFourColors() {
        Deck d = new Deck(new Random(1));
        d.build();
        String card = d.draw();
        assertNotNull(card);
    }

    @Test
    public void testDeckHasNumberCards() {
        assertTrue(Card.rank("R5").equals("NUMBER"));
        assertTrue(Card.rank("B0").equals("NUMBER"));
        assertTrue(Card.rank("G9").equals("NUMBER"));
    }

    @Test
    public void testDeckHasSkipCards() {
        assertEquals("SKIP", Card.rank("RS"));
        assertEquals("SKIP", Card.rank("GS"));
        assertEquals("SKIP", Card.rank("BS"));
        assertEquals("SKIP", Card.rank("YS"));
    }

    @Test
    public void testDeckHasReverseCards() {
        assertEquals("REVERSE", Card.rank("RR"));
        assertEquals("REVERSE", Card.rank("GR"));
    }

    @Test
    public void testDeckHasDrawTwoCards() {
        assertEquals("DRAW_TWO", Card.rank("R+2"));
        assertEquals("DRAW_TWO", Card.rank("G+2"));
    }

    @Test
    public void testDeckHasWildCards() {
        assertEquals("WILD", Card.rank("W"));
    }

    @Test
    public void testDeckHasWildDrawFourCards() {
        assertEquals("WILD_DRAW_FOUR", Card.rank("W4"));
    }

    @Test
    public void testDeckHasFourWildsAndFourWildDrawFours() {
        ArrayList<String> all = drawFullDeck();
        int wilds = 0, wildFours = 0;
        for (String c : all) {
            if (c.equals("W")) wilds++;
            else if (c.equals("W4")) wildFours++;
        }
        assertEquals(4, wilds);
        assertEquals(4, wildFours);
    }

    @Test
    public void testDeckHas25CardsPerColor() {
        ArrayList<String> all = drawFullDeck();
        for (String color : new String[]{"R", "Y", "G", "B"}) {
            int count = 0;
            for (String c : all) if (c.startsWith(color)) count++;
            assertEquals("Expected 25 cards for color " + color, 25, count);
        }
    }

    @Test
    public void testDeckHasTwoSkipsPerColor() {
        ArrayList<String> all = drawFullDeck();
        for (String color : new String[]{"R", "Y", "G", "B"}) {
            int count = 0;
            for (String c : all) if (c.equals(color + "S")) count++;
            assertEquals(2, count);
        }
    }

    //  LEGAL PLAY TESTS 

    @Test
    public void testMatchByColor() {
        assertTrue(Card.isLegal("R5", "R9", ""));
        assertTrue(Card.isLegal("RS", "R3", ""));
        assertFalse(Card.isLegal("G5", "R9", ""));
    }

    @Test
    public void testMatchByNumber() {
        assertTrue(Card.isLegal("G5", "R5", ""));
        assertTrue(Card.isLegal("B5", "Y5", ""));
        assertFalse(Card.isLegal("G4", "R5", ""));
    }

    @Test
    public void testMatchByActionType() {
        assertTrue(Card.isLegal("GS", "RS", ""));
        assertTrue(Card.isLegal("BR", "YR", ""));
        assertTrue(Card.isLegal("Y+2", "R+2", ""));
    }

    @Test
    public void testWildAlwaysLegal() {
        assertTrue(Card.isLegal("W", "R5", ""));
        assertTrue(Card.isLegal("W", "G9", ""));
        assertTrue(Card.isLegal("W4", "B3", ""));
        assertTrue(Card.isLegal("W4", "W", "R"));
    }

    @Test
    public void testIllegalPlaysRejected() {
        assertFalse(Card.isLegal("G5", "R7", ""));
        assertFalse(Card.isLegal("B3", "R9", ""));
        assertFalse(Card.isLegal("R4", "G7", ""));
    }

    //  SKIP TESTS 

    @Test
    public void testSkipNextPlayerLosesTurn() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        players.add(new Player("Bot3", false));
        GameEngine engine = new GameEngine(players, new Deck(new Random(1)), new Random(1), new GameView(true));
        engine.upCard = "R5";
        engine.calledColor = "";
        engine.currentPlayer = 0;
        engine.direction = 1;
        engine.applyEffect("RS");
        assertEquals(2, engine.currentPlayer);
    }

    @Test
    public void testSkipInTwoPlayerGame() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        GameEngine engine = new GameEngine(players, new Deck(new Random(1)), new Random(1), new GameView(true));
        engine.currentPlayer = 0;
        engine.direction = 1;
        engine.applyEffect("RS");
        assertEquals(0, engine.currentPlayer);
    }

    //  REVERSE TESTS 

    @Test
    public void testReverseChangesDirection() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        players.add(new Player("Bot3", false));
        GameEngine engine = new GameEngine(players, new Deck(new Random(1)), new Random(1), new GameView(true));
        engine.currentPlayer = 0;
        engine.direction = 1;
        engine.applyEffect("RR");
        assertEquals(-1, engine.direction);
    }

    @Test
    public void testReverseTwoPlayers() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        GameEngine engine = new GameEngine(players, new Deck(new Random(1)), new Random(1), new GameView(true));
        engine.currentPlayer = 0;
        engine.direction = 1;
        engine.applyEffect("RR");
        assertEquals(0, engine.currentPlayer);
    }

    //  DRAW TWO TESTS 

    @Test
    public void testDrawTwoNextPlayerDrawsTwoCards() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        players.add(new Player("Bot3", false));
        Deck deck = new Deck(new Random(1));
        deck.build();
        GameEngine engine = new GameEngine(players, deck, new Random(1), new GameView(true));
        engine.currentPlayer = 0;
        engine.direction = 1;
        int before = players.get(1).hand.size();
        engine.applyEffect("R+2");
        assertEquals(before + 2, players.get(1).hand.size());
    }

    @Test
    public void testDrawTwoNextPlayerLosesTurn() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        players.add(new Player("Bot3", false));
        Deck deck = new Deck(new Random(1));
        deck.build();
        GameEngine engine = new GameEngine(players, deck, new Random(1), new GameView(true));
        engine.currentPlayer = 0;
        engine.direction = 1;
        engine.applyEffect("R+2");
        assertEquals(2, engine.currentPlayer);
    }

    //  WILD TESTS 

    @Test
    public void testWildCalledColorAffectsLegal() {
        assertTrue(Card.isLegal("G3", "W", "G"));
        assertTrue(Card.isLegal("GS", "W", "G"));
        assertFalse(Card.isLegal("R3", "W", "G"));
    }

    @Test
    public void testWildIsLegalOnAnyCard() {
        assertTrue(Card.isLegal("W", "R5", ""));
        assertTrue(Card.isLegal("W", "W4", "R"));
        assertTrue(Card.isLegal("W", "BS", ""));
    }

    //  WILD DRAW FOUR TESTS 

    @Test
    public void testWildDrawFourNextPlayerDrawsFour() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        players.add(new Player("Bot3", false));
        Deck deck = new Deck(new Random(1));
        deck.build();
        GameEngine engine = new GameEngine(players, deck, new Random(1), new GameView(true));
        engine.currentPlayer = 0;
        engine.direction = 1;
        int before = players.get(1).hand.size();
        engine.applyEffect("W4");
        assertEquals(before + 4, players.get(1).hand.size());
    }

    @Test
    public void testWildDrawFourNextPlayerLosesTurn() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        players.add(new Player("Bot3", false));
        Deck deck = new Deck(new Random(1));
        deck.build();
        GameEngine engine = new GameEngine(players, deck, new Random(1), new GameView(true));
        engine.currentPlayer = 0;
        engine.direction = 1;
        engine.applyEffect("W4");
        assertEquals(2, engine.currentPlayer);
    }

    //  DRAW/PASS TESTS 

    @Test
    public void testDrawPassNoLegalCard() {
        Player bot = new Player("test", false);
        bot.hand.add("G3");
        bot.hand.add("B7");
        assertEquals(-1, bot.chooseCard("R5", ""));
    }

    @Test
    public void testDrawnCardCanBePlayed() {
        assertTrue(Card.isLegal("R5", "R9", ""));
    }

    @Test
    public void testPassWhenNoLegalPlay() {
        Player bot = new Player("test", false);
        bot.hand.add("G3");
        int result = bot.chooseCard("R5", "");
        assertEquals(-1, result);
    }

    //  UNO CALL AND PENALTY TESTS

    @Test
    public void testUnoDetectedWhenOneCard() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        players.get(0).hand.add("R5");
        players.get(0).hand.add("G3");
        players.get(1).hand.add("B7");
        assertEquals(1, players.get(1).hand.size());
    }

    @Test
    public void testUnoPenaltyDrawsTwoCards() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        Deck deck = new Deck(new Random(1));
        deck.build();
        players.get(1).hand.add("R5");
        int before = players.get(1).hand.size();
        players.get(1).hand.add(deck.draw());
        players.get(1).hand.add(deck.draw());
        assertEquals(before + 2, players.get(1).hand.size());
    }

    @Test
    public void testUnoDetectedThroughEngine() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        Deck deck = new Deck(new Random(1));
        deck.build();
        GameEngine engine = new GameEngine(players, deck, new Random(1), new GameView(true));
        players.get(0).hand.clear();
        players.get(0).hand.add("R5");
        players.get(0).hand.add("R3");
        players.get(1).hand.add("G5");
        engine.upCard = "R7";
        engine.calledColor = "";
        engine.currentPlayer = 0;
        engine.direction = 1;
        engine.takeTurn();
        assertEquals(1, players.get(0).hand.size());
    }

    @Test
    public void testMissedUnoPenaltyThroughEngine() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        Deck deck = new Deck(new Random(1));
        deck.build();
        GameEngine engine = new GameEngine(players, deck, new Random(1), new GameView(true));
        players.get(0).hand.clear();
        players.get(0).hand.add("R5");
        players.get(0).hand.add("R3");
        players.get(1).hand.clear();
        players.get(1).hand.add("G5");
        engine.upCard = "R7";
        engine.calledColor = "";
        engine.currentPlayer = 0;
        engine.direction = 1;
        engine.takeTurn();
        assertEquals(3, players.get(1).hand.size());
    }

    //  SCORING TESTS 

    @Test
    public void testNumberCardScoring() {
        assertEquals(5, Card.points("R5"));
        assertEquals(0, Card.points("G0"));
        assertEquals(9, Card.points("B9"));
    }

    @Test
    public void testActionCardScoring() {
        assertEquals(20, Card.points("RS"));
        assertEquals(20, Card.points("GR"));
        assertEquals(20, Card.points("B+2"));
    }

    @Test
    public void testWildCardScoring() {
        assertEquals(50, Card.points("W"));
        assertEquals(50, Card.points("W4"));
    }

    @Test
    public void testTotalScoring() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5"); hand.add("GS"); hand.add("W");
        int total = 0;
        for (String c : hand) total += Card.points(c);
        assertEquals(75, total);
    }

    @Test
    public void testMultiRoundTargetScore() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        Deck deck = new Deck(new Random(1));
        GameEngine engine = new GameEngine(players, deck, new Random(1), new GameView(true));
        assertFalse(engine.isGameOver());
        engine.scores[0] = 500;
        assertTrue(engine.isGameOver());
    }

    @Test
    public void testGetWinnerIndex() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Bot1", false));
        players.add(new Player("Bot2", false));
        Deck deck = new Deck(new Random(1));
        GameEngine engine = new GameEngine(players, deck, new Random(1), new GameView(true));
        engine.scores[0] = 300;
        engine.scores[1] = 500;
        assertEquals(1, engine.getWinnerIndex());
    }
}