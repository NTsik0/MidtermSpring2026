package persistence;

import java.sql.Timestamp;

public class GameRecord {
    public int id;
    public Timestamp startedAt;
    public Timestamp endedAt;
    public int rounds;
    public String winner;

    public GameRecord(Timestamp startedAt, Timestamp endedAt, int rounds, String winner) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.rounds = rounds;
        this.winner = winner;
    }
}
