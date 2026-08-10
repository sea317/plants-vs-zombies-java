package entity;

import utils.Constants;

public class Bullet {
    private int x, y, row;
    private int damage;
    private static final int SPEED = 8;

    public Bullet(int x, int y, int row, int damage) {
        this.x = x;
        this.y = y;
        this.row = row;
        this.damage = damage;
    }

    public void move() {
        x += SPEED;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRow() { return row; }
    public int getDamage() { return damage; }
    public boolean isOutOfBounds() {
        return x > Constants.WIDTH;
    }
}