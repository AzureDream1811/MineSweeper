package com.minesweeper.data.dao;

import java.sql.*;

/**
 * Helper khởi tạo H2 in-memory DB với schema tương tự MySQL
 * dùng chung cho tất cả DAO test trong UC11.
 */
public class TestDBHelper {

    static final String H2_URL  = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    static final String H2_USER = "sa";
    static final String H2_PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(H2_URL, H2_USER, H2_PASS);
    }

    /** Tạo toàn bộ bảng cần thiết cho UC11 */
    public static void createSchema() throws SQLException {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS player (
                    id         VARCHAR(36) PRIMARY KEY,
                    name       VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS game_session (
                    id          VARCHAR(36) PRIMARY KEY,
                    mode        VARCHAR(20) NOT NULL,
                    difficulty  VARCHAR(10),
                    status      VARCHAR(20),
                    board_rows  INT NOT NULL,
                    board_cols  INT NOT NULL,
                    total_mines INT NOT NULL,
                    share_link  VARCHAR(500),
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    started_at  TIMESTAMP,
                    ended_at    TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS game_participant (
                    id              VARCHAR(36) PRIMARY KEY,
                    session_id      VARCHAR(36),
                    player_id       VARCHAR(36),
                    player_order    INT NOT NULL,
                    status          VARCHAR(20),
                    elapsed_seconds INT DEFAULT 0,
                    is_paused       TINYINT DEFAULT 0,
                    pause_requested TINYINT DEFAULT 0,
                    joined_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS score_record (
                    id              VARCHAR(36) PRIMARY KEY,
                    participant_id  VARCHAR(36),
                    session_id      VARCHAR(36),
                    mode            VARCHAR(20) NOT NULL,
                    difficulty      VARCHAR(10),
                    elapsed_seconds INT NOT NULL,
                    result          VARCHAR(20) NOT NULL,
                    achieved_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        }
    }

    /** Xoá sạch dữ liệu giữa các test */
    public static void clearAll() throws SQLException {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM score_record");
            st.execute("DELETE FROM game_participant");
            st.execute("DELETE FROM game_session");
            st.execute("DELETE FROM player");
        }
    }

    /** Insert nhanh một bộ player + session + participant + score_record */
    public static String insertSoloWin(String playerName, String difficulty,
                                       int elapsedSeconds) throws SQLException {
        String playerId       = java.util.UUID.randomUUID().toString();
        String sessionId      = java.util.UUID.randomUUID().toString();
        String participantId  = java.util.UUID.randomUUID().toString();
        String scoreId        = java.util.UUID.randomUUID().toString();

        try (Connection conn = getConnection()) {
            conn.createStatement().execute(
                    "INSERT INTO player (id, name) VALUES ('" + playerId + "', '" + playerName + "')");
            conn.createStatement().execute(
                    "INSERT INTO game_session (id, mode, difficulty, status, board_rows, board_cols, total_mines) " +
                            "VALUES ('" + sessionId + "', 'SOLO', '" + difficulty + "', 'finished', 9, 9, 10)");
            conn.createStatement().execute(
                    "INSERT INTO game_participant (id, session_id, player_id, player_order, status) " +
                            "VALUES ('" + participantId + "', '" + sessionId + "', '" + playerId + "', 1, 'WIN')");
            conn.createStatement().execute(
                    "INSERT INTO score_record (id, participant_id, session_id, mode, difficulty, elapsed_seconds, result) " +
                            "VALUES ('" + scoreId + "', '" + participantId + "', '" + sessionId + "', " +
                            "'SOLO', '" + difficulty + "', " + elapsedSeconds + ", 'WIN')");
        }
        return participantId;
    }

    /** Insert nhanh một trận PvP với 2 player */
    public static void insertPvpMatch(String player1Name, int time1, String status1,
                                      String player2Name, int time2, String status2)
            throws SQLException {
        String p1Id  = java.util.UUID.randomUUID().toString();
        String p2Id  = java.util.UUID.randomUUID().toString();
        String sesId = java.util.UUID.randomUUID().toString();
        String gp1Id = java.util.UUID.randomUUID().toString();
        String gp2Id = java.util.UUID.randomUUID().toString();

        try (Connection conn = getConnection()) {
            conn.createStatement().execute(
                    "INSERT INTO player (id, name) VALUES ('" + p1Id + "', '" + player1Name + "')");
            conn.createStatement().execute(
                    "INSERT INTO player (id, name) VALUES ('" + p2Id + "', '" + player2Name + "')");
            conn.createStatement().execute(
                    "INSERT INTO game_session (id, mode, difficulty, status, board_rows, board_cols, total_mines) " +
                            "VALUES ('" + sesId + "', 'PVP', 'EASY', 'finished', 9, 9, 10)");
            conn.createStatement().execute(
                    "INSERT INTO game_participant (id, session_id, player_id, player_order, status, elapsed_seconds) " +
                            "VALUES ('" + gp1Id + "', '" + sesId + "', '" + p1Id + "', 1, '" + status1 + "', " + time1 + ")");
            conn.createStatement().execute(
                    "INSERT INTO game_participant (id, session_id, player_id, player_order, status, elapsed_seconds) " +
                            "VALUES ('" + gp2Id + "', '" + sesId + "', '" + p2Id + "', 2, '" + status2 + "', " + time2 + ")");
        }
    }
}