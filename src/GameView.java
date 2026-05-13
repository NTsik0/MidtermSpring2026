import java.util.ArrayList;
import java.util.Scanner;

public class GameView {
    private boolean quiet;
    // I am gonna move askHuman() and askColor() from Main into this GameView class, so I will add scanner and also update GameView constructor here
    private Scanner scanner;
    public GameView(boolean quiet) {
        this.quiet = quiet;
        this.scanner = new Scanner(System.in);
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
    // now added askHuman and askColor method to GameView, this class here
    public int askHuman(ArrayList<String> hand, String upCard, String calledColor) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) return -1;
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) return index;
            } catch (Exception ignored) {}
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (Card.isLegal(hand.get(i), upCard, calledColor)) return i;
                    System.out.println("That card is not legal.");
                }
            }
            System.out.println("Card not found.");
        }
    }

    public String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R") || input.equals("Y") || input.equals("G") || input.equals("B")) return input;
            System.out.println("Bad color.");
        }
    }
    // yes or no about play drawn card added
    public boolean askYesNo() {
        System.out.println("Play drawn card? y/n: ");
        return scanner.nextLine().equalsIgnoreCase("y");
    }
    // I had this private but now I update it to static String join....
     static String join(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) out += " ";
        }
        return out;
    }
}