package com.minesweeper.data.dao;

import java.time.LocalDateTime;

public class ScoreRecord {
    private String id;
    private String participantId;
    private String sessionId;
    private String mode;           // basic | pvp
    private String difficulty;
    private int elapsedSeconds;
    private String result;         // win | lose
    private LocalDateTime achievedAt;

    public ScoreRecord() {}

    public ScoreRecord(String id, String participantId, String sessionId,
                       String mode, String difficulty,
                       int elapsedSeconds, String result) {
        this.id             = id;
        this.participantId  = participantId;
        this.sessionId      = sessionId;
        this.mode           = mode;
        this.difficulty     = difficulty;
        this.elapsedSeconds = elapsedSeconds;
        this.result         = result;
    }

    public String getId()                          { return id; }
    public void setId(String id)                   { this.id = id; }

    public String getParticipantId()               { return participantId; }
    public void setParticipantId(String p)         { this.participantId = p; }

    public String getSessionId()                   { return sessionId; }
    public void setSessionId(String s)             { this.sessionId = s; }

    public String getMode()                        { return mode; }
    public void setMode(String mode)               { this.mode = mode; }

    public String getDifficulty()                  { return difficulty; }
    public void setDifficulty(String difficulty)   { this.difficulty = difficulty; }

    public int getElapsedSeconds()                 { return elapsedSeconds; }
    public void setElapsedSeconds(int e)           { this.elapsedSeconds = e; }

    public String getResult()                      { return result; }
    public void setResult(String result)           { this.result = result; }

    public LocalDateTime getAchievedAt()           { return achievedAt; }
    public void setAchievedAt(LocalDateTime a)     { this.achievedAt = a; }
}
