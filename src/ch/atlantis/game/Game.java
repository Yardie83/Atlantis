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

    public Game( String gameName, int numberOfPlayers, Player player ) {
        this.gameName = gameName;
        this.numberOfPlayers = numberOfPlayers;
        players = new ArrayList<>();
        players.add( player );
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
}
