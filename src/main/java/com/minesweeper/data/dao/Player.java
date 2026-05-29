package com.minesweeper.data.dao;

import java.time.LocalDateTime;

public class Player {
    private String id;
    private String name;
    private LocalDateTime createdAt;

    public Player() {}

    public Player(String id, String name) {
        this.id   = id;
        this.name = name;
    }

    public String getId()                    { return id; }
    public void setId(String id)             { this.id = id; }

    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }

    public LocalDateTime getCreatedAt()                      { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)        { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Player{id='" + id + "', name='" + name + "'}";
    }
}
