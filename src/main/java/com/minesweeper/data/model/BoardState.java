package com.minesweeper.data.model;

import java.time.LocalDateTime;

public class BoardState {
    private String id;
    private String participantId;
    private String minePositions;  // JSON array: [{row,col}, ...]
    private String cellStates;     // JSON 2D array: hidden|open|flagged
    private int flagsPlaced;
    private int flagsCorrect;      // Chế độ easy: hiển thị số mìn = số cờ đúng (UC4)
    private int cellsOpened;
    private LocalDateTime updatedAt;

    public BoardState() {}

    public BoardState(String id, String participantId,
                      String minePositions, String cellStates) {
        this.id             = id;
        this.participantId  = participantId;
        this.minePositions  = minePositions;
        this.cellStates     = cellStates;
    }

    public String getId()                          { return id; }
    public void setId(String id)                   { this.id = id; }

    public String getParticipantId()               { return participantId; }
    public void setParticipantId(String p)         { this.participantId = p; }

    public String getMinePositions()               { return minePositions; }
    public void setMinePositions(String m)         { this.minePositions = m; }

    public String getCellStates()                  { return cellStates; }
    public void setCellStates(String c)            { this.cellStates = c; }

    public int getFlagsPlaced()                    { return flagsPlaced; }
    public void setFlagsPlaced(int flagsPlaced)    { this.flagsPlaced = flagsPlaced; }

    public int getFlagsCorrect()                   { return flagsCorrect; }
    public void setFlagsCorrect(int flagsCorrect)  { this.flagsCorrect = flagsCorrect; }

    public int getCellsOpened()                    { return cellsOpened; }
    public void setCellsOpened(int cellsOpened)    { this.cellsOpened = cellsOpened; }

    public LocalDateTime getUpdatedAt()                    { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)      { this.updatedAt = updatedAt; }
}
