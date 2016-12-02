package ch.atlantis.game;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hermann Grieder on 07.10.2016.
 * <p>
 * Single Game instance
 */
public class Game {

    private String gameName;
    private int numberOfPlayers;
    private boolean isReady;
    private GameModel model;

    public Game(String gameName, int numberOfPlayers) {

        this.gameName = gameName;
        this.model = new GameModel();
        this.isReady = false;
        this.numberOfPlayers = numberOfPlayers;
    }

    public HashMap<String, ArrayList> init() {
        return model.init();
    }

    public void addPlayer(Player player) {
        model.addPlayer(player);
    }

    public void removePlayer(Player player) {
        model.remove(player);
    }

    public String getGameName() {
        return gameName;
    }

    public ArrayList<Player> getPlayers() {
        return model.getPlayers();
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

}

