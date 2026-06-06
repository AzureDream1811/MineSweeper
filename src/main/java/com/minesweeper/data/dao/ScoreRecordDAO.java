package com.minesweeper.data.dao;

import com.minesweeper.data.DBConnection;
import com.minesweeper.data.model.ScoreRecord;

import java.sql.*;
import java.util.*;

/**
 * UC11 — Xem bảng High Score.
 * DAO cho bảng score_record.
 * Cung cấp các phương thức truy vấn kỷ lục thẳng từ DB,
 * thay thế cơ chế đọc file .properties cũ.
 */
public class ScoreRecordDAO {

    // ── INSERT (giữ nguyên) ───────────────────────────────────

    public boolean insert(ScoreRecord sr) {
        String sql = """
            INSERT INTO score_record
              (id, participant_id, session_id, mode, difficulty, elapsed_seconds, result)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (sr.getId() == null) sr.setId(UUID.randomUUID().toString());
            ps.setString(1, sr.getId());
            ps.setString(2, sr.getParticipantId());
            ps.setString(3, sr.getSessionId());
            ps.setString(4, sr.getMode());
            ps.setString(5, sr.getDifficulty());
            ps.setInt   (6, sr.getElapsedSeconds());
            ps.setString(7, sr.getResult());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── SOLO LEADERBOARD ──────────────────────────────────────

    /**
     *   11.1.3. Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian tốt, sắp xếp theo thời gian. Hệ thống hiển thị bảng xếp hạng: Hệ thống truy vấn cơ sở dữ liệu lấy danh sách thời gian
     *                tốt nhất của từng người chơi ở chế độ Solo, mức độ được chọn,
     *                chỉ tính lượt thắng (result = 'WIN'), sắp xếp tăng dần theo thời gian.
     *
     * @param difficulty  "EASY" | "MEDIUM" | "HARD"
     * @param limit       số hàng tối đa hiển thị
     * @return danh sách SoloRankRow đã sắp xếp theo thứ hạng
     */
    public List<SoloRankRow> getSoloLeaderboard(String difficulty, int limit) {
        String sql = """
            SELECT
                p.name                         AS player_name,
                MIN(sr.elapsed_seconds)        AS best_seconds,
                MIN(sr.achieved_at)            AS achieved_at
            FROM score_record sr
            JOIN game_participant gp ON gp.id  = sr.participant_id
            JOIN player           p  ON p.id   = gp.player_id
            WHERE sr.mode       = 'SOLO'
              AND sr.result     = 'WIN'
              AND sr.difficulty = ?
            GROUP BY gp.player_id, p.name
            ORDER BY best_seconds ASC
            LIMIT ?
            """;

        List<SoloRankRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, difficulty);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();

            int rank = 1;
            while (rs.next()) {
                SoloRankRow row = new SoloRankRow();
                row.rank        = rank++;
                row.playerName  = rs.getString("player_name");
                row.bestSeconds = rs.getInt("best_seconds");
                row.achievedAt  = rs.getTimestamp("achieved_at");
                rows.add(row);
            }
        } catch (SQLException e) {
            //  11.5.1: Kết nối cơ sở dữ liệu thất bại → trả về danh sách rỗng.
            e.printStackTrace();
        }
        return rows;
    }

    // ── PVP HISTORY ───────────────────────────────────────────

    /**
     *  11.4.2: Hệ thống truy vấn lịch sử các trận PvP gần nhất từ cơ sở dữ liệu.
     * Mỗi trận gồm thông tin của đúng 2 player.
     *
     * @param limit số trận tối đa
     * @return danh sách PvpMatchRow theo thứ tự mới nhất trước
     */
    public List<PvpMatchRow> getPvpHistory(int limit) {
        String sql = """
            SELECT
                gs.id                          AS session_id,
                gs.created_at                  AS session_date,
                p.name                         AS player_name,
                gp.player_order                AS player_order,
                gp.elapsed_seconds             AS elapsed_seconds,
                gp.status                      AS participant_status
            FROM game_session gs
            JOIN game_participant gp ON gp.session_id = gs.id
            JOIN player           p  ON p.id          = gp.player_id
            WHERE gs.mode = 'PVP'
            ORDER BY gs.created_at DESC, gs.id, gp.player_order ASC
            LIMIT ?
            """;

        Map<String, PvpMatchRow> matchMap = new LinkedHashMap<>();
        List<String> sessionOrder = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit * 2);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String sessionId = rs.getString("session_id");

                if (!matchMap.containsKey(sessionId)) {
                    PvpMatchRow match = new PvpMatchRow();
                    match.sessionId   = sessionId;
                    match.sessionDate = rs.getTimestamp("session_date");
                    matchMap.put(sessionId, match);
                    sessionOrder.add(sessionId);
                }

                PvpMatchRow match = matchMap.get(sessionId);
                int order = rs.getInt("player_order");

                if (order == 1) {
                    match.player1Name    = rs.getString("player_name");
                    match.player1Seconds = rs.getInt("elapsed_seconds");
                    match.player1Status  = rs.getString("participant_status");
                } else {
                    match.player2Name    = rs.getString("player_name");
                    match.player2Seconds = rs.getInt("elapsed_seconds");
                    match.player2Status  = rs.getString("participant_status");
                }
            }
        } catch (SQLException e) {
            //  11.5.1: Kết nối cơ sở dữ liệu thất bại → trả về danh sách rỗng.
            e.printStackTrace();
        }

        List<PvpMatchRow> result = new ArrayList<>();
        for (String sid : sessionOrder) {
            if (result.size() >= limit) break;
            PvpMatchRow m = matchMap.get(sid);
            if (m.player1Name != null && m.player2Name != null) {
                result.add(m);
            }
        }
        return result;
    }

    // ── Inner DTOs ────────────────────────────────────────────

    /**
     *  11.1.4. Hệ thống hiển thị bảng xếp hạng : Dữ liệu một hàng trong bảng xếp hạng Solo.
     *                Thứ hạng, Tên, Thời gian thực hiện.
     */
    public static class SoloRankRow {
        public int       rank;
        public String    playerName;
        public int       bestSeconds;
        public Timestamp achievedAt;
    }

    /**
     *  11.4.3: Dữ liệu một trận PvP trong lịch sử.
     *                Thông tin 2 player, tỉ số, thời gian dò mìn.
     */
    public static class PvpMatchRow {
        public String    sessionId;
        public Timestamp sessionDate;
        public String    player1Name;
        public int       player1Seconds;
        public String    player1Status;
        public String    player2Name;
        public int       player2Seconds;
        public String    player2Status;
    }
}