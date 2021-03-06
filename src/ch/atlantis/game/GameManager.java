package ch.atlantis.game;

import ch.atlantis.server.AtlantisServer;
import ch.atlantis.util.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by Hermann Grieder on 23.08.16.
 * <p>
 * Manages all games. Creates new games, adds players to it and like adding players to a game, adding created games
 * to a gameList. Also creates a new game instance.
 */

public class GameManager {

    private HashMap<String, Game> games;

    private Logger logger;

    public GameManager() {

        logger = Logger.getLogger(AtlantisServer.AtlantisLogger);

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
    public boolean handleNewGame(Message message) throws IOException {

        // Extract the information from the message
        String[] gameInformation = message.getMessageObject().toString().split(",");
        String gameName = gameInformation[0];
        Integer nrOfPlayers = Integer.parseInt(gameInformation[1]);

        // Create and add a new game to the games ArrayList with the above game information.
        // If the game already exists, return false.
        if (games.containsKey(gameName)) {
            return false;
        } else {
            // If the game does not exist, create it and add it to the list of games
            Game game = new Game(gameName, nrOfPlayers);
            games.put(gameName, game);
            logger.info("Game: " + gameName + " added.");
            return true;
        }
    }

    /**
     * Hermann Grieder
     * <br>
     * Checks if the game is full
     *
     * @param gameName Name of the game to be checked
     * @return True if the game is full
     */
    public boolean isGameFull(String gameName) {
        Game g = games.get(gameName);
        if (g.getPlayers().size() == g.getNumberOfPlayers()) {
            g.setReady(true);
            return g.isReady();
        }
        return false;
    }

    /**
     * Hermann Grieder
     * <br>
     * When a user tries to join a game this method checks if and how it is possible.
     *
     * @param gameName          Name of the game
     * @param currentPlayerName Name of the player to be added
     * @return A new player object
     */
    public Player addPlayer(String gameName, String currentPlayerName) {
        logger.info("Adding Player: " + currentPlayerName);

        Player playerToRemove = null;
        Game gameToRemovePlayerFrom = null;
        Game gameToAddPlayerTo = null;

        // Before adding the player we have to check various things first
        for (HashMap.Entry<String, Game> entry : games.entrySet()) {
            Game g = entry.getValue();
            for (Player p : g.getPlayers()) {
                // Check if the player tried to join the same game twice
                if (p.getPlayerName().equals(currentPlayerName) && g.getGameName().equals(gameName)) {
                    return null;
                }
                // Check if the player has already joined another game, if so remove the player form that game
                if (p.getPlayerName().equals(currentPlayerName) && !(g.getGameName().equals(gameName))) {
                    playerToRemove = p;
                    gameToRemovePlayerFrom = g;
                }
            }
            // If none of the above applies and the game is not full, then this is the game we wanted to find.
            if (g.getGameName().equals(gameName) && g.getPlayers().size() < g.getNumberOfPlayers()) {
                gameToAddPlayerTo = g;
            }
        }
        //Remove the player from the other game
        if (playerToRemove != null) {
            gameToRemovePlayerFrom.removePlayer(playerToRemove);
        }
        //Now that we checked everything, we can add the player to the game

        // If we found a game to add the player to, then create a new player and add the player to that game.
        if (gameToAddPlayerTo != null) {
            int playerID = gameToAddPlayerTo.getPlayers().size();
            Player player = new Player(currentPlayerName, playerID, gameName);
            if (gameToAddPlayerTo.getPlayers().size() == 0) {
                gameToAddPlayerTo.setHost(player);
            }
            gameToAddPlayerTo.addPlayer(player);
            logger.info("Player with ID: " + playerID + " added.");
            return player;
        }
        return null;
    }

    /**
     * Hermann Grieder
     * <br>
     * Removes a player from a game
     *
     * @param currentPlayerName Name of the player which needs to be removed
     */
    public void removePlayer(String currentPlayerName) {
        for (HashMap.Entry<String, Game> entry : games.entrySet()) {
            Game g = entry.getValue();
            if (g.getHost().getPlayerName().equals(currentPlayerName)) {
                g.getPlayers().remove(0);
                g.setHost(null);
            }
        }
    }

    /**
     * Hermann Grieder
     * <br>
     * Calls the gameController handleMove method
     *
     * @param game         The game instance
     * @param gameStateMap The gameStateMap from the client
     * @return The new gameStateMap created after the move has been validated
     */
    public boolean handleMove(Game game, HashMap<String, Object> gameStateMap) {
        return game.getGameController().handleMove(gameStateMap);
    }


    /**
     * Finds the game name
     *
     * @param gameStateMap GameStateMap
     * @return Game name
     */
    public Game findGame(HashMap<String, Object> gameStateMap) {
        String gameName = (String) gameStateMap.get("GameName");
        return games.get(gameName);
    }

    /**
     * Hermann Grieder
     * <br>
     * Updates the gameList
     */
    public void updateGameList() {
        // Check for empty games or games without a host and remove them from the list
        if (games != null) {
            Game gameToRemove = null;
            for (HashMap.Entry<String, Game> entry : games.entrySet()) {
                Game g = entry.getValue();
                if (g.getPlayers().size() == 0 || g.getHost() == null) {
                    logger.info("Players in Game: " + g.getGameName() + ": " + g.getPlayers().size());
                    gameToRemove = g;
                }
            }
            if (gameToRemove != null) {
                logger.info("Number of Games: " + games.size());
                removeGame(gameToRemove);
                logger.info("Number of Games: " + games.size());
            }
        }
    }

    public void removeGame(Game game) {
        games.remove(game.getGameName());
    }

    public HashMap<String, Game> getGames() {
        return games;
    }


    // Finish initializing the game
    public HashMap<String, Object> initGame(Player hostPlayer) {
        Game game = games.get(hostPlayer.getGameName());
        return game.init();

    }

    public boolean isGameOver(Game game) {
        return game.getGameController().isGameOver();

    }

    public HashMap<String, Object> writeGameState(Game game) {
        return game.getGameController().writeGameState();
    }

}
