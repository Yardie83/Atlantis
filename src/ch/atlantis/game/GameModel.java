package ch.atlantis.game;

import java.util.ArrayList;

/**
 * Created by Hermann Grieder on 28.10.2016.
 *
 */
public class GameModel {
    private ArrayList<Player> players;

    public GameModel() {
        this.players = new ArrayList<>();
    }


    public void init() {

    }



    public void addPlayer(Player player) {
        players.add( player );
    }

    public void remove( Player player ) {
        players.remove( player );
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }


}
