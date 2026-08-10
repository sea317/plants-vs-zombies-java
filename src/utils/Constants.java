package utils;

public class Constants {
    // 窗口尺寸
    public static final int WIDTH = 900;
    public static final int HEIGHT = 600;

    // 网格参数：9列 x 5行
    public static final int COLS = 9;
    public static final int ROWS = 5;
    public static final int CELL_WIDTH = WIDTH / COLS;   // 100
    public static final int CELL_HEIGHT = HEIGHT / ROWS; // 120

    // 初始阳光
    public static final int START_SUN = 300;

    // 植物价格
    public static final int PEASHOOTER_COST = 80;
    public static final int SUNFLOWER_COST = 40;

    // 僵尸波次
    public static final int ZOMBIES_PER_WAVE = 5;
    public static final int WAVE_INTERVAL_FRAMES = 60; // 生成间隔帧数

    // 游戏帧率（毫秒）
    public static final int GAME_TICK_MS = 50; // 20帧/秒
}