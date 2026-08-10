package entity;

public interface Plant {
    int getX();
    int getY();
    int getRow();
    int getHealth();
    void takeDamage(int damage);
    void update();   // 每帧更新（攻击、产阳光等）
    boolean isDead();
}