import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Deck {
    private ArrayList<String> drawPile = new ArrayList<String>();
    private ArrayList<String> discardPile = new ArrayList<String>();
    private Random random;

    public Deck(Random random) {
        this.random = random;
    }

    public void build() {
        drawPile.clear();
        discardPile.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (String c : colors) {
            drawPile.add(c + "0");
            for (int n = 1; n <= 9; n++) {
                drawPile.add(c + n);
                drawPile.add(c + n);
            }
            drawPile.add(c + "S"); drawPile.add(c + "S");
            drawPile.add(c + "R"); drawPile.add(c + "R");
            drawPile.add(c + "+2"); drawPile.add(c + "+2");
        }
        for (int i = 0; i < 4; i++) {
            drawPile.add("W");
            drawPile.add("W4");
        }
        Collections.shuffle(drawPile, random);
    }

    public String draw() {
        if (drawPile.isEmpty()) {
            drawPile.addAll(discardPile);
            discardPile.clear();
            Collections.shuffle(drawPile, random);
        }
        if (drawPile.isEmpty()) return "W";
        return drawPile.remove(0);
    }

    public void discard(String card) {
        discardPile.add(card);
    }
}