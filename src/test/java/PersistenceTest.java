import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import persistence.Database;
import persistence.GameRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class PersistenceTest {

    @Before
    public void setUp() {
        Database.setEnvironment("test");
        Database.getFactory();
    }

    @Test
    public void testSaveAndRetrieveGame() {
        GameRepository repo = new GameRepository();
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Timestamp end = new Timestamp(System.currentTimeMillis() + 1000);
        int gameId = repo.saveGame(start, end, 10, "Bot1");
        assertTrue(gameId > 0);
    }

    @Test
    public void testSaveScore() {
        GameRepository repo = new GameRepository();
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Timestamp end = new Timestamp(System.currentTimeMillis() + 1000);
        int gameId = repo.saveGame(start, end, 5, "Bot2");
        repo.saveScore(gameId, "Bot2", 100);
        repo.saveScore(gameId, "Bot3", 50);

        List<Map<String, Object>> scores = repo.getHighestScores();
        assertFalse(scores.isEmpty());
    }

    @Test
    public void testRecentGames() {
        GameRepository repo = new GameRepository();
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Timestamp end = new Timestamp(System.currentTimeMillis() + 1000);
        repo.saveGame(start, end, 8, "Bot1");

        List<Map<String, Object>> games = repo.getRecentGames();
        assertFalse(games.isEmpty());
    }

    @Test
    public void testPlayerWinCount() {
        GameRepository repo = new GameRepository();
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Timestamp end = new Timestamp(System.currentTimeMillis() + 1000);
        repo.saveGame(start, end, 3, "Bot1");
        repo.saveGame(start, end, 4, "Bot1");

        List<Map<String, Object>> wins = repo.getPlayerWinCount();
        assertFalse(wins.isEmpty());
        assertEquals("Bot1", wins.get(0).get("WINNER"));
    }

    @Test
    public void testHighestScores() {
        GameRepository repo = new GameRepository();
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Timestamp end = new Timestamp(System.currentTimeMillis() + 1000);
        int gameId = repo.saveGame(start, end, 6, "Bot3");
        repo.saveScore(gameId, "Bot3", 200);

        List<Map<String, Object>> scores = repo.getHighestScores();
        assertFalse(scores.isEmpty());
    }
}