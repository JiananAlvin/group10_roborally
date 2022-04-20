package model.game.board.map;

import content.MapName;
import io.cucumber.java.bs.A;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import model.game.board.map.element.CheckPoint;
import model.game.board.map.element.RebootPoint;
import model.game.board.map.element.StartPoint;
import model.game.board.map.element.Tile;
import utils.MapReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

@Data
@RequiredArgsConstructor
public class GameMap {
    private String mapName;
    private Tile[][] content;
    private ArrayList<StartPoint> startPoints;
    private ArrayList<RebootPoint> rebootPoints;
    // checkPoints are sorted in numerical order
    private ArrayList<CheckPoint> checkPoints;

    /**
     * GameMap: Initialize an instance of GameMap from the name of MAPNAME.txt
     *
     * @param mapName the name of this map. Such as 'STARTER', 'BEGINNER' represent the map stored in 'STARTER.txt' and 'BEGINNER.txt'.
     */
    public GameMap(MapName mapName) throws IOException {
        this.mapName = mapName.getMapName();
        this.content = MapReader.txtToTileMatrix(this.mapName);
        this.startPoints = new ArrayList<>();
        this.rebootPoints = new ArrayList<>();
        this.checkPoints = new ArrayList<>();
        for (Tile[] tiles : content)
            for (Tile tile : tiles) {
                if (RebootPoint.class.getName().equals(tile.getClass().getName()))
                    this.rebootPoints.add((RebootPoint) tile);
                else if (StartPoint.class.getName().equals(tile.getClass().getName()))
                    this.startPoints.add((StartPoint) tile);
                else if (CheckPoint.class.getName().equals(tile.getClass().getName()))
                    this.checkPoints.add((CheckPoint) tile);
            }
        this.checkPoints.sort(new Comparator<CheckPoint>() {
            @Override
            public int compare(CheckPoint o1, CheckPoint o2) {
                return o1.getCheckPointNum() - o2.getCheckPointNum();
            }
        });
    }

    public StartPoint getARandomStartPoint() {
        int randomSeed = new Random().nextInt(this.startPoints.size());
        return this.startPoints.get(randomSeed);
    }

    public RebootPoint getARandomRebootPoint() {
        int randomSeed = new Random().nextInt(this.rebootPoints.size());
        return this.rebootPoints.get(randomSeed);
    }

    public Tile getTileWithPosition(Position position) {
        return content[position.getXcoord()][position.getYcoord()];
    }
}
