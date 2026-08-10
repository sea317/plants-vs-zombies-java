package entity;

public class Sun {
    private int x, y;
    private int lifeTime = 120; // 大约6秒消失（20帧/秒 * 6）
    private boolean collected = false;

    public Sun(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        lifeTime--;
    }

    public boolean shouldRemove() {
        return lifeTime <= 0 || collected;
    }

    public void collect() {
        collected = true;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}