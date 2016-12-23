package ch.atlantis.game;

import java.io.Serializable;

/**
 * Created by Fabian on 28/10/16.
 *
 * Tiles are dividing the board view into squares where all the elements are placed on top.
 * They have a pathId, an x and y coordinate.
 */

public class Tile implements Serializable {
    private static final long serialVersionUID = 7661939850705259952L;
    private int x;
    private int y;
    private int pathId;


    public Tile(int x, int y, int pathId) {
        this.x = x;
        this.y = y;
        this.pathId = pathId;
    }

    public int getPathId() {
        return pathId;
    }
}
