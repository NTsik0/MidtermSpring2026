package persistence;

import java.util.List;
import java.util.Map;

public interface GameMapper {
    void insertGame(GameRecord game);
    void upsertPlayer(String name);
    void insertScore(ScoreRecord score);
    List<Map<String, Object>> recentGames();
    List<Map<String, Object>> playerWinCount();
    List<Map<String, Object>> highestScores();
}