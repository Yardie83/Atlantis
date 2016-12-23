package ch.atlantis.game;

import javafx.scene.shape.Circle;

import java.io.Serializable;

/**
 * Created by Hermann Grieder on 07.10.2016.
 * <br>
 * * A gamePiece extends a JavaFx circle and holds a currentPathId, x and y coordinates.
 */
public class GamePiece extends Circle implements Serializable {

    private static final long serialVersionUID = 7661939850705259125L;
    private int currentPathId;

    public GamePiece() {
        currentPathId = 300;
    }

    public boolean isOnLand() {
        return currentPathId == 400;
    }

    public void setCurrentPathId(int currentPathId) {
        this.currentPathId = currentPathId;
    }

    public int getCurrentPathId() {
        return currentPathId;
    }

}

