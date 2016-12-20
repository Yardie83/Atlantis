package ch.atlantis.game;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hermann Grieder on 07.10.2016.
 * <p>
 * Single Game instance with its own gameModel and controller
 */
public class Game {

    private String gameName;
    private int numberOfPlayers;
    private boolean isReady;
    private GameModel gameModel;
    private GameController gameController;
    private Player host;

    public Game(String gameName, int numberOfPlayers) {

        this.gameName = gameName;
        this.gameModel = new GameModel();
        this.gameController = new GameController(gameModel);
        this.isReady = false;
        this.numberOfPlayers = numberOfPlayers;
    }

    public void addPlayer(Player player) {
        gameModel.addPlayer(player);
    }

    public void removePlayer(Player player) {
        gameModel.remove(player);
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public String getGameName() {
        return gameName;
    }

    public HashMap<String, Object> init() {
        return gameModel.finalizeGame();
    }

    public GameController getGameController() {
        return gameController;
    }

    public ArrayList<Player> getPlayers() {
        return gameModel.getPlayers();
    }

    public void setHost(Player host) {
        this.host = host;
    }

    public Player getHost() {
        return host;
    }
}

