package ch.atlantis.game;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Hermann Grieder on 15/08/16.
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 7661939850705259874L;
    private ArrayList<GamePiece> gamePieces;
    private ArrayList<Card> movementCards;
    private String playerName;
    private String gameName;
    private int playerID;
    private int bridge;
    private int score;

    public Player(String playerName, int playerID, String gameName) {
        this.playerName = playerName;
        this.playerID = playerID;
        this.gameName = gameName;

        this.movementCards = new ArrayList<>();
        this.gamePieces = new ArrayList<>(4);
        this.bridge = 1;
        addGamePiece();
    }

    private void addGamePiece() {

        for (int i = 0; i < 3; i++) {
            gamePieces.add(new GamePiece());
        }

    }

    public String getPlayerName() {
        return playerName;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void subtractScore(int score) {
        this.score -= score;
    }

    public int getScore() {
        return score;
    }

    public ArrayList<GamePiece> getGamePieces() {
        return gamePieces;
    }

    public void removeBridge() {
        this.bridge = 0;
    }

    public int getBridge() {
        return bridge;
    }

    public ArrayList<Card> getMovementCards() {
        return movementCards;
    }

    public void addMovementCard(Card movementCard) {
        this.movementCards.add(movementCard);
    }

    public int getPlayerID() {
        return playerID;
    }

    public String getGameName() {
        return gameName;
    }
}

