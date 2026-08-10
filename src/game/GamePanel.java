package game;

import entity.*;
import factory.PlantFactory;
import utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GamePanel extends JPanel implements Runnable {
    private static GamePanel instance;
    private Thread gameThread;
    private boolean running = true;

    // 游戏对象集合（使用线程安全集合）
    private List<Plant> plants = new CopyOnWriteArrayList<>();
    private List<Zombie> zombies = new CopyOnWriteArrayList<>();
    private List<Bullet> bullets = new CopyOnWriteArrayList<>();
    private List<Sun> suns = new CopyOnWriteArrayList<>();

    private int sunAmount = Constants.START_SUN;
    private int wave = 1;
    private int remainingZombies = 0;
    private int waveSpawnCounter = 0;

    // 当前选中的植物类型
    public enum PlantType { PEASHOOTER, SUNFLOWER }
    private PlantType selectedPlant = PlantType.PEASHOOTER;

    private Random random = new Random();

    private GamePanel() {
        setPreferredSize(new Dimension(Constants.WIDTH, Constants.HEIGHT));
        setFocusable(true);
        initMouseListener();
        initKeyListener();
        startWave();
        startGameThread();
    }

    public static GamePanel getInstance() {
        if (instance == null) {
            instance = new GamePanel();
        }
        return instance;
    }

    private void initMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 放置植物
                int col = e.getX() / Constants.CELL_WIDTH;
                int row = e.getY() / Constants.CELL_HEIGHT;
                if (col >= 0 && col < Constants.COLS && row >= 0 && row < Constants.ROWS) {
                    int x = col * Constants.CELL_WIDTH;
                    int y = row * Constants.CELL_HEIGHT;
                    boolean occupied = plants.stream().anyMatch(p -> p.getX() == x && p.getY() == y);
                    if (!occupied) {
                        Plant p = PlantFactory.createPlant(selectedPlant, x, y, row);
                        int cost = (selectedPlant == PlantType.PEASHOOTER) ? Constants.PEASHOOTER_COST : Constants.SUNFLOWER_COST;
                        if (p != null && sunAmount >= cost) {
                            sunAmount -= cost;
                            plants.add(p);
                            System.out.println("种植成功！消耗 " + cost + " 阳光，剩余阳光：" + sunAmount);
                        } else {
                            System.out.println("阳光不足！需要 " + cost + "，当前阳光 " + sunAmount);
                        }
                    }
                }

                // 收集阳光
                for (Sun sun : suns) {
                    Rectangle rect = new Rectangle(sun.getX(), sun.getY(), 20, 20);
                    if (rect.contains(e.getPoint())) {
                        sun.collect();
                        sunAmount += 50;
                        System.out.println("收集阳光！当前阳光：" + sunAmount);
                        break;
                    }
                }
            }
        });
    }

    private void initKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_1) {
                    selectedPlant = PlantType.PEASHOOTER;
                    System.out.println("切换到：豌豆射手 (100阳光)");
                } else if (e.getKeyCode() == KeyEvent.VK_2) {
                    selectedPlant = PlantType.SUNFLOWER;
                    System.out.println("切换到：向日葵 (50阳光)");
                }
            }
        });
    }

    private void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (running) {
            updateGame();
            repaint();
            try {
                Thread.sleep(Constants.GAME_TICK_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGame() {
        // 1. 更新植物
        for (Plant p : plants) {
            p.update();
        }
        plants.removeIf(Plant::isDead);

        // 2. 更新子弹
        for (Bullet b : bullets) {
            b.move();
        }

        // 子弹碰撞检测
        for (Bullet b : bullets) {
            for (Zombie z : zombies) {
                if (z.getRow() == b.getRow() && Math.abs(z.getX() - b.getX()) < 30) {
                    z.takeDamage(b.getDamage());
                    bullets.remove(b);
                    break;
                }
            }
        }
        bullets.removeIf(b -> b.isOutOfBounds());

        // 3. 更新僵尸
        for (Zombie z : zombies) {
            // 寻找前方最近的植物
            Plant target = null;
            for (Plant p : plants) {
                if (p.getRow() == z.getRow() && p.getX() > z.getX()) {
                    if (target == null || p.getX() < target.getX()) {
                        target = p;
                    }
                }
            }
            if (target != null && Math.abs(z.getX() - target.getX()) < 20) {
                z.attack(target);
            } else {
                z.move();
            }
            // 检查是否到达房子
            if (z.getX() <= 0) {
                GameController.getInstance().onZombieReachEnd();
                zombies.remove(z);
                break;
            }
        }
        zombies.removeIf(Zombie::isDead);

        // 4. 更新阳光
        for (Sun s : suns) {
            s.update();
        }
        suns.removeIf(Sun::shouldRemove);

        // 5. 波次控制
        if (remainingZombies == 0 && zombies.isEmpty()) {
            wave++;
            startWave();
        }

        if (waveSpawnCounter > 0) {
            waveSpawnCounter--;
            if (waveSpawnCounter == 0) {
                spawnZombie();
            }
        }
    }

    private void startWave() {
        int count = Constants.ZOMBIES_PER_WAVE + wave / 2;
        remainingZombies = count;
        waveSpawnCounter = 60;
        System.out.println("第 " + wave + " 波开始！剩余僵尸：" + remainingZombies);
    }

    private void spawnZombie() {
        if (remainingZombies <= 0) return;
        int row = (int)(Math.random() * Constants.ROWS);
        int x = Constants.WIDTH - 50;
        int y = row * Constants.CELL_HEIGHT + Constants.CELL_HEIGHT/2 - 30;
        zombies.add(new NormalZombie(x, y, row));
        remainingZombies--;
        if (remainingZombies > 0) {
            waveSpawnCounter = 60;
        }
        System.out.println("生成一只僵尸，剩余：" + remainingZombies);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ========== 1. 草地背景渐变 ==========
        GradientPaint grassGradient = new GradientPaint(
                0, 0, new Color(34, 139, 34),
                0, Constants.HEIGHT, new Color(50, 180, 50)
        );
        g2d.setPaint(grassGradient);
        g2d.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        // ========== 2. 草地质感（随机亮点）==========
        g2d.setColor(new Color(60, 170, 60, 80));
        for (int i = 0; i < 800; i++) {
            int x = random.nextInt(Constants.WIDTH);
            int y = random.nextInt(Constants.HEIGHT);
            g2d.fillRect(x, y, 2, 2);
        }

        // ========== 3. 小草线条 ==========
        g2d.setColor(new Color(30, 120, 30, 120));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < 400; i++) {
            int x = random.nextInt(Constants.WIDTH);
            int y = random.nextInt(Constants.HEIGHT);
            g2d.drawLine(x, y, x + 4, y - 8);
            g2d.drawLine(x, y, x - 3, y - 6);
        }

        // ========== 4. 网格线（浮雕效果）==========
        // 暗线
        g2d.setColor(new Color(0, 0, 0, 60));
        for (int i = 0; i <= Constants.COLS; i++) {
            int x = i * Constants.CELL_WIDTH;
            g2d.drawLine(x, 0, x, Constants.HEIGHT);
        }
        for (int i = 0; i <= Constants.ROWS; i++) {
            int y = i * Constants.CELL_HEIGHT;
            g2d.drawLine(0, y, Constants.WIDTH, y);
        }

        // 亮线（高光）
        g2d.setColor(new Color(255, 255, 255, 40));
        for (int i = 0; i <= Constants.COLS; i++) {
            int x = i * Constants.CELL_WIDTH + 1;
            g2d.drawLine(x, 0, x, Constants.HEIGHT);
        }
        for (int i = 0; i <= Constants.ROWS; i++) {
            int y = i * Constants.CELL_HEIGHT + 1;
            g2d.drawLine(0, y, Constants.WIDTH, y);
        }

        // ========== 5. 每一行的阴影条 ==========
        g2d.setColor(new Color(0, 0, 0, 35));
        for (int i = 0; i < Constants.ROWS; i++) {
            int y = i * Constants.CELL_HEIGHT + Constants.CELL_HEIGHT - 8;
            g2d.fillRect(0, y, Constants.WIDTH, 8);
        }

        // ========== 6. 草地上的小野花 ==========
        for (int i = 0; i < 80; i++) {
            int x = random.nextInt(Constants.WIDTH);
            int y = random.nextInt(Constants.HEIGHT);
            if (x % Constants.CELL_WIDTH > 15 && x % Constants.CELL_WIDTH < 35) {
                g2d.setColor(new Color(255, 200, 100, 180));
                g2d.fillOval(x, y, 4, 4);
                g2d.setColor(new Color(255, 255, 150, 180));
                g2d.fillOval(x - 2, y - 2, 3, 3);
            }
        }

        // ========== 7. 绘制植物 ==========
        for (Plant p : plants) {
            if (p instanceof PeaShooter) {
                drawPeaShooter(g2d, p.getX(), p.getY(), p.getHealth());
            } else if (p instanceof Sunflower) {
                drawSunflower(g2d, p.getX(), p.getY(), p.getHealth());
            }
        }

        // ========== 8. 绘制僵尸 ==========
        for (Zombie z : zombies) {
            drawZombie(g2d, z.getX(), z.getY(), z.getHealth());
        }

        // ========== 9. 绘制子弹 ==========
        for (Bullet b : bullets) {
            drawPea(g2d, b.getX(), b.getY());
        }

        // ========== 10. 绘制阳光 ==========
        for (Sun s : suns) {
            drawSun(g2d, s.getX(), s.getY());
        }

        // ========== 11. UI信息 ==========
        // 半透明背景
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(10, 10, 200, 100, 15, 15);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g2d.drawString("☀ " + sunAmount, 25, 45);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 16));
        g2d.drawString("第 " + wave + " 波", 25, 75);
        g2d.drawString("僵尸剩余: " + (remainingZombies + zombies.size()), 25, 100);

        // 当前选中植物显示
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(Constants.WIDTH - 160, 10, 150, 40, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        String selectedName = (selectedPlant == PlantType.PEASHOOTER) ? "🌱 豌豆射手 (按1)" : "🌻 向日葵 (按2)";
        g2d.drawString(selectedName, Constants.WIDTH - 150, 35);
    }

    // ==================== 绘制方法 ====================

    // 绘制豌豆射手（更接近原版造型）
    private void drawPeaShooter(Graphics2D g, int x, int y, int health) {
        // 阴影
        g.setColor(new Color(0, 0, 0, 40));
        g.fillOval(x + 12, y + 25, 55, 60);

        // 头部（大圆脑袋）
        g.setColor(new Color(100, 200, 80));
        g.fillOval(x + 10, y + 15, 60, 65);

        // 头部高光
        g.setColor(new Color(140, 230, 110));
        g.fillOval(x + 15, y + 20, 50, 40);

        // 嘴巴（大嘴，豌豆射手的标志）
        g.setColor(new Color(180, 70, 60));
        g.fillOval(x + 55, y + 38, 28, 25);

        // 嘴巴内部（深色）
        g.setColor(new Color(120, 40, 35));
        g.fillOval(x + 60, y + 43, 18, 15);

        // 嘴巴高光
        g.setColor(new Color(220, 120, 100));
        g.fillOval(x + 58, y + 40, 8, 6);

        // 眼睛（大眼，萌态）
        g.setColor(Color.WHITE);
        g.fillOval(x + 18, y + 28, 20, 20);
        g.fillOval(x + 42, y + 28, 20, 20);

        g.setColor(Color.BLACK);
        g.fillOval(x + 24, y + 34, 10, 10);
        g.fillOval(x + 48, y + 34, 10, 10);

        g.setColor(Color.WHITE);
        g.fillOval(x + 26, y + 36, 4, 4);
        g.fillOval(x + 50, y + 36, 4, 4);

        // 眉毛（生气表情）
        g.setColor(new Color(60, 120, 50));
        g.setStroke(new BasicStroke(3));
        g.drawLine(x + 16, y + 25, x + 32, y + 28);
        g.drawLine(x + 48, y + 28, x + 64, y + 25);

        // 头上的叶子（豌豆苗）
        g.setColor(new Color(60, 160, 50));
        g.fillOval(x + 28, y + 2, 14, 22);
        g.fillOval(x + 38, y + 5, 12, 18);

        g.setColor(new Color(40, 120, 35));
        g.drawLine(x + 35, y + 8, x + 35, y + 20);
        g.drawLine(x + 42, y + 10, x + 42, y + 18);

        // 身体（下方椭圆）
        g.setColor(new Color(80, 170, 70));
        g.fillOval(x + 20, y + 60, 40, 30);

        // 底座/根（两片小叶子）
        g.setColor(new Color(70, 150, 60));
        g.fillOval(x + 12, y + 75, 15, 10);
        g.fillOval(x + 53, y + 75, 15, 10);

        // 枪口指向方向（小圆圈）
        g.setColor(new Color(200, 90, 70));
        g.fillOval(x + 80, y + 46, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(x + 82, y + 48, 4, 4);

        // 血量显示
        g.setColor(Color.RED);
        g.setFont(new Font("微软雅黑", Font.BOLD, 12));
        g.drawString("❤" + health, x + 5, y + 12);
    }

    // 绘制向日葵
    private void drawSunflower(Graphics2D g, int x, int y, int health) {
        // 阴影
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x + 18, y + 30, 50, 50);

        // 花瓣
        g.setColor(new Color(255, 220, 80));
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI * 2 / 12;
            int px = x + 40 + (int)(28 * Math.cos(angle));
            int py = y + 40 + (int)(22 * Math.sin(angle));
            g.fillOval(px - 8, py - 8, 16, 16);
        }

        // 花盘
        g.setColor(new Color(120, 80, 40));
        g.fillOval(x + 22, y + 28, 36, 36);

        // 花盘纹理
        g.setColor(new Color(90, 60, 30));
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                g.fillRect(x + 26 + i * 6, y + 32 + j * 6, 2, 2);
            }
        }

        // 眼睛
        g.setColor(Color.WHITE);
        g.fillOval(x + 28, y + 38, 8, 8);
        g.fillOval(x + 44, y + 38, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(x + 30, y + 40, 4, 4);
        g.fillOval(x + 46, y + 40, 4, 4);

        // 微笑
        g.setColor(new Color(80, 50, 20));
        g.drawArc(x + 30, y + 46, 20, 10, 0, -180);

        // 叶子
        g.setColor(new Color(60, 140, 60));
        g.fillOval(x + 8, y + 55, 15, 10);
        g.fillOval(x + 58, y + 55, 15, 10);

        // 血量
        g.setColor(Color.RED);
        g.setFont(new Font("微软雅黑", Font.BOLD, 11));
        g.drawString("❤" + health, x + 5, y + 15);
    }

    // 绘制僵尸（更接近原版造型）
    private void drawZombie(Graphics2D g, int x, int y, int health) {
        // 阴影
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x + 8, y + 65, 45, 15);

        // 身体/躯干
        g.setColor(new Color(70, 90, 60));
        g.fillRect(x + 12, y + 35, 40, 45);

        // 衣服（破烂的西装）
        g.setColor(new Color(50, 65, 45));
        g.fillRect(x + 15, y + 50, 34, 30);

        // 衣服破洞
        g.setColor(new Color(40, 55, 35));
        g.fillRect(x + 20, y + 55, 8, 8);
        g.fillRect(x + 35, y + 60, 6, 10);

        // 领带（歪斜）
        g.setColor(new Color(100, 40, 40));
        g.fillRect(x + 28, y + 48, 8, 18);
        g.fillOval(x + 26, y + 64, 12, 6);

        // 头部
        g.setColor(new Color(90, 120, 75));
        g.fillOval(x + 8, y + 5, 48, 45);

        // 头部青筋/血管
        g.setColor(new Color(70, 100, 60));
        g.drawLine(x + 15, y + 12, x + 25, y + 18);
        g.drawLine(x + 45, y + 12, x + 38, y + 18);
        g.drawLine(x + 20, y + 45, x + 28, y + 42);

        // 眼睛（死鱼眼）
        g.setColor(new Color(220, 220, 200));
        g.fillOval(x + 14, y + 18, 12, 12);
        g.fillOval(x + 36, y + 18, 12, 12);

        g.setColor(new Color(40, 50, 35));
        g.fillOval(x + 18, y + 22, 5, 5);
        g.fillOval(x + 40, y + 22, 5, 5);

        // 眼睛血丝
        g.setColor(new Color(150, 50, 50, 120));
        g.drawLine(x + 16, y + 20, x + 12, y + 18);
        g.drawLine(x + 24, y + 20, x + 28, y + 18);
        g.drawLine(x + 38, y + 20, x + 34, y + 18);
        g.drawLine(x + 46, y + 20, x + 50, y + 18);

        // 黑眼圈
        g.setColor(new Color(40, 60, 35, 150));
        g.fillOval(x + 12, y + 20, 16, 10);
        g.fillOval(x + 34, y + 20, 16, 10);

        // 鼻子
        g.setColor(new Color(60, 85, 50));
        g.fillOval(x + 30, y + 28, 6, 5);
        g.setColor(new Color(50, 70, 40));
        g.fillOval(x + 31, y + 29, 4, 3);

        // 嘴巴（露出牙齿）
        g.setColor(new Color(50, 40, 35));
        g.fillRect(x + 20, y + 34, 24, 10);

        g.setColor(new Color(230, 220, 180));
        g.fillRect(x + 22, y + 34, 5, 6);
        g.fillRect(x + 29, y + 34, 5, 6);
        g.fillRect(x + 36, y + 34, 5, 6);

        // 缺了一颗牙
        g.setColor(new Color(50, 40, 35));
        g.fillRect(x + 29, y + 34, 5, 6);

        // 下排牙齿
        g.fillRect(x + 24, y + 40, 4, 4);
        g.fillRect(x + 33, y + 40, 4, 4);

        // 嘴角下垂
        g.setColor(new Color(40, 30, 25));
        g.drawLine(x + 18, y + 38, x + 14, y + 42);
        g.drawLine(x + 44, y + 38, x + 48, y + 42);

        // 耳朵（残缺）
        g.setColor(new Color(80, 110, 65));
        g.fillOval(x + 4, y + 20, 8, 12);
        g.fillOval(x + 50, y + 20, 8, 12);

        // 头发（稀疏）
        g.setColor(new Color(50, 70, 45));
        g.drawLine(x + 18, y + 6, x + 22, y + 10);
        g.drawLine(x + 28, y + 4, x + 30, y + 9);
        g.drawLine(x + 38, y + 5, x + 40, y + 10);
        g.drawLine(x + 45, y + 7, x + 44, y + 11);

        // 手臂（向前伸）
        g.setColor(new Color(80, 110, 65));
        g.fillRect(x - 18, y + 42, 30, 12);
        g.fillOval(x - 22, y + 40, 12, 14);

        // 左手手指
        g.setColor(new Color(70, 100, 55));
        g.fillRect(x - 22, y + 40, 4, 4);
        g.fillRect(x - 20, y + 44, 4, 4);
        g.fillRect(x - 18, y + 48, 4, 4);

        // 右臂
        g.fillRect(x + 52, y + 45, 18, 10);
        g.fillOval(x + 68, y + 43, 10, 12);

        g.fillRect(x + 70, y + 43, 4, 4);
        g.fillRect(x + 72, y + 47, 4, 4);

        // 腿（罗圈腿）
        g.setColor(new Color(65, 90, 55));
        g.fillRect(x + 14, y + 78, 12, 15);
        g.fillRect(x + 36, y + 78, 12, 15);

        // 破裤子
        g.setColor(new Color(50, 65, 45));
        g.fillRect(x + 16, y + 75, 8, 8);
        g.fillRect(x + 38, y + 78, 6, 6);

        // 僵尸特有的绿色尸斑
        g.setColor(new Color(60, 140, 60, 80));
        g.fillOval(x + 20, y + 30, 8, 6);
        g.fillOval(x + 40, y + 35, 7, 5);
        g.fillOval(x + 25, y + 55, 6, 4);

        // 脚下的阴影
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(x + 10, y + 92, 45, 10);

        // 血量显示
        g.setColor(Color.RED);
        g.setFont(new Font("微软雅黑", Font.BOLD, 12));
        g.drawString("❤" + health, x + 5, y + 10);
    }

    // 绘制豌豆子弹
    private void drawPea(Graphics2D g, int x, int y) {
        // 阴影
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(x - 1, y + 2, 12, 12);

        // 豌豆主体
        g.setColor(new Color(70, 210, 70));
        g.fillOval(x, y, 10, 10);

        // 高光
        g.setColor(new Color(130, 250, 130));
        g.fillOval(x + 2, y + 2, 4, 4);

        // 中线
        g.setColor(new Color(40, 160, 40));
        g.drawLine(x + 3, y + 5, x + 7, y + 5);
    }

    // 绘制阳光
    private void drawSun(Graphics2D g, int x, int y) {
        // 外发光
        g.setColor(new Color(255, 200, 50, 100));
        g.fillOval(x - 5, y - 5, 30, 30);

        // 阳光主体
        g.setColor(new Color(255, 180, 50));
        g.fillOval(x, y, 20, 20);

        // 高光
        g.setColor(new Color(255, 220, 100));
        g.fillOval(x + 3, y + 3, 8, 8);

        // 光芒
        g.setColor(new Color(255, 200, 80));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2 / 8;
            int px = x + 10 + (int)(14 * Math.cos(angle));
            int py = y + 10 + (int)(14 * Math.sin(angle));
            g.fillOval(px - 3, py - 3, 6, 6);
        }

        // 文字
        g.setColor(Color.BLACK);
        g.setFont(new Font("微软雅黑", Font.BOLD, 10));
        g.drawString("+50", x + 5, y + 14);
    }

    // 外部调用接口
    public void addSun(Sun sun) { suns.add(sun); }
    public void addBullet(Bullet bullet) { bullets.add(bullet); }
    public void addSunAmount(int amount) { sunAmount += amount; }
    public List<Zombie> getZombies() { return zombies; }
}