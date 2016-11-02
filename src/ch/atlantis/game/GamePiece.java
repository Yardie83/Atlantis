package ch.atlantis.game;

import javafx.scene.shape.Rectangle;

import java.io.Serializable;

/**
 * Created by Hermann Grieder on 07.10.2016.
 */
public class GamePiece extends Rectangle implements Serializable {

    private static final long serialVersionUID = 7661939850705259125L;
    int pathIdOfGamePiece = 0;

    public void setPathIdOfGamePiece(int pathIdOfGamePiece) { this.pathIdOfGamePiece = pathIdOfGamePiece; }

    public int getPathIdOfGamePiece() { return pathIdOfGamePiece; }

}

