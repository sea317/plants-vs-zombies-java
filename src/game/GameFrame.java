package game;

import javax.swing.*;

public class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("植物大战僵尸 - Java版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        GamePanel panel = GamePanel.getInstance();
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}