package com.minesweeper.model;

public enum GameState {
    /** Chưa bắt đầu ván nào (màn hình khởi động) */
    IDLE,

    /** Đang chơi */
    PLAYING,

    /** Đang tạm dừng — FR-03 */
    PAUSED,

    /** Người chơi thắng — FR-18 */
    WIN,

    /** Người chơi thua (mở trúng mìn) — FR-17 */
    LOSE,

    /** Chế độ PvP cục bộ đang chạy */
    PVP_SPLIT_START
}
