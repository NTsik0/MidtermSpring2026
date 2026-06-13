package persistence;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static SqlSessionFactory factory;
    private static String environment = "development";

    public static void setEnvironment(String env) {
        environment = env;
        factory = null;
    }

    public static SqlSessionFactory getFactory() {
        if (factory == null) {
            try {
                InputStream config = Resources.getResourceAsStream("mybatis-config.xml");
                factory = new SqlSessionFactoryBuilder().build(config, environment);
                initSchema();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load MyBatis config", e);
            }
        }
        return factory;
    }

    private static void initSchema() {
        try (SqlSession session = factory.openSession()) {
            Connection conn = session.getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS players (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(100) NOT NULL UNIQUE);"
                );
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS games (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "started_at TIMESTAMP NOT NULL," +
                    "ended_at TIMESTAMP," +
                    "rounds INTEGER DEFAULT 0," +
                    "winner VARCHAR(100));"
                );
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS game_scores (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "game_id INTEGER NOT NULL," +
                    "player VARCHAR(100) NOT NULL," +
                    "score INTEGER DEFAULT 0," +
                    "FOREIGN KEY (game_id) REFERENCES games(id));"
                );
                conn.commit();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to init schema", e);
            }
        }
    }

    public static SqlSession openSession() {
        return getFactory().openSession(true);
    }
}
