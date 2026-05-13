import java.util.ArrayList;

public class GameView {
    private boolean quiet;

    public GameView(boolean quiet) {
        this.quiet = quiet;
    }

    public void showUpCard(String upCard, String calledColor) {
        if (quiet) return;
        System.out.println("\nUp card: " + upCard +
                (calledColor.equals("") ? "" : " called " + calledColor));
    }

    public void showHand(String playerName, ArrayList<String> hand) {
        if (quiet) return;
        System.out.println(playerName + " hand: " + join(hand));
    }

    public void showDraw(String playerName, String card) {
        if (quiet) return;
        System.out.println(playerName + " draws " + card);
    }

    public void showPlay(String playerName, String card) {
        if (quiet) return;
        System.out.println(playerName + " plays " + card);
    }

    public void showColorCall(String playerName, String color) {
        if (quiet) return;
        System.out.println(playerName + " calls " + color);
    }

    public void showUno(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " says UNO!");
    }

    public void showWin(String playerName, int points) {
        if (quiet) return;
        System.out.println(playerName + " wins and scores " + points);
    }

    public void showDrawTwo(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " draws two.");
    }

    public void showDrawFour(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " draws four.");
    }

    public void showPenalty(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " tried illegal card and draws a penalty card.");
    }

    public void showBadIndex(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " selected an invalid index and draws a penalty card.");
    }

    public void showSafetyLimit() {
        if (quiet) return;
        System.out.println("Game stopped at safety limit.");
    }

    public void showGameHeader(int gameNumber) {
        if (quiet) return;
        System.out.println("\n=== Game " + gameNumber + " ===");
    }

    public void showFinalScores(ArrayList<Player> players, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < players.size(); i++) {
            System.out.println(players.get(i).name + ": " + scores[i]);
        }
    }

    private String join(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) out += " ";
        }
        return out;
    }
}