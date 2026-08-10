package entity;

import game.GamePanel;

public class PeaShooter implements Plant {
    private int x, y, row;
    private int health = 10;
    private int shootCooldown = 0;
    private static final int SHOOT_DELAY_FRAMES = 20; // 约1秒

    public PeaShooter(int x, int y, int row) {
        this.x = x;
        this.y = y;
        this.row = row;
    }

    @Override
    public void update() {
        if (shootCooldown > 0) {
            shootCooldown--;
        } else {
            // 检查同行是否有僵尸
            boolean zombieInRow = GamePanel.getInstance().getZombies().stream()
                    .anyMatch(z -> z.getRow() == row && z.getX() > x);
            if (zombieInRow) {
                GamePanel.getInstance().addBullet(new Bullet(x + 40, y + 20, row, 20));
                shootCooldown = SHOOT_DELAY_FRAMES;
                System.out.println("豌豆射手开枪！");  // ← 添加这一行
            }
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