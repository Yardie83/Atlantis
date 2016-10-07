package ch.atlantis.game;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Hermann Grieder on 23.08.16.
 * <p>
 * Handles all game related functions, like adding players to a game, adding created games
 * to a gameList. Also creates a new game instance.
 */

public class GameHandler {
    private ArrayList<Game> games;



    public GameHandler() {
        games = new ArrayList<>();

    }

    public void addGame( Game game ) {
        game.getPlayers().get( 0 ).setPlayerId( 0 );
        games.add( game );
    }

    public void removeGame( String gameName ) {
        for ( Game g : games ) {
            if ( g.getGameName().equals( gameName ) ) {
                games.remove( g );
            }
        }
    }

    public ArrayList<Game> getGames(){
        return games;
    }

    public boolean addPlayer( String gameName, String playerName ) {
        for ( Game g : games ) {
            if ( g.getGameName().equals( gameName ) && g.getPlayers().size() < g.getNumberOfPlayers() ) {
                Player p = new Player( playerName );
                p.setPlayerId( g.getPlayers().size()+1 );
                p.setPlayerColor( p.getPlayerID() );
                g.getPlayers().add( p );
                return true;
            }
        }
        return false;
    }
}