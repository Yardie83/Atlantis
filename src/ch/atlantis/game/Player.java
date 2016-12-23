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
    private ArrayList<Card> pathCardStack;
    private String playerName;
    private String gameName;
    private int playerID;
    private int score;
    private int gamePiecesOnLand;

    public Player(String playerName, int playerID, String gameName) {
        this.playerName = playerName;
        this.playerID = playerID;
        this.gameName = gameName;

        this.movementCards = new ArrayList<>();
        this.gamePieces = new ArrayList<>(3);
        this.pathCardStack = new ArrayList<>();
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

    public int getGamePiecesOnLand() { return gamePiecesOnLand; }

    public void setGamePiecesOnLand(int gamePiecesOnLand) { this.gamePiecesOnLand = gamePiecesOnLand; }

    public ArrayList<Card> getPathCardStack() { return pathCardStack; }

    public void setScore(int score) { this.score = score; }
}

