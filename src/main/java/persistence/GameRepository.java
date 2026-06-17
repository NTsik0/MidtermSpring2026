package persistence;

import org.apache.ibatis.session.SqlSession;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class GameRepository {

    public int saveGame(Timestamp startedAt, Timestamp endedAt, int rounds, String winner) {
        try (SqlSession session = Database.openSession()) {
            GameMapper mapper = session.getMapper(GameMapper.class);
            GameRecord record = new GameRecord(startedAt, endedAt, rounds, winner);
            mapper.insertGame(record);
            return record.id;
        }
    }

    public void saveScore(int gameId, String player, int score) {
        try (SqlSession session = Database.openSession()) {
            GameMapper mapper = session.getMapper(GameMapper.class);
            mapper.upsertPlayer(player);
            mapper.insertScore(new ScoreRecord(gameId, player, score));
        }
    }

    public List<Map<String, Object>> getRecentGames() {
        try (SqlSession session = Database.openSession()) {
            return session.getMapper(GameMapper.class).recentGames();
        }
    }

    public List<Map<String, Object>> getPlayerWinCount() {
        try (SqlSession session = Database.openSession()) {
            return session.getMapper(GameMapper.class).playerWinCount();
        }
    }

    public List<Map<String, Object>> getHighestScores() {
        try (SqlSession session = Database.openSession()) {
            return session.getMapper(GameMapper.class).highestScores();
        }
    }
}
