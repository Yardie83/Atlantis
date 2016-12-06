package ch.atlantis.game;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Hermann Grieder on 28.10.2016.
 */
public class GameModel {

    private ArrayList<Player> players;
    private ArrayList<Tile> tiles;
    private final ArrayList<Card> pathCards;
    private ArrayList<Card> movementCards;
    private ArrayList<Card> deck;
    private int currentTurnLocal;
    private int currentTurnRemote;
    private Card selectedCard;
    private int selectedGamePieceIndex;
    private int activePlayerId;
    private int targetPathIdRemote;
    private int targetPathId;

    public GameModel() {

        players = new ArrayList<>();
        tiles = new ArrayList<>();
        currentTurnLocal = 0;

        ArrayList<Card> pathCardsSetA = new ArrayList<>();
        createPathCards(pathCardsSetA);
        cleanCardSetA(pathCardsSetA);
        Collections.shuffle(pathCardsSetA);

        ArrayList<Card> pathCardsSetB = new ArrayList<>();
        createPathCards(pathCardsSetB);
        cleanCardSetB(pathCardsSetB);
        Collections.shuffle(pathCardsSetB);

        movementCards = new ArrayList<>();
        createMovementCards();
        Collections.shuffle(movementCards);

        deck = new ArrayList<>();

        readLayout();
        pathCards = new ArrayList<>();
        placeCards(pathCardsSetA, pathCardsSetB);

    }

