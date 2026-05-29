package com.minesweeper.data.model;

import com.minesweeper.data.DBConnection;
import com.minesweeper.data.dao.FlagAction;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlagActionDAO {

    // Đặt cờ mới (UC1)
    public boolean insert(FlagAction fa) {
        String sql = """
            INSERT INTO flag_action
              (id, participant_id, row_index, col_index, is_mine, hint_used)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (fa.getId() == null) fa.setId(UUID.randomUUID().toString());
            ps.setString (1, fa.getId());
            ps.setString (2, fa.getParticipantId());
            ps.setInt    (3, fa.getRowIndex());
            ps.setInt    (4, fa.getColIndex());
            ps.setBoolean(5, fa.isMine());
            ps.setBoolean(6, fa.isHintUsed());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
