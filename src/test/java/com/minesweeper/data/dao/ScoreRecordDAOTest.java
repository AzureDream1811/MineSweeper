package com.minesweeper.data.dao;

import com.minesweeper.data.dao.ScoreRecordDAO.PvpMatchRow;
import com.minesweeper.data.dao.ScoreRecordDAO.SoloRankRow;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class cho ScoreRecordDAO.
 * Vị trí: src/test/java/com/minesweeper/data/dao/ScoreRecordDAOTest.java
 *
 * Dùng H2 in-memory DB thay MySQL để test độc lập, không cần kết nối thật.
 * DBConnection được override bằng TestDBHelper trong mỗi test.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScoreRecordDAOTest {

    private ScoreRecordDAO dao;

    @BeforeAll
    static void setUpSchema() throws SQLException {
        // Khởi tạo schema H2 một lần cho toàn bộ test class
        TestDBHelper.createSchema();
    }

    @BeforeEach
    void setUp() throws SQLException {
        dao = new ScoreRecordDAO();
        // Xoá sạch dữ liệu trước mỗi test để tránh ảnh hưởng lẫn nhau
        TestDBHelper.clearAll();
    }

    // ════════════════════════════════════════════════════════════
    // getSoloLeaderboard()
    // ════════════════════════════════════════════════════════════

    /**
     *   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Hệ thống truy vấn DB lấy danh sách best time,
     *                sắp xếp tăng dần theo thời gian.
     *  11.1.4. Hệ thống hiển thị bảng xếp hạng : Hiển thị bảng xếp hạng đúng thứ hạng.
     */
    @Test
    @Order(1)
    @DisplayName("getSoloLeaderboard - trả về đúng thứ hạng khi có nhiều người chơi")
    void getSoloLeaderboard_shouldReturnRankedRows_whenMultiplePlayers()
            throws SQLException {
        // Arrange
        TestDBHelper.insertSoloWin("Alice", "EASY", 42);
        TestDBHelper.insertSoloWin("Bob",   "EASY", 30);
        TestDBHelper.insertSoloWin("Carol", "EASY", 55);

        // Act
        List<SoloRankRow> result = dao.getSoloLeaderboard("EASY", 10);

        // Assert
        assertEquals(3, result.size(), "Phải có đúng 3 người chơi");

        // Thứ hạng 1 phải là Bob (thời gian thấp nhất = 30s)
        assertEquals(1,     result.get(0).rank,       "Rank 1 phải là Bob");
        assertEquals("Bob", result.get(0).playerName, "Tên rank 1 phải là Bob");
        assertEquals(30,    result.get(0).bestSeconds,"Thời gian rank 1 phải là 30s");

        assertEquals(2,       result.get(1).rank,       "Rank 2 phải là Alice");
        assertEquals("Alice", result.get(1).playerName);

        assertEquals(3,       result.get(2).rank,       "Rank 3 phải là Carol");
        assertEquals("Carol", result.get(2).playerName);
    }

    /**
     *   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Chỉ tính lượt thắng (result = 'WIN').
     */
    @Test
    @Order(2)
    @DisplayName("getSoloLeaderboard - chỉ tính lượt WIN, bỏ qua LOSE")
    void getSoloLeaderboard_shouldExcludeLoseRecords() throws SQLException {
        // Arrange — Alice thắng, Bob thua
        TestDBHelper.insertSoloWin("Alice", "EASY", 42);

        // Insert thủ công một lượt LOSE của Bob
        String bobParticipantId = insertSoleLose("Bob", "EASY", 99);

        // Act
        List<SoloRankRow> result = dao.getSoloLeaderboard("EASY", 10);

        // Assert
        assertEquals(1, result.size(), "Chỉ có 1 kết quả WIN, LOSE phải bị loại");
        assertEquals("Alice", result.get(0).playerName);
    }

    /**
     *  11.1.5 Hệ thống truy vấn lại dữ liệu và cập nhật bảng tương ứng theo bộ lọc người dùng.: Lọc đúng theo độ khó được chọn.
     */
    @Test
    @Order(3)
    @DisplayName("getSoloLeaderboard - lọc đúng theo difficulty")
    void getSoloLeaderboard_shouldFilterByDifficulty() throws SQLException {
        // Arrange
        TestDBHelper.insertSoloWin("Alice", "EASY",   42);
        TestDBHelper.insertSoloWin("Bob",   "MEDIUM", 60);
        TestDBHelper.insertSoloWin("Carol", "HARD",   90);

        // Act
        List<SoloRankRow> easyResult   = dao.getSoloLeaderboard("EASY",   10);
        List<SoloRankRow> mediumResult = dao.getSoloLeaderboard("MEDIUM", 10);
        List<SoloRankRow> hardResult   = dao.getSoloLeaderboard("HARD",   10);

        // Assert
        assertEquals(1, easyResult.size(),   "EASY chỉ có 1 người");
        assertEquals(1, mediumResult.size(), "MEDIUM chỉ có 1 người");
        assertEquals(1, hardResult.size(),   "HARD chỉ có 1 người");

        assertEquals("Alice", easyResult.get(0).playerName);
        assertEquals("Bob",   mediumResult.get(0).playerName);
        assertEquals("Carol", hardResult.get(0).playerName);
    }

    /**
     *   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Mỗi player chỉ lấy best time (MIN), không lấy nhiều lần.
     */
    @Test
    @Order(4)
    @DisplayName("getSoloLeaderboard - mỗi player chỉ xuất hiện 1 lần với best time")
    void getSoloLeaderboard_shouldReturnBestTimePerPlayer() throws SQLException {
        // Arrange — Alice chơi 2 lần
        TestDBHelper.insertSoloWin("Alice", "EASY", 80);
        TestDBHelper.insertSoloWin("Alice", "EASY", 42); // lần 2 tốt hơn

        // Act
        List<SoloRankRow> result = dao.getSoloLeaderboard("EASY", 10);

        // Assert
        assertEquals(1, result.size(), "Alice chỉ xuất hiện 1 lần");
        assertEquals(42, result.get(0).bestSeconds, "Phải lấy best time = 42s");
    }

    /**
     *  11.2.2: Truy vấn DB trả về rỗng khi chưa có dữ liệu.
     */
    @Test
    @Order(5)
    @DisplayName("getSoloLeaderboard - trả về rỗng khi chưa có kỷ lục")
    void getSoloLeaderboard_shouldReturnEmpty_whenNoData() throws SQLException {
        // Arrange — DB rỗng (đã clearAll trong @BeforeEach)

        // Act
        List<SoloRankRow> result = dao.getSoloLeaderboard("EASY", 10);

        // Assert
        assertTrue(result.isEmpty(), "Kết quả phải rỗng khi chưa có dữ liệu");
    }

    /**
     *  11.1.3: Giới hạn số hàng trả về theo tham số limit.
     */
    @Test
    @Order(6)
    @DisplayName("getSoloLeaderboard - giới hạn đúng số hàng theo limit")
    void getSoloLeaderboard_shouldRespectLimit() throws SQLException {
        // Arrange — 5 người chơi
        for (int i = 1; i <= 5; i++) {
            TestDBHelper.insertSoloWin("Player" + i, "EASY", i * 10);
        }

        // Act
        List<SoloRankRow> result = dao.getSoloLeaderboard("EASY", 3);

        // Assert
        assertEquals(3, result.size(), "Chỉ trả về tối đa 3 hàng theo limit");
    }

    // ════════════════════════════════════════════════════════════
    // getPvpHistory()
    // ════════════════════════════════════════════════════════════

    /**
     *  11.4.2: Hệ thống truy vấn lịch sử trận PvP từ DB.
     *  11.4.3: Mỗi trận có đủ thông tin 2 player.
     */
    @Test
    @Order(7)
    @DisplayName("getPvpHistory - trả về đúng thông tin 2 player mỗi trận")
    void getPvpHistory_shouldReturnMatchWithBothPlayers() throws SQLException {
        // Arrange
        TestDBHelper.insertPvpMatch("Alice", 42, "WIN", "Bob", 65, "LOSE");

        // Act
        List<PvpMatchRow> result = dao.getPvpHistory(10);

        // Assert
        assertEquals(1, result.size(), "Phải có 1 trận PvP");

        PvpMatchRow match = result.get(0);
        assertEquals("Alice", match.player1Name,    "Player 1 phải là Alice");
        assertEquals("Bob",   match.player2Name,    "Player 2 phải là Bob");
        assertEquals(42,      match.player1Seconds, "Thời gian P1 = 42s");
        assertEquals(65,      match.player2Seconds, "Thời gian P2 = 65s");
        assertEquals("WIN",   match.player1Status,  "P1 phải thắng");
        assertEquals("LOSE",  match.player2Status,  "P2 phải thua");
    }

    /**
     *  11.4.4: Trả về rỗng khi chưa có trận PvP.
     */
    @Test
    @Order(8)
    @DisplayName("getPvpHistory - trả về rỗng khi chưa có trận PvP")
    void getPvpHistory_shouldReturnEmpty_whenNoMatches() throws SQLException {
        // Arrange — DB rỗng

        // Act
        List<PvpMatchRow> result = dao.getPvpHistory(10);

        // Assert
        assertTrue(result.isEmpty(), "Kết quả phải rỗng khi chưa có trận PvP");
    }

    /**
     *  11.4.2: Giới hạn số trận trả về theo limit.
     */
    @Test
    @Order(9)
    @DisplayName("getPvpHistory - giới hạn đúng số trận theo limit")
    void getPvpHistory_shouldRespectLimit() throws SQLException {
        // Arrange — 5 trận PvP
        for (int i = 1; i <= 5; i++) {
            TestDBHelper.insertPvpMatch(
                    "P1_" + i, i * 10, "WIN",
                    "P2_" + i, i * 20, "LOSE"
            );
        }

        // Act
        List<PvpMatchRow> result = dao.getPvpHistory(3);

        // Assert
        assertEquals(3, result.size(), "Chỉ trả về tối đa 3 trận theo limit");
    }

    /**
     *  11.4.2: Không trả về trận chỉ có 1 participant (dữ liệu không hợp lệ).
     */
    @Test
    @Order(10)
    @DisplayName("getPvpHistory - bỏ qua trận thiếu player")
    void getPvpHistory_shouldSkipIncompleteMatch() throws SQLException {
        // Arrange — insert session PvP nhưng chỉ có 1 participant
        String sesId = java.util.UUID.randomUUID().toString();
        String p1Id  = java.util.UUID.randomUUID().toString();
        String gp1Id = java.util.UUID.randomUUID().toString();

        try (var conn = TestDBHelper.getConnection()) {
            conn.createStatement().execute(
                    "INSERT INTO player (id, name) VALUES ('" + p1Id + "', 'LonePlayer')");
            conn.createStatement().execute(
                    "INSERT INTO game_session (id, mode, difficulty, status, board_rows, board_cols, total_mines) " +
                            "VALUES ('" + sesId + "', 'PVP', 'EASY', 'finished', 9, 9, 10)");
            conn.createStatement().execute(
                    "INSERT INTO game_participant (id, session_id, player_id, player_order, status, elapsed_seconds) " +
                            "VALUES ('" + gp1Id + "', '" + sesId + "', '" + p1Id + "', 1, 'WIN', 30)");
        }

        // Act
        List<PvpMatchRow> result = dao.getPvpHistory(10);

        // Assert
        assertTrue(result.isEmpty(), "Trận thiếu player 2 phải bị bỏ qua");
    }

    // ════════════════════════════════════════════════════════════
    // insert() — giữ nguyên từ code cũ, test smoke
    // ════════════════════════════════════════════════════════════

    /**
     * Smoke test: insert score_record thành công.
     */
    @Test
    @Order(11)
    @DisplayName("insert - lưu score_record vào DB thành công")
    void insert_shouldPersistScoreRecord() throws SQLException {
        // Arrange
        String participantId = TestDBHelper.insertSoloWin("TestPlayer", "EASY", 99);

        com.minesweeper.data.model.ScoreRecord sr = new com.minesweeper.data.model.ScoreRecord(
                java.util.UUID.randomUUID().toString(),
                participantId,
                java.util.UUID.randomUUID().toString(),
                "SOLO", "EASY", 50, "WIN"
        );

        // Act
        boolean success = dao.insert(sr);

        // Assert
        assertTrue(success, "Insert phải trả về true khi thành công");
    }

    // ════════════════════════════════════════════════════════════
    // Helper private
    // ════════════════════════════════════════════════════════════

    /** Insert một lượt thua để test filter result = WIN */
    private String insertSoleLose(String playerName, String difficulty,
                                  int elapsed) throws SQLException {
        String playerId      = java.util.UUID.randomUUID().toString();
        String sessionId     = java.util.UUID.randomUUID().toString();
        String participantId = java.util.UUID.randomUUID().toString();

        try (var conn = TestDBHelper.getConnection()) {
            conn.createStatement().execute(
                    "INSERT INTO player (id, name) VALUES ('" + playerId + "', '" + playerName + "')");
            conn.createStatement().execute(
                    "INSERT INTO game_session (id, mode, difficulty, status, board_rows, board_cols, total_mines) " +
                            "VALUES ('" + sessionId + "', 'SOLO', '" + difficulty + "', 'finished', 9, 9, 10)");
            conn.createStatement().execute(
                    "INSERT INTO game_participant (id, session_id, player_id, player_order, status) " +
                            "VALUES ('" + participantId + "', '" + sessionId + "', '" + playerId + "', 1, 'LOSE')");
            conn.createStatement().execute(
                    "INSERT INTO score_record (id, participant_id, session_id, mode, difficulty, elapsed_seconds, result) " +
                            "VALUES ('" + java.util.UUID.randomUUID() + "', '" + participantId + "', '" +
                            sessionId + "', 'SOLO', '" + difficulty + "', " + elapsed + ", 'LOSE')");
        }
        return participantId;
    }
}