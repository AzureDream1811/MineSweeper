package com.minesweeper.data.dao;

import java.time.LocalDateTime;

public class FlagAction {
    private String id;
    private String participantId;
    private int rowIndex;
    private int colIndex;
    private boolean isMine;        // Đổi màu cờ xanh lá nếu đúng (UC1)
    private boolean hintUsed;      // Đã dùng hint cho ô này chưa (UC1)
    private LocalDateTime placedAt;
    private LocalDateTime removedAt; // NULL = đang cắm | có giá trị = đã gỡ (UC2)

    public FlagAction() {}

    public FlagAction(String id, String participantId,
                      int rowIndex, int colIndex, boolean isMine) {
        this.id            = id;
        this.participantId = participantId;
        this.rowIndex      = rowIndex;
        this.colIndex      = colIndex;
        this.isMine        = isMine;
    }

    public String getId()                          { return id; }
    public void setId(String id)                   { this.id = id; }

    public String getParticipantId()               { return participantId; }
    public void setParticipantId(String p)         { this.participantId = p; }

    public int getRowIndex()                       { return rowIndex; }
    public void setRowIndex(int rowIndex)          { this.rowIndex = rowIndex; }

    public int getColIndex()                       { return colIndex; }
    public void setColIndex(int colIndex)          { this.colIndex = colIndex; }

    public boolean isMine()                        { return isMine; }
    public void setMine(boolean mine)              { isMine = mine; }

    public boolean isHintUsed()                    { return hintUsed; }
    public void setHintUsed(boolean hintUsed)      { this.hintUsed = hintUsed; }

    public LocalDateTime getPlacedAt()             { return placedAt; }
    public void setPlacedAt(LocalDateTime p)       { this.placedAt = p; }

    public LocalDateTime getRemovedAt()            { return removedAt; }
    public void setRemovedAt(LocalDateTime r)      { this.removedAt = r; }

    public boolean isActive() { return removedAt == null; }
}
