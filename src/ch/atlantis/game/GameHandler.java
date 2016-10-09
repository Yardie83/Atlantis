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
    private int playerId;


    public GameHandler() {
        games = new ArrayList<>();

    }

    public void addGame( String gameName, Integer nrOfPlayers ) {
        playerId = 0;
        // If the game already exists, do nothing
        for ( Game g : games ) {
            if ( g.getGameName().equals( gameName ) ) {
                return;
            }
        }

        // If the game does not exist, create it and add it to the list of games
        Game game = new Game( gameName, nrOfPlayers );
        games.add( game );
    }

    public ArrayList<Game> getGames() {
        return games;
    }

    public void addPlayer( String gameName, String playerName ) {
        Player playerToRemove = null;
        Game gameToRemovePlayerFrom = null;
        Game gameToAddPlayerTo = null;

        //Find the Game to which we want to add the player to.
        for ( Game g : games ) {
            for ( Player p : g.getPlayers() ) {
                // Check if the player tried to join the same game twice
                if ( p.getPlayerName().equals( playerName ) && g.getGameName().equals( gameName ) ) {
                    return;
                }
                // Check if the player has already joined another game, if so remove the player form that game
                if ( p.getPlayerName().equals( playerName ) && !( g.getGameName().equals( gameName ) ) ) {
                    playerToRemove = p;
                    gameToRemovePlayerFrom = g;
                }
            }
            // If none of the above applies and the game is not full, then this is the game we wanted to find.
            if ( g.getGameName().equals( gameName ) && g.getPlayers().size() < g.getNumberOfPlayers() ) {
                gameToAddPlayerTo = g;
            }
        }
        //Remove the player from the other game
        if(playerToRemove != null){
            gameToRemovePlayerFrom.removePlayer( playerToRemove );
        }
        // If we found a game to add the player to, then create a new player and add the player to that game.
        if ( gameToAddPlayerTo != null ) {
            Player player = new Player( playerName );
            player.setPlayerId( playerId++ );
            player.setPlayerColor( player.getPlayerID() );
            gameToAddPlayerTo.addPlayer( player );
        }
    }
}