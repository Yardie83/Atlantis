package ch.atlantis.game;

import ch.atlantis.util.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hermann Grieder on 23.08.16.
 * <p>
 * Handles all game related functions, like adding players to a game, adding created games
 * to a gameList. Also creates a new game instance.
 */

public class GameHandler {
    private HashMap<String, Game> games;
    private int playerId;


    public GameHandler() {
        games = new HashMap<>();
    }

    /**
     * Extracts the information from the message. Creates a new game and
     * adds it to the gameHandler. Then adds the Player who sent the message
     * as the first player (with the playerId = 0) to the game
     * <p>
     * Hermann Grieder
     *
     * @param message Message received from the client
     */
    public boolean handleNewGame( Message message, String currentPlayerName) throws IOException {

        // Extract the information from the message
        String[] gameInformation = message.getMessageObject().toString().split( "," );
        String gameName = gameInformation[ 0 ];
        Integer nrOfPlayers = Integer.parseInt( gameInformation[ 1 ] );

        // Create and add a new game to the games ArrayList in
        // the GameHandler with the above game information
        if ( addGame( gameName, nrOfPlayers ) ) {
            System.out.println("Game: " + gameName + " added");
            return true;
        }
        return false;
    }

    private boolean addGame( String gameName, Integer nrOfPlayers ) {
        playerId = 0;
        // If the game already exists, do nothing
        if ( games.containsKey( gameName ) ) {
            return false;
        } else {
            // If the game does not exist, create it and add it to the list of games
            Game game = new Game( gameName, nrOfPlayers );
            games.put( gameName, game );
            return true;
        }
    }

    public boolean isGameFull( String gameName ) {
        Game g = games.get( gameName );
        if ( g.getPlayers().size() == g.getNumberOfPlayers() ) {
            g.setReady( true );
            return g.isReady();
        }
        return false;
    }

    public Player addPlayer( String gameName, String currentPlayerName) {
        System.out.println("Adding Player: " + currentPlayerName);
        Player player = new Player( currentPlayerName);

        Player playerToRemove = null;
        Game gameToRemovePlayerFrom = null;
        Game gameToAddPlayerTo = null;

        // Before adding the player we have to check various things first
        for ( HashMap.Entry<String, Game> entry : games.entrySet() ) {
            Game g = entry.getValue();
            for ( Player p : g.getPlayers() ) {
                // Check if the player tried to join the same game twice
                if ( p.getPlayerName().equals( currentPlayerName ) && g.getGameName().equals( gameName ) ) {
                    return null;
                }
                // Check if the player has already joined another game, if so remove the player form that game
                if ( p.getPlayerName().equals( currentPlayerName ) && !( g.getGameName().equals( gameName ) ) ) {
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
        if ( playerToRemove != null ) {
            gameToRemovePlayerFrom.removePlayer( playerToRemove );
        }
        //Now that we checked everything, we can add the player to the game
        //
        // If we found a game to add the player to, then create a new player and add the player to that game.
        if ( gameToAddPlayerTo != null ) {
            System.out.println("Player with ID: " + playerId + " added");
            player.setPlayerId( playerId++ );
            player.setGameName(gameName);
            player.setPlayerColor( player.getPlayerID() );
            gameToAddPlayerTo.addPlayer( player );
            return player;
        }
        return null;
    }

    public HashMap<String, Game> getGames() {
        return games;
    }


    // Finish initializing the game
    public HashMap<String, ArrayList> initGame(Player hostPlayer) {
        Game game = games.get( hostPlayer.getGameName() );
        return game.init();

    }
}
