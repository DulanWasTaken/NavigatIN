package es.udc.tfg.navigatin.adapter;

/**
 * Created by Usuario on 10/11/2016.
 */

public class Level {
    private int level;
    private int levelID,BuildingID;

    public Level(int levelID, int buildingID, int level) {
        this.levelID = levelID;
        BuildingID = buildingID;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public int getLevelID() {
        return levelID;
    }

    public int getBuildingID() {
        return BuildingID;
    }
}
