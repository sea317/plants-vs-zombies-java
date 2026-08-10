package game;

import javax.swing.*;

public class GameController {
    private static GameController instance;
    private GamePanel panel;

    private GameController() {
        panel = GamePanel.getInstance();
    }

    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    public void onZombieReachEnd() {
        JOptionPane.showMessageDialog(panel, "僵尸入侵成功！游戏结束！", "失败", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }

    public void onZombieDied() {
        panel.addSunAmount(25);
    }
}