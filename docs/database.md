\# Database Documentation



\## Selected Database



\*\*H2\*\* — embedded Java database. No installation required. Data is stored in `uno-data.mv.db` in the project root during normal runs. Tests use an in-memory database that resets automatically.



\## Selected ORM/Persistence Framework



\*\*MyBatis 3.5.16\*\* — SQL mapper framework. Queries are defined in `src/main/resources/mapper/GameMapper.xml`. Java interfaces in `src/main/java/persistence/` handle all database access.



\## Schema



Three tables are created automatically on first run:



```sql

CREATE TABLE players (

&#x20;   id   INTEGER PRIMARY KEY AUTO\_INCREMENT,

&#x20;   name VARCHAR(100) NOT NULL UNIQUE

);



CREATE TABLE games (

&#x20;   id         INTEGER PRIMARY KEY AUTO\_INCREMENT,

&#x20;   started\_at TIMESTAMP NOT NULL,

&#x20;   ended\_at   TIMESTAMP,

&#x20;   rounds     INTEGER DEFAULT 0,

&#x20;   winner     VARCHAR(100)

);



CREATE TABLE game\_scores (

&#x20;   id      INTEGER PRIMARY KEY AUTO\_INCREMENT,

&#x20;   game\_id INTEGER NOT NULL,

&#x20;   player  VARCHAR(100) NOT NULL,

&#x20;   score   INTEGER DEFAULT 0,

&#x20;   FOREIGN KEY (game\_id) REFERENCES games(id)

);

```



No manual schema setup is needed. Tables are created automatically when the game starts.



\## Configuration



MyBatis configuration is in `src/main/resources/mybatis-config.xml`.



\- `development` environment: stores data in `uno-data.mv.db` file

\- `test` environment: uses in-memory H2 database, resets between test runs



No credentials are stored in source code. H2 uses the default `sa` user with no password for local development.



\## How To Run Persistence Tests



```bash

mvn test

```



Persistence tests are in `src/test/java/PersistenceTest.java` and run automatically with all other tests. They use an isolated in-memory database and do not affect game data.



\## How To View Game History And Statistics



After playing one or more games, run:



```bash

java -jar target/uno-cli.jar --stats

```



This shows:



\- Recent games with winner, rounds, and timestamp

\- Player win counts

\- Highest cumulative scores

