package entity;

import game.GamePanel;

public class Sunflower implements Plant {
    private int x, y, row;
    private int health = 8;
    private int produceCooldown = 0;
    private static final int PRODUCE_DELAY_FRAMES = 40; // 约2秒

    public Sunflower(int x, int y, int row) {
        this.x = x;
        this.y = y;
        this.row = row;
    }

    @Override
    public void update() {
        if (produceCooldown <= 0) {
            GamePanel.getInstance().addSun(new Sun(x + 30, y + 20));
            produceCooldown = PRODUCE_DELAY_FRAMES;
            System.out.println("向日葵产了一个阳光");  // ← 添加这一行
        } else {
            produceCooldown--;
        }
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;
    }

    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }
    @Override
    public int getRow() { return row; }
    @Override
    public int getHealth() { return health; }
    @Override
    public boolean isDead() { return health <= 0; }
}