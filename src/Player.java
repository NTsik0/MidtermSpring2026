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

    public void clearHand() { hand.clear(); }
    public void addCard(String card) { hand.add(card); }
    public void removeCard(int index) { hand.remove(index); }
    public int handSize() { return hand.size(); }
}