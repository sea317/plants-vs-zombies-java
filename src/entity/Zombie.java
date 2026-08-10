package entity;

public interface Zombie {
    void move();
    void attack(Plant plant);
    void takeDamage(int damage);
    int getRow();
    int getX();
    int getY();
    boolean isDead();
    int getHealth();   // 用于显示血量
}