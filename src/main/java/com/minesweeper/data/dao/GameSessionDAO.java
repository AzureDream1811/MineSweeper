package com.minesweeper.data.dao;

import com.minesweeper.data.DBConnection;
import com.minesweeper.data.model.GameSession;


import java.sql.*;
import java.util.UUID;

public class GameSessionDAO {

    public boolean insert(GameSession gs) {
        String sql = """
            INSERT INTO game_session
              (id, mode, difficulty, status, board_rows, board_cols, total_mines, share_link)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (gs.getId() == null) gs.setId(UUID.randomUUID().toString());
            ps.setString(1, gs.getId());
            ps.setString(2, gs.getMode());
            ps.setString(3, gs.getDifficulty());
            ps.setString(4, gs.getStatus() != null ? gs.getStatus() : "waiting");
            ps.setInt   (5, gs.getBoardRows());
            ps.setInt   (6, gs.getBoardCols());
            ps.setInt   (7, gs.getTotalMines());
            ps.setString(8, gs.getShareLink());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
