package ch.atlantis.game;

import java.util.ArrayList;

/**
 * Created by Hermann Grieder on 07.10.2016.
 *
 * Single Game instance
 */
public class Game {

    private String gameName;
    private ArrayList<Player> players;
    private int numberOfPlayers;
    private boolean isReady;

    public Game( String gameName, int numberOfPlayers) {

        this.gameName = gameName;
        this.isReady = false;
        this.numberOfPlayers = numberOfPlayers;
        this.players = new ArrayList<>();
    }

    public void addPlayer(Player player){
        players.add( player );
    }

    public void removePlayer(Player player){
        players.remove( player );
    }

    public String getGameName() {
        return gameName;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int getNumberOfPlayers(){
        return numberOfPlayers;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady( boolean ready ) {
        isReady = ready;
    }
}
