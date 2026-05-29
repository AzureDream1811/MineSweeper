package com.minesweeper.data.dao;

import java.time.LocalDateTime;

public class GameSession {
    private String id;
    private String mode;          // basic | pvp
    private String difficulty;    // easy | medium | hard
    private String status;        // waiting | playing | paused | finished
    private int boardRows;
    private int boardCols;
    private int totalMines;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String shareLink;

    public GameSession() {}

    public GameSession(String id, String mode, String difficulty,
                       int boardRows, int boardCols, int totalMines) {
        this.id         = id;
        this.mode       = mode;
        this.difficulty = difficulty;
        this.boardRows  = boardRows;
        this.boardCols  = boardCols;
        this.totalMines = totalMines;
        this.status     = "waiting";
    }

    public String getId()                          { return id; }
    public void setId(String id)                   { this.id = id; }

    public String getMode()                        { return mode; }
    public void setMode(String mode)               { this.mode = mode; }

    public String getDifficulty()                  { return difficulty; }
    public void setDifficulty(String difficulty)   { this.difficulty = difficulty; }

    public String getStatus()                      { return status; }
    public void setStatus(String status)           { this.status = status; }

    public int getBoardRows()                      { return boardRows; }
    public void setBoardRows(int boardRows)        { this.boardRows = boardRows; }

    public int getBoardCols()                      { return boardCols; }
    public void setBoardCols(int boardCols)        { this.boardCols = boardCols; }

    public int getTotalMines()                     { return totalMines; }
    public void setTotalMines(int totalMines)      { this.totalMines = totalMines; }

    public LocalDateTime getCreatedAt()                    { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }

    public LocalDateTime getStartedAt()                    { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt)      { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt()                      { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt)          { this.endedAt = endedAt; }

    public String getShareLink()                   { return shareLink; }
    public void setShareLink(String shareLink)     { this.shareLink = shareLink; }
}
