import java.util.ArrayList;

public class Player {
    public String name;
    public boolean human;
    public ArrayList<String> hand;

    public Player(String name, boolean human) {
        this.name = name;
        this.human = human;
        this.hand = new ArrayList<String>();
    }
    // by adding this two methods here I can move chooseBotCard() and chooseBotColor() from main into player.java as instance methods.
    public int chooseCard(String upCard, String calledColor) {
        for (int i = 0; i < hand.size(); i++) {
            if (Card.rank(hand.get(i)).equals("DRAW_TWO") && Card.isLegal(hand.get(i), upCard, calledColor)) return i;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (Card.rank(hand.get(i)).equals("SKIP") && Card.isLegal(hand.get(i), upCard, calledColor)) return i;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (Card.rank(hand.get(i)).equals("NUMBER") && Card.isLegal(hand.get(i), upCard, calledColor)) return i;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) return i;
        }
        return -1;
    }

    public String chooseColor() {
        int r = 0, y = 0, g = 0, b = 0;
        for (String c : hand) {
            if (Card.color(c).equals("R")) r++;
            else if (Card.color(c).equals("Y")) y++;
            else if (Card.color(c).equals("G")) g++;
            else if (Card.color(c).equals("B")) b++;
        }
        if (r >= y && r >= g && r >= b) return "R";
        else if (y >= r && y >= g && y >= b) return "Y";
        else if (g >= r && g >= y && g >= b) return "G";
        else return "B";
    }
}