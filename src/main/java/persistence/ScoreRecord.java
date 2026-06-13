package persistence;

public class ScoreRecord {
    public int gameId;
    public String player;
    public int score;

    public ScoreRecord(int gameId, String player, int score) {
        this.gameId = gameId;
        this.player = player;
        this.score = score;
    }
}
