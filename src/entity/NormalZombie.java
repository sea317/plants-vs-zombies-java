package entity;

import game.GameController;
import java.util.List;

public class NormalZombie implements Zombie {
    private int x, y, row;
    private int health = 15;
    private int speed = 2;
    private int attackTimer = 0;
    private static final int ATTACK_DELAY_FRAMES = 30;

    public NormalZombie(int x, int y, int row) {
        this.x = x;
        this.y = y;
        this.row = row;
    }

    @Override
    public void move() {
        if (attackTimer > 0) {
            attackTimer--;
            return;
        }
        x -= speed;
    }

    @Override
    public void attack(Plant plant) {
        if (attackTimer == 0) {
            plant.takeDamage(10);
            attackTimer = ATTACK_DELAY_FRAMES;
        }
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            GameController.getInstance().onZombieDied();
        }
    }

    @Override
    public int getRow() { return row; }
    @Override
    public int getX() { return x; }
    @Override
    public int getY() {
        return y;
    }
    @Override
    public boolean isDead() { return health <= 0; }
    @Override
    public int getHealth() { return health; }
}