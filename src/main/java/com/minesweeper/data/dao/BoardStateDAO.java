package com.minesweeper.data.dao;

import com.minesweeper.data.DBConnection;
import com.minesweeper.data.model.BoardState;

import java.sql.*;
import java.util.UUID;

public class BoardStateDAO {

    public boolean insert(BoardState bs) {
        String sql = """
            INSERT INTO board_state
              (id, participant_id, mine_positions, cell_states)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (bs.getId() == null) bs.setId(UUID.randomUUID().toString());
            ps.setString(1, bs.getId());
            ps.setString(2, bs.getParticipantId());
            ps.setString(3, bs.getMinePositions());
            ps.setString(4, bs.getCellStates());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
