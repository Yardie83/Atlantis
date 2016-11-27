package ch.atlantis.game;

import javafx.scene.shape.Rectangle;

import java.io.Serializable;

/**
 * Created by Hermann Grieder on 07.10.2016.
 */
public class GamePiece extends Rectangle implements Serializable {

    private static final long serialVersionUID = 7661939850705259125L;
    private int pathIdOfGamePiece;
    private int gamePiecePlayerId;
    private double x;
    private double y;

    public GamePiece() {

    }

    public void moveGamePiece(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getGamePieceX() { return x; }

    public double getGamePieceY() { return y; }

    public void setPathIdOfGamePiece(int pathIdOfGamePiece) { this.pathIdOfGamePiece = pathIdOfGamePiece; }

    public int getPathIdOfGamePiece() { return pathIdOfGamePiece; }

    public int getGamePiecePlayerId() { return gamePiecePlayerId; }

}