    private void createMovementCards() {
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 15; j++) {
                this.movementCards.add(new Card(i, CardType.MOVEMENT));
            }
        }
    }

    /**
     * Fabian Witschi
     * <br>
     * Cleans the Path Card Set A.
     *
     * @param pathCardsSetA
     */
    private void cleanCardSetA(ArrayList<Card> pathCardsSetA) {
        for (int i = 0; i < pathCardsSetA.size(); i++) {
            Card card = pathCardsSetA.get(i);
            int value = card.getValue();
            int colorSet = card.getColorSet();
            int index;
            if (value == 7) {
                if (colorSet == Card.GREY || colorSet == Card.YELLOW || colorSet == Card.BLUE || colorSet == Card.WHITE) {
                    index = pathCardsSetA.indexOf(card);
                    pathCardsSetA.remove(index);
                }
            } else if (value == 6) {
                if (colorSet == Card.BROWN || colorSet == Card.PINK || colorSet == Card.GREEN) {
                    index = pathCardsSetA.indexOf(card);
                    pathCardsSetA.remove(index);
                }
            }
        }
    }

    /**
     * Fabian Witschi
     * <br>
     * Cleans the Path Card Set B.
     *
     * @param pathCardsSetB
     */
    private void cleanCardSetB(ArrayList<Card> pathCardsSetB) {
        for (int i = 0; i < pathCardsSetB.size(); i++) {
            Card card = pathCardsSetB.get(i);
            int value = card.getValue();
            int colorSet = card.getColorSet();
            int index;
            if (value == 7) {
                if (colorSet == Card.BROWN || colorSet == Card.PINK || colorSet == Card.GREEN) {
                    index = pathCardsSetB.indexOf(card);
                    pathCardsSetB.remove(index);
                }
            } else if (value == 6) {
                if (colorSet == Card.GREY || colorSet == Card.YELLOW || colorSet == Card.BLUE || colorSet == Card.WHITE) {
                    index = pathCardsSetB.indexOf(card);
                    pathCardsSetB.remove(index);
                }
            }
        }
    }

    private void createPathCards(ArrayList<Card> pathCardsSet) {
        for (int j = 0; j < 7; j++) {
            for (int k = 1; k <= 7; k++) {
                pathCardsSet.add(new Card(j, k, CardType.PATH));
            }
        }
    }

    /**
     * Hermann Grieder
     * <br>
     * Reads a random layout file and transfers the values into the values array
     * <p>
     * Author: Hermann Grieder
     */
    private void readLayout() {
        Random rand = new Random();
        int layoutId = rand.nextInt(2);
        System.out.println(layoutId);
        try {
            BufferedReader bf = new BufferedReader(new FileReader("src/ch/atlantis/res/Layout_" + String.valueOf(layoutId) + ".txt"));

            String currentLine;
            int y = -1;

            try {
                while ((currentLine = bf.readLine()) != null) {
                    y++;
                    String[] values = currentLine.trim().split(" ");
                    for (int x = 0; x < values.length; x++) {

                        int pathId = Integer.parseInt(values[x]);

                        tiles.add(new Tile(x, y, pathId));
                    }
                }
                bf.close();
            } catch (IOException e) {
                System.out.println("Empty Line!");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File \"Layout_0.txt\" not found!");
        }
    }

    private void placeCards(ArrayList<Card> pathCardsSetA, ArrayList<Card> pathCardsSetB) {

        Iterator<Card> iteratorA = pathCardsSetA.iterator();
        Iterator<Card> iteratorB = pathCardsSetB.iterator();

        for (Tile tile : tiles) {

            int pathId = tile.getPathId();

            //Fill the path with water cards before adding the pathCards
            if (pathId >= 101 && pathId <= 153) {
                placeSpecialCard(Card.BLUE, CardType.WATER, tile);
            }

            if (pathId != 0 && pathId != 500) {

                // Place two cards from 101 to 110 and from 120 to 126
                // Place one card from 111 to 120 from Card set A
                if (pathId <= 126) {
                    if (!(pathId >= 111 && pathId <= 120)) {
                        placeTwoCards(iteratorA, tile);
                    } else {
                        placeOneCard(iteratorA, tile);
                    }
                }

                // Place two cards from 128 to 133 and from 144 to 153
                // Place one card from 134 to 143 from Card set B
                else if (pathId >= 128 && pathId <= 154) {
                    if (!(pathId >= 134 && pathId <= 143)) {
                        placeTwoCards(iteratorB, tile);
                    } else {
                        placeOneCard(iteratorB, tile);
                    }
                }
                //Start card
                else if (pathId == 300) {
                    placeSpecialCard(Card.YELLOW, CardType.START, tile);
                }
                //End card
                else if (pathId == 400) {
                    placeSpecialCard(Card.GREEN, CardType.END, tile);
                }
            }
        }
    }

    private void placeSpecialCard(int colorSet, CardType cardType, Tile tile) {
        Card card = new Card(colorSet, cardType);
        card.setIsOnTop(true);
        setIdAndAddCard(card, tile);
    }

    private void placeOneCard(Iterator<Card> iterator, Tile tile) {
        Card card = iterator.next();
        card.setIsOnTop(true);
        setIdAndAddCard(card, tile);
    }

    private void placeTwoCards(Iterator<Card> iterator, Tile tile) {
        Card card;
        for (int i = 0; i < 2; i++) {
            card = iterator.next();
            if (i == 0) {
                card.setIsOnTop(false);
            } else {
                card.setIsOnTop(true);
            }
            setIdAndAddCard(card, tile);
        }
    }

    /**
     * Hermann Grieder
     * <br>
     * Gives the card a pathId according to the tile path id
     *
     * @param card Card for which to set the path id
     * @param tile Tile which holds the path id that should be assigned to the card
     */
    private void setIdAndAddCard(Card card, Tile tile) {
        card.setPathId(tile.getPathId());
        pathCards.add(card);
    }

    /**
     * Hermann Grieder
     * <br>
     * Checks if the game is over by checking if there are still gamePieces on the way to the end
     *
     * @return True if the game is over
     */
    public boolean isGameOver() {
        boolean stillPlaying = false;
        for (Player player : players) {
            for (GamePiece gamePiece : player.getGamePieces()) {
                if (gamePiece.getCurrentPathId() != 400) {
                    stillPlaying = true;
                }
            }
        }
        return !stillPlaying;
    }

    public boolean handleMove() {

        // Check if the games have the same turn number
        if (this.currentTurnLocal != currentTurnRemote) {
            System.out.println("GameModel -> PlayerTurns do not match\nthis.PlayerTurn:" + this.currentTurnLocal + "PlayerTurn: " + currentTurnRemote);
            return false;
        }
        // Check if the message came from the right player
        if (this.currentTurnLocal != activePlayerId) {
            System.out.println("GameModel -> PlayerTurn and PlayerID do not match\nthis.PlayerTurn:" + this.currentTurnLocal + "PlayerId: " + activePlayerId);
            return false;
        }

        // TODO: Act on move according to the comment below
        // If the move is valid we have to remove the movement card played from the player, add the appropriate amount of
        // movement cards to his hand from the deck, update the game piece path id, increase the currentTurnLocal by 1 and
        // update the score
        GamePiece gamePiece = players.get(activePlayerId).getGamePieces().get(selectedGamePieceIndex);
        // Find the pathId on the server side
        int targetPathId = findTargetPathId(gamePiece);
        // Check if the targetPathId matches. If it does, set the new pathId
        if (targetPathId != targetPathIdRemote) {
            System.out.println("GameModel - > TargetPathIds do not match");
            System.out.println("LocalTargetPathId: " + targetPathId + "\nRemoteTargetPathId: " + targetPathIdRemote);
            return false;
        } else {
            System.out.println("GameModel - > TargetPathIds match");
            System.out.println("LocalTargetPathId: " + targetPathId + "\nRemoteTargetPathId: " + targetPathIdRemote);
            players.get(activePlayerId).getGamePieces().get(selectedGamePieceIndex).setCurrentPathId(targetPathId);
        }

        currentTurnLocal++;
        if (currentTurnLocal >= players.size()) {
            currentTurnLocal = 0;
        }
        System.out.println("GameModel -> PlayerTurn: " + this.currentTurnLocal);
        return true;
    }

    public int findTargetPathId(GamePiece activeGamePiece) {

        int startPathId = 101;
        if (activeGamePiece.getCurrentPathId() != 300) {
            startPathId = activeGamePiece.getCurrentPathId() + 1;
        }

        boolean found = false;
        int nextPathId = startPathId;
        targetPathId = 0;
        while (!found && nextPathId < 154) {
            for (Card pathCard : pathCards) {
                if (pathCard.isOnTop()
                        && pathCard.getCardType() != CardType.WATER
                        && pathCard.getPathId() == nextPathId
                        && pathCard.getColorSet() == selectedCard.getColorSet()) {
                    found = true;
                    targetPathId = pathCard.getPathId();
                }
            }
            nextPathId++;
        }
        return targetPathId;
    }

    /**
     * Fabian Witschi
     * <br>
     * <p>
     * Finds the price that needs to be paid to cross one or more water tiles
     *
     * @param pathId The current pathId of the gamePiece that was moved there
     * @return The price to cross
     */
    private int getPriceForCrossing(int pathId) {
        int pathIdBehind = pathId - 1;
        int pathIdAfter = pathId + 1;
        int valueBehind = 0;
        int valueAfter = 0;

        for (Card pathCard : pathCards) {

            if (pathIdBehind >= 101 && pathIdAfter <= 154) {

                if (pathCard.getCardType() != CardType.WATER) {

                    if (pathCard.getPathId() == pathIdBehind) {
                        valueBehind = pathCard.getValue();
                    }
                    if (pathCard.getPathId() == pathIdAfter) {
                        valueAfter = pathCard.getValue();
                    }
                } else {
                    if (pathCard.getPathId() == pathIdBehind) {
                        getPriceForCrossing(pathIdBehind--);
                    }
                    if (pathCard.getPathId() == pathIdAfter) {
                        getPriceForCrossing(pathIdAfter++);
                    }
                }
            }
        }
        if (valueBehind > valueAfter) {
            return valueAfter;
        } else {
            return valueBehind;
        }
    }

    /**
     * Finalizes the game. The game is created before all the players are known.
     *
     * @return The initial game state HashMap
     */
    public HashMap<String, Object> init() {
        addMovementCardsToPlayers(movementCards);
        return writeInitialGameStateMap();
    }

    /**
     * Fabian Witschi
     * <p>
     * Gives each player their initial movement cards. Player 1 gets 4, Player 2 gets 5 etc.
     *
     * @param movementCards ArrayList of all the movement cards
     */
    private void addMovementCardsToPlayers(ArrayList<Card> movementCards) {

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            for (int k = 0; k < 4 + i; k++) {
                player.addMovementCard(movementCards.get(0));
                movementCards.remove(0);
            }
        }
        // The rest of the cards go into the deck
        deck = movementCards;
    }

    /**
     * Hermann Grieder
     * <br>
     * Reads the incoming Game State Map
     *
     * @param gameStateMap The incoming HashMap
     */
    public void readGameStateMap(HashMap<String, Object> gameStateMap) {
        currentTurnRemote = (int) gameStateMap.get("CurrentTurn");
        activePlayerId = (int) gameStateMap.get("PlayerId");
        selectedCard = (Card) gameStateMap.get("Card");
        selectedGamePieceIndex = (int) gameStateMap.get("GamePieceIndex");
        targetPathIdRemote = (int) gameStateMap.get("TargetPathId");
    }

    /**
     * Hermann Grieder
     * <br>
     * Writes the initial Game State Map. Which holds different information than
     * what is needed during the actual gameplay.
     *
     * @return HashMap
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, Object> writeInitialGameStateMap() {

        HashMap<String, Object> gameStateMap = new HashMap<>();
        gameStateMap.put("CurrentTurn", currentTurnLocal);
        gameStateMap.put("Players", players);
        gameStateMap.put("Tiles", tiles);
        gameStateMap.put("PathCards", pathCards);
        return gameStateMap;
    }

    /**
     * Hermann Grieder
     * <br>
     * Writes the Game State Map with the needed information during the game
     *
     * @return HashMap
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, Object> writeGameStateMap() {
        HashMap<String, Object> gameStateMap = new HashMap<>();
        gameStateMap.put("CurrentTurn", currentTurnLocal);
        gameStateMap.put("Players", players);
        gameStateMap.put("CardUsed", selectedCard);
        gameStateMap.put("GamePieceUsedIndex", selectedGamePieceIndex);
        gameStateMap.put("TargetPathId", targetPathId);
        return gameStateMap;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void remove(Player player) {
        players.remove(player);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

}

