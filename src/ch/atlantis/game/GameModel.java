package ch.atlantis.game;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
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
    private int selectedCard;
    private int selectedGamePieceIndex;
    private int activePlayerId;
    private int targetPathIdRemote;
    private int targetPathId;
    private ArrayList<Card> discardedCards;
    private int indexOfCardToRemove;
    private int indexOfCardToShow;
    private Card newCardFromDeck;

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
        discardedCards = new ArrayList<>();

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
        boolean gameOver = true;
        for (Player player : players) {
            for (GamePiece gamePiece : player.getGamePieces()) {
                if (gamePiece.getCurrentPathId() != 400) {
                    gameOver = false;
                }
            }
        }
        System.out.println("GameModel -> GameOver: " + gameOver);
        return gameOver;
    }

    public boolean handleMove() {

        // Check if the remote and local game have the same turn number
        if (this.currentTurnLocal != currentTurnRemote) {
            System.out.println("GameModel -> PlayerTurns do not match\nthis.PlayerTurn:" + this.currentTurnLocal + "PlayerTurn: " + currentTurnRemote);
            return false;
        }
        // Check if the message came from the right player
        if (this.currentTurnLocal != activePlayerId) {
            System.out.println("GameModel -> PlayerTurn and PlayerID do not match\nthis.PlayerTurn:" + this.currentTurnLocal + "PlayerId: " + activePlayerId);
            return false;
        }

        GamePiece activeGamePiece = players.get(activePlayerId).getGamePieces().get(selectedGamePieceIndex);

        // Find the target pathId on the server side
        targetPathId = findTargetPathId(activeGamePiece);

        // Check if the target pathId is already occupied by someone else
        boolean targetIsOccupied = checkIfOccupied(targetPathId, activeGamePiece);

        // Check if there is water on the way to the target. Return the pathId of that water tile or 0 if not water is on the way
        int waterOnTheWayPathId = checkIfWaterOnTheWay(activeGamePiece.getCurrentPathId(), targetPathId);

        // If there is water on the way to the target then calculate the price to cross
        int priceToCrossWater = 0;
        if (waterOnTheWayPathId != 0) {
            priceToCrossWater = getPriceForCrossing(waterOnTheWayPathId);
            return false;
        }

        // Set the targetPathId as the currentPathId in the active gamePiece
        players.get(activePlayerId).getGamePieces().get(selectedGamePieceIndex).setCurrentPathId(targetPathId);

        // Remove the movement card played by the player and add it to the discarded cards list
        Card cardToDiscard = players.get(activePlayerId).getMovementCards().get(selectedCard);
        players.get(activePlayerId).getMovementCards().remove(selectedCard);
        discardedCards.add(cardToDiscard);
        System.out.println("GameModel -> Movement card removed");
        System.out.println("GameModel -> Player holds " + players.get(activePlayerId).getMovementCards().size() + " cards");

        // Pick up the card behind the gamePiece
        int scoreToAdd = removePathCardFromPath(targetPathId);
        // TODO: We need a list for the individuals score picked up by the player. So we can later pay with it.
        // Add the score of that card to the player
        players.get(activePlayerId).addScore(scoreToAdd);
        System.out.println("GameModel -> Score of " + scoreToAdd + " added to " + players.get(activePlayerId).getPlayerName());

        // Give the player new movement cards. The amount of cards the player played, plus for each GamePiece
        // that has reached the end, one additional card
        addCardFromDeckToPlayer();
        System.out.println("GameModel -> Player holds " + players.get(activePlayerId).getMovementCards().size() + " cards");

        // Increase the turn count
        currentTurnLocal++;
        if (currentTurnLocal >= players.size()) {
            currentTurnLocal = 0;
        }
        System.out.println("GameModel -> PlayerTurn: " + this.currentTurnLocal);
        return true;
    }

    /**
     * Hermann Grieder
     * <br>
     * Finds and returns the pathId to which the gamePiece should be move to ultimately.
     *
     * @param activeGamePiece The gamePiece that was moved
     * @return The targetPathId found
     */
    private int findTargetPathId(GamePiece activeGamePiece) {

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
                        && pathCard.getColorSet() == players.get(activePlayerId).getMovementCards().get(selectedCard).getColorSet()) {
                    found = true;
                    targetPathId = pathCard.getPathId();
                }
            }
            nextPathId++;
        }
        // If we cannot find a targetPathId on the path then the next target is the end
        if (!found && nextPathId + 1 == 154) {
            targetPathId = 400;
        }
        // Check if the targetPathIds match. If they do, set the new pathId
        if (targetPathId != targetPathIdRemote) {
            System.out.println("GameModel -> TargetPathIds do not match");
            System.out.println("LocalTargetPathId: " + targetPathId + "\nRemoteTargetPathId: " + targetPathIdRemote);
            return 0;
        } else {
            System.out.println("GameModel -> TargetPathIds match");
            System.out.println("LocalTargetPathId: " + targetPathId + "\nRemoteTargetPathId: " + targetPathIdRemote);
            return targetPathId;
        }
    }

    /**
     * Checks if the targetPathId that was found is already occupied.
     *
     * @param targetPathId    The pathId the gamePiece should be moved to
     * @param activeGamePiece The gamePiece that was moved
     * @return True if the target is occupied, false if it is free to go to
     */
    private boolean checkIfOccupied(int targetPathId, GamePiece activeGamePiece) {
        for (Player player : players) {
            for (GamePiece gamePiece : player.getGamePieces()) {
                if (gamePiece != activeGamePiece && gamePiece.getCurrentPathId() == targetPathId) {
                    System.out.println("GameModel -> TargetPathId is occupied");
                    return true;
                }
            }
        }
        System.out.println("GameModel -> TargetPathId is not occupied");
        return false;
    }

    /**
     * Hermann Grieder
     * <br>
     * Recursive method that goes trough each pathCard on the way
     * to the target to check if there is water on the way
     *
     * @param currentPathId The pathId of the GamePiece to be moved
     * @param targetPathId  The targetPathId where the GamePiece ultimately should be
     * @return The pathId of the water tile
     */
    private int checkIfWaterOnTheWay(int currentPathId, int targetPathId) {
        int startPathId = currentPathId + 1;

        // If the gamePiece is on the home tile we need to check from the first actual path tile
        if (currentPathId == 300) {
            startPathId = 101;
        }
        // If our target would be the end tile we have to check up to the last path card on the way
        int target = targetPathId;
        if (target == 400) {
            target = 153;
        }
        int count = 0;
        // We count the cards that have the same pathId as the startPathId. If we count more than one
        // card it must be an exposed water tile.
        if (startPathId < target) {
            for (Card pathCard : pathCards) {
                if (pathCard.getPathId() == startPathId) {
                    count++;
                }
            }
            // Recursive call in case we find more than one card on that pathId.
            if (count != 1) {
                System.out.println("GameModel - > There are " + count + " cards at " + startPathId);
                return checkIfWaterOnTheWay(startPathId, targetPathId);
            }
        }
        // If by the time we get to the target path and have not found any water tiles we return 0
        if (startPathId == targetPathId) {
            System.out.println("No water found to the target");
            return 0;
        }
        System.out.println("Water on PathId: " + startPathId);
        return startPathId;
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

    // FIXME: 06.12.2016 : This produces a stackOverflow exception.
    private int getPriceForCrossing(int pathId) {
        int valueBehind = getValueFromCardBehind(pathId);
        int valueAfter = getValueFromCardAfter(pathId);
        if (valueBehind > valueAfter) {
            System.out.println("GameModel -> Price to cross: " + valueAfter);
            return valueAfter;
        } else {
            System.out.println("GameModel -> Price to cross: " + valueBehind);
            return valueBehind;
        }
    }

    // Fabian
    private int getValueFromCardAfter(int pathId) {
        int valueOfCard = 0;
        int pathIdAfter = pathId + 1;
        ArrayList<Card> tempList = new ArrayList<>();

        if (pathIdAfter < 154) {
            for (Card pathCard : pathCards) {
                if (pathCard.getPathId() == pathIdAfter) {
                    tempList.add(pathCard);
                }
            }
            if (tempList.size() > 1) {
                for (Card pathCard : tempList) {
                    if (pathCard.getCardType() != CardType.WATER && pathCard.isOnTop()) {
                        valueOfCard = pathCard.getValue();
                    }
                }
            } else {
                getValueFromCardAfter(pathIdAfter);
            }
        }

        return valueOfCard;
    }

    // Fabian
    private int getValueFromCardBehind(int pathId) {
        int pathIdBehind = pathId - 1;
        int valueOfCardBehind = 0;
        for (Card pathCard : pathCards) {
            if (pathCard.getPathId() == pathIdBehind) {
                if (pathCard.getCardType() != CardType.WATER && pathCard.isOnTop()) {
                    valueOfCardBehind = pathCard.getValue();
                }
            }
        }
        return valueOfCardBehind;
    }

    /**
     * Hermann Grieder
     * <br>
     * Removes the pathCard at the index behind the targetPathId. In case that there is water behind, the mehtod
     * will keep searching for the first pathCard behind the player and water.
     *
     * @param targetPathId The pathId from where we pick up a card behind the player
     * @return The value of the pathCard that has been removed
     */
    private int removePathCardFromPath(int targetPathId) {
        int startPathId = targetPathId - 1;

        // If the gamePiece is on the first tile we return 0, as we do not pick up a card with value
        if (targetPathId == 101) {
            return 0;
        }

        // If our target is on the end tile we start from the tile before
        if (targetPathId == 400) {
            startPathId = 153;
        }

        // We remove the card behind the player and return the score. If we count less than two
        // cards it must be an exposed water tile. Otherwise we remove the found card and in case there is
        // another card underneath we will set it to be on top for the next time.
        int score = 0;
        ArrayList<Card> foundCardsAtPathId = new ArrayList<>();
        Card cardToRemove = null;
        Card cardToShow = null;

        for (Card pathCard : pathCards) {
            if (pathCard.getPathId() == startPathId) {
                foundCardsAtPathId.add(pathCard);
            }
        }
        // Recursive call in case we find less than two cards which means there is only water. So we continue our search
        if (foundCardsAtPathId.size() == 1) {
            System.out.println("GameModel - > There is only " + foundCardsAtPathId.size() + " card at " + startPathId);
            return removePathCardFromPath(startPathId);
        }

        for (Card card : foundCardsAtPathId) {
            if (card.getCardType() != CardType.WATER) {
                if (card.isOnTop()) {
                    cardToRemove = card;
                    score = cardToRemove.getValue();
                } else {
                    cardToShow = card;
                }
            }
        }

        if (cardToRemove != null) {
            indexOfCardToRemove = pathCards.indexOf(cardToRemove);
            pathCards.get(indexOfCardToRemove).setPathId(-1);
        }
        if (cardToShow != null) {
            indexOfCardToShow = pathCards.indexOf(cardToShow);
            pathCards.get(indexOfCardToShow).setIsOnTop(true);
        }
        System.out.println("GameModel -> Score from PathId: " + score);
        return score;
    }

    private void addCardFromDeckToPlayer() {
        if (deck.size() == 0) {
            deck = discardedCards;
            Collections.shuffle(deck);
        }
        newCardFromDeck = deck.get(0);
        players.get(activePlayerId).getMovementCards().add(newCardFromDeck);
        System.out.println("GameModel -> Card: " + newCardFromDeck.getColorSet());
        deck.remove(0);
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
        selectedCard = (int) gameStateMap.get("SelectedCard");
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
        gameStateMap.put("SelectedCard", selectedCard);
        gameStateMap.put("GamePieceUsedIndex", selectedGamePieceIndex);
        gameStateMap.put("TargetPathId", targetPathId);
        gameStateMap.put("IndexOfCardToRemove", indexOfCardToRemove);
        gameStateMap.put("IndexOfCardToShow", indexOfCardToShow);
        gameStateMap.put("DeckCard", newCardFromDeck);

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

