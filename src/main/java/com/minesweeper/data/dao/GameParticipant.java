package com.minesweeper.data.dao;

import java.time.LocalDateTime;

public class GameParticipant {
    private String id;
    private String sessionId;
    private String playerId;
    private int playerOrder;      // 1 hoặc 2
    private String status;        // waiting | playing | win | lose
    private int elapsedSeconds;
    private boolean isPaused;
    private boolean pauseRequested;
    private LocalDateTime joinedAt;

    public GameParticipant() {}

    public GameParticipant(String id, String sessionId, String playerId, int playerOrder) {
        this.id          = id;
        this.sessionId   = sessionId;
        this.playerId    = playerId;
        this.playerOrder = playerOrder;
        this.status      = "waiting";
    }

    public String getId()                              { return id; }
    public void setId(String id)                       { this.id = id; }

    public String getSessionId()                       { return sessionId; }
    public void setSessionId(String sessionId)         { this.sessionId = sessionId; }

    public String getPlayerId()                        { return playerId; }
    public void setPlayerId(String playerId)           { this.playerId = playerId; }

    public int getPlayerOrder()                        { return playerOrder; }
    public void setPlayerOrder(int playerOrder)        { this.playerOrder = playerOrder; }

    public String getStatus()                          { return status; }
    public void setStatus(String status)               { this.status = status; }

    public int getElapsedSeconds()                     { return elapsedSeconds; }
    public void setElapsedSeconds(int elapsedSeconds)  { this.elapsedSeconds = elapsedSeconds; }

    public boolean isPaused()                          { return isPaused; }
    public void setPaused(boolean paused)              { isPaused = paused; }

    public boolean isPauseRequested()                  { return pauseRequested; }
    public void setPauseRequested(boolean pauseRequested) { this.pauseRequested = pauseRequested; }

    public LocalDateTime getJoinedAt()                 { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt)    { this.joinedAt = joinedAt; }
}
