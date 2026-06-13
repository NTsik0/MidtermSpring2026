import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;

public class UnoTest {

    @Test
    public void testColorR5() { assertEquals("R", Card.color("R5")); }

    @Test
    public void testColorY5() { assertEquals("Y", Card.color("Y5")); }

    @Test
    public void testColorGPlus2() { assertEquals("G", Card.color("G+2")); }

    @Test
    public void testColorBS() { assertEquals("B", Card.color("BS")); }

    @Test
    public void testColorWild() { assertEquals("", Card.color("W")); }

    @Test
    public void testColorWild4() { assertEquals("", Card.color("W4")); }

    @Test
    public void testRankSkip() { assertEquals("SKIP", Card.rank("RS")); }

    @Test
    public void testRankReverse() { assertEquals("REVERSE", Card.rank("BR")); }

    @Test
    public void testRankDrawTwo() { assertEquals("DRAW_TWO", Card.rank("R+2")); }

    @Test
    public void testRankWild() { assertEquals("WILD", Card.rank("W")); }

    @Test
    public void testRankWild4() { assertEquals("WILD_DRAW_FOUR", Card.rank("W4")); }

    @Test
    public void testRankNumber0() { assertEquals("NUMBER", Card.rank("R0")); }

    @Test
    public void testRankNumber7() { assertEquals("NUMBER", Card.rank("G7")); }

    @Test
    public void testNumberR0() { assertEquals(0, Card.number("R0")); }

    @Test
    public void testNumberG7() { assertEquals(7, Card.number("G7")); }

    @Test
    public void testNumberB9() { assertEquals(9, Card.number("B9")); }

    @Test
    public void testNumberNonNumber() { assertEquals(-1, Card.number("RS")); }

    @Test
    public void testPointsNumber() { assertEquals(5, Card.points("R5")); }

    @Test
    public void testPointsZero() { assertEquals(0, Card.points("G0")); }

    @Test
    public void testPointsSkip() { assertEquals(20, Card.points("BS")); }

    @Test
    public void testPointsReverse() { assertEquals(20, Card.points("YR")); }

    @Test
    public void testPointsDrawTwo() { assertEquals(20, Card.points("R+2")); }

    @Test
    public void testPointsWild() { assertEquals(50, Card.points("W")); }

    @Test
    public void testPointsWild4() { assertEquals(50, Card.points("W4")); }

    @Test
    public void testLegalSameColor() { assertTrue(Card.isLegal("R2", "R9", "")); }

    @Test
    public void testLegalSameNumber() { assertTrue(Card.isLegal("G9", "R9", "")); }

    @Test
    public void testLegalCalledColor() { assertTrue(Card.isLegal("B3", "W", "B")); }

    @Test
    public void testIllegalMismatch() { assertFalse(Card.isLegal("B3", "R9", "")); }

    @Test
    public void testLegalSameColorAction() { assertTrue(Card.isLegal("RS", "R5", "")); }

    @Test
    public void testIllegalDifferentAll() { assertFalse(Card.isLegal("G5", "R7", "")); }

    @Test
    public void testLegalSameNumberDifferentColor() { assertTrue(Card.isLegal("B5", "Y5", "")); }

    @Test
    public void testLegalSkipOnSkip() { assertTrue(Card.isLegal("GS", "RS", "")); }

    @Test
    public void testLegalReverseOnReverse() { assertTrue(Card.isLegal("BR", "YR", "")); }

    @Test
    public void testLegalDrawTwoOnDrawTwo() { assertTrue(Card.isLegal("Y+2", "R+2", "")); }

    @Test
    public void testLegalWildAlways() { assertTrue(Card.isLegal("W", "R5", "")); }

    @Test
    public void testLegalWild4Always() { assertTrue(Card.isLegal("W4", "G9", "")); }

    @Test
    public void testLegalCalledColorAction() { assertTrue(Card.isLegal("GS", "W4", "G")); }

    @Test
    public void testIllegalWrongCalledColor() { assertFalse(Card.isLegal("R3", "W", "G")); }

    @Test
    public void testBotPrefersDrawTwo() {
        Player bot = new Player("test", false);
        bot.hand.add("R+2"); bot.hand.add("RS"); bot.hand.add("R5"); bot.hand.add("W");
        assertEquals(0, bot.chooseCard("R7", ""));
    }

    @Test
    public void testBotPrefersSkipOverNumber() {
        Player bot = new Player("test", false);
        bot.hand.add("RS"); bot.hand.add("R5"); bot.hand.add("W");
        assertEquals(0, bot.chooseCard("R7", ""));
    }

    @Test
    public void testBotPrefersNumberOverWild() {
        Player bot = new Player("test", false);
        bot.hand.add("R5"); bot.hand.add("W");
        assertEquals(0, bot.chooseCard("R7", ""));
    }

    @Test
    public void testBotFallsBackToWild() {
        Player bot = new Player("test", false);
        bot.hand.add("G3"); bot.hand.add("W");
        assertEquals(1, bot.chooseCard("R7", ""));
    }

    @Test
    public void testBotReturnsMinusOneNoLegal() {
        Player bot = new Player("test", false);
        bot.hand.add("G3"); bot.hand.add("B7");
        assertEquals(-1, bot.chooseCard("R5", ""));
    }

    @Test
    public void testBotColorMostCommon() {
        Player bot = new Player("test", false);
        bot.hand.add("R1"); bot.hand.add("R2"); bot.hand.add("R3");
        bot.hand.add("G1"); bot.hand.add("G2");
        assertEquals("R", bot.chooseColor());
    }

    @Test
    public void testBotColorTie() {
        Player bot = new Player("test", false);
        bot.hand.add("B1"); bot.hand.add("Y1");
        assertEquals("Y", bot.chooseColor());
    }

    @Test
    public void testScoringMath() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5"); hand.add("GS"); hand.add("W");
        int total = 0;
        for (String c : hand) total += Card.points(c);
        assertEquals(75, total);
    }

    @Test
    public void testJoinFormat() {
        ArrayList<String> cards = new ArrayList<>();
        cards.add("R5"); cards.add("W");
        assertEquals("0:R5 1:W", GameView.join(cards));
    }

    @Test
    public void testDeckFallback() {
        Deck d = new Deck(new java.util.Random(42));
        assertEquals("W", d.draw());
    }
}