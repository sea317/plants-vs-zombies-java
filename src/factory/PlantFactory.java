package factory;

import entity.PeaShooter;
import entity.Plant;
import entity.Sunflower;
import game.GamePanel.PlantType;

public class PlantFactory {
    public static Plant createPlant(PlantType type, int x, int y, int row) {
        switch (type) {
            case PEASHOOTER:
                return new PeaShooter(x, y, row);
            case SUNFLOWER:
                return new Sunflower(x, y, row);
            default:
                return null;
        }
    }
}