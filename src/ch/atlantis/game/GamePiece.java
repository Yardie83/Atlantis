package ch.atlantis.game;

import javafx.scene.shape.Rectangle;

import java.io.Serializable;

/**
 * Created by Hermann Grieder on 07.10.2016.
 */
public class GamePiece extends Rectangle implements Serializable {

    private static final long serialVersionUID = 7661939850705259125L;
    private int startPathId;
    private int currentPathId;
    private int targetPathId;


    public GamePiece() {

        currentPathId = 300;
        startPathId = currentPathId;
    }

    public boolean isOnLand() {
        if (currentPathId == 400) {
            return true;
        } else {
            return false;
        }
    }

    public void setCurrentPathId(int currentPathId) { this.currentPathId = currentPathId; }

    public int getCurrentPathId() { return currentPathId; }

}

