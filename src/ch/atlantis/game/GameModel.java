package ch.atlantis.game;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Hermann Grieder on 28.10.2016.
 */
public class GameModel {

    private ArrayList<Player> players;
    private ArrayList<Tile> tiles;
    private ArrayList<Card> pathCards;
    private ArrayList<Integer> playedCardsIndices;
    private ArrayList<Integer> targetPathIdsRemote;
    private ArrayList<Card> discardedCards;
    private ArrayList<Card> movementCards;
    private ArrayList<Card> deck;
    private Card newCardFromDeck;
    private int currentTurnLocal;
    private int currentTurnRemote;
    private int selectedGamePieceIndex;
    private int activePlayerId;
    private int targetPathId;
    private int indexOfCardToRemove;
    private int indexOfCardToShow;

    public GameModel() {
        initGame();
    }

    // *************************  PRE-GAME METHODS ***************************************//

    /**
     * Hermann Grieder
     * <br>
     * Starts the initialisation of the needed game components for the game.
     */
    private void initGame() {
        players = new ArrayList<>();
        deck = new ArrayList<>();
        discardedCards = new ArrayList<>();

        ArrayList<Card> pathCardsSetA = createPathCards();
        cleanCardSetA(pathCardsSetA);
        Collections.shuffle(pathCardsSetA);

        ArrayList<Card> pathCardsSetB = createPathCards();
        cleanCardSetB(pathCardsSetB);
        Collections.shuffle(pathCardsSetB);

        movementCards = createMovementCards();
        Collections.shuffle(movementCards);

        readLayout();

        placeCards(pathCardsSetA, pathCardsSetB);
    }

    /**
     * Hermann Grieder
     * <br>
     * Create 105 movement cards with their value and CardType.MOVEMENT
     *
     * @return
     */
    private ArrayList<Card> createMovementCards() {
        movementCards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 15; j++) {
                movementCards.add(new Card(i, CardType.MOVEMENT));
            }
        }
        return movementCards;
    }

    /**
     * Fabian Witschi
     * <br>
     * "Cleans" the Path Card Set A. We remove certain cards that are not in the game.
     * The cards to be removed are different in cardSetA than in cardSetB
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
     * "Cleans" the Path Card Set B. We remove certain cards that are not in the game.
     * The cards to be removed are different in cardSetA than in cardSetB
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

    private ArrayList<Card> createPathCards() {
        ArrayList<Card> pathCardsSet = new ArrayList<>();
        for (int j = 0; j < 7; j++) {
            for (int k = 1; k <= 7; k++) {
                pathCardsSet.add(new Card(j, k, CardType.PATH));
            }
        }
        return pathCardsSet;
    }

    /**
     * Hermann Grieder
     * <br>
     * Reads a random layout file and creates the correspondent tiles from it
     * <p>
     */
    private void readLayout() {
        File folder = new File("src/ch/atlantis/res");
        int numberOfLayouts;
        numberOfLayouts = folder.listFiles().length;
        Random rand = new Random();
        int layoutId = rand.nextInt(numberOfLayouts);
        System.out.println(layoutId);
        tiles = new ArrayList<>();
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

    /**
     * Hermann Grieder
     * <br>
     *
     * @param pathCardsSetA
     * @param pathCardsSetB
     */
    private void placeCards(ArrayList<Card> pathCardsSetA, ArrayList<Card> pathCardsSetB) {
        pathCards = new ArrayList<>();
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

    /**
     * Hermann Grieder
     * <br>
     *
     * @param colorSet
     * @param cardType
     * @param tile
     */
    private void placeSpecialCard(int colorSet, CardType cardType, Tile tile) {
        Card card = new Card(colorSet, cardType);
        card.setIsOnTop(true);
        setIdAndAddCard(card, tile);
    }

    /**
     * Hermann Grieder
     * <br>
     *
     * @param iterator
     * @param tile
     */
    private void placeOneCard(Iterator<Card> iterator, Tile tile) {
        Card card = iterator.next();
        card.setIsOnTop(true);
        setIdAndAddCard(card, tile);
    }

    /**
     * Hermann Grieder
     * <br>
     *
     * @param iterator
     * @param tile
     */
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

    // *************************  START GAME METHODS ************************************//

    /**
     * Hermann Grieder
     * <br>
     * Finalizes the game once the game has been started by the player and returns a HashMap with
     * the needed information to show the game in the client. Since the game object is created
     * before all the players are known we have to do this additional step here to add their
     * individual movementCards.
     *
     * @return The initial game state HashMap
     */
    public HashMap<String, Object> finalizeGame() {
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
        gameStateMap.put("Deck", deck);
        return gameStateMap;
    }


    // ****************************  IN-GAME METHODS ************************************//

    /**
     * Fabian Witschi
     * <br>
     * Checks if the game is over by checking if there are still gamePieces on the way to the end
     *
     * @return True if the game is over
     */
    public boolean isGameOver() {
        int count = 0;
        boolean isGameOver = true;

        for (Player player : players) {
            for (GamePiece gamePiece : player.getGamePieces()) {
                if (gamePiece.isOnLand()) {
                    count++;
                }
            }
            if (count == 3) {
                player.setGamePiecesOnLand(count);
                System.out.println("Player with ID " + player.getPlayerID() + " has ended game first!");
                isGameOver = true;
            } else {
                player.setGamePiecesOnLand(count);
                System.out.println("Player with ID " + player.getPlayerID() + " has " + count + " gamepieces on land");
                isGameOver = false;
            }
        }
        return isGameOver;
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

        //We repeat these following steps and check if every move made is valid. If all of them are valid we
        // accept the move and increase the turn count and can return true
        for (int i = 0; i < targetPathIdsRemote.size(); i++) {
            // Find the target pathId on the server side
            targetPathId = findTargetPathId(activeGamePiece, i);
            // Check if there is water on the way to the target. Return the pathId of that water tile or 0 if not water is on the way
            // TODO: so far we only want to check if the target is occupied. So we have to deal with this later.
            int waterOnTheWayPathId = getWaterPathId(activeGamePiece.getCurrentPathId(), targetPathId);
            System.out.println("Water Path Id: " + waterOnTheWayPathId);

            // If there is water on the way to the target then calculate the price to cross
            int priceToCrossWater = 0;
            if (waterOnTheWayPathId != 0) {
                priceToCrossWater = getPriceForCrossing(waterOnTheWayPathId);
                System.out.println("Price to cross water: " + priceToCrossWater);
                //TODO: if there is water
                //return false;
            }

            // Check if the target pathId is already occupied by someone else
            boolean targetIsOccupied = checkIfOccupied(targetPathId, activeGamePiece);
            System.out.println("GameModel -> Target occupied: " + targetIsOccupied);
            if (!(targetIsOccupied && targetPathIdsRemote.size() > i)) {
                System.out.println("No card was played to jump over another gamePiece");
                //return false;
            }
            // Set the targetPathId as the currentPathId in the active gamePiece
            players.get(activePlayerId).getGamePieces().get(selectedGamePieceIndex).setCurrentPathId(targetPathId);
        }

        // Remove the movement card played by the player and add it to the discarded cards list
        for (Integer index : playedCardsIndices) {
            Card cardToDiscard = players.get(activePlayerId).getMovementCards().get(index);
            discardedCards.add(cardToDiscard);
            System.out.println("GameModel-> Card added to discard pile");
        }
        System.out.println("GameModel -> Player holds " + players.get(activePlayerId).getMovementCards().size() + " cards");
        for (Card card : discardedCards) {
            System.out.println("GameModel -> Movement card removed: " + players.get(activePlayerId).getMovementCards().remove(card));
        }
        System.out.println("GameModel -> Player holds " + players.get(activePlayerId).getMovementCards().size() + " cards");

        // Pick up the card behind the gamePiece
        int scoreToAdd = removePathCardFromPath(targetPathId);
        // TODO: We need a list for the individuals score picked up by the player. So we can later pay with it.
        // Add the score of that card to the player
        players.get(activePlayerId).addScore(scoreToAdd);

        // Give the player new movement cards. The amount of cards the player played, plus for each GamePiece
        // that has reached the end, one additional card
        // This part is checking for each player which one is playing at this moment - if found, it will give
        // the player as much cards as are allowed regarding the rules -> 0 gamepieces on land = 1 card
        // 1 gamepiece on land = 2 cards and 2 gamepieces on land = 3 cards.
        for (Player player : players) {
            if (player.getPlayerID() == activePlayerId) {
                for (int i = 0; i <= player.getGamePiecesOnLand(); i++) {
                    addCardFromDeckToPlayer();
                }
            }
        }
        System.out.println("GameModel -> Player holds " + players.get(activePlayerId).getMovementCards().size() + " cards");

        System.out.println("GameModel -> Score of " + scoreToAdd + " added to " + players.get(activePlayerId).getPlayerName());

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
     * @param index           Current index of the iteration through all the moves
     * @return The targetPathId found
     */
    private int findTargetPathId(GamePiece activeGamePiece, int index) {
        int targetPathIdRemote = targetPathIdsRemote.get(index);
        System.out.println("Index of selected card: " + index);
        System.out.println("Size of playedCardsIndices " + playedCardsIndices.size());
        int selectedCard = playedCardsIndices.get(index);
        System.out.println("Movement cards: " + players.get(activePlayerId).getMovementCards().size());

        int startPathId = 101;
        if (activeGamePiece.getCurrentPathId() != 300) {
            startPathId = activeGamePiece.getCurrentPathId() + 1;
        }
        System.out.println("GameModel -> StartPathId: " + startPathId);

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
        if (!found && nextPathId == 154) {
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
     * Hermann Grieder
     * <br>
     * Checks if the targetPathId that was found is already occupied.
     *
     * @param targetPathId    The pathId the gamePiece should be moved to
     * @param activeGamePiece The gamePiece that was moved
     * @return True if the target is occupied, false if it is free to go to
     */
    private boolean checkIfOccupied(int targetPathId, GamePiece activeGamePiece) {
        for (Player player : players) {
            for (GamePiece gamePiece : player.getGamePieces()) {
                if (gamePiece != activeGamePiece && gamePiece.getCurrentPathId() == targetPathId && gamePiece.getCurrentPathId() != 400) {
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
    private int getWaterPathId(int currentPathId, int targetPathId) {
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
        int waterPathId = 0;
        // We count the cards that have the same pathId as the startPathId. If we count less than two
        // cards it must be an exposed water tile.
        if (startPathId < target) {
            for (Card pathCard : pathCards) {
                if (pathCard.getPathId() == startPathId) {
                    waterPathId = pathCard.getPathId();
                    count++;
                }
            }
            // Recursive call in case we find more than one card on that pathId.
            if (count != 1) {
                System.out.println("GameModel - > There are " + count + " cards at " + startPathId);
                return getWaterPathId(startPathId, targetPathId);
            }
        }
        // If by the time we get to the target path and have not found any water tiles we return 0
        if (startPathId == targetPathId) {
            System.out.println("No water found to the target");
            return 0;
        }

        if (count == 1 && waterPathId != 0) {
            System.out.println("Water on PathId: " + startPathId);
            return waterPathId;
        }
        return 0;
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
        int valueBehind = getValueFromCardBehind(pathId);
        System.out.println("PRICE (BEHIND) -> " + valueBehind);
        int valueAfter = getValueFromCardAfter(pathId);
        System.out.println("PRICE (After) -> " + valueAfter);
        if (valueBehind > valueAfter) {
            System.out.println("GameModel -> Price to cross: " + valueAfter);
            return valueAfter;
        } else {
            System.out.println("GameModel -> Price to cross: " + valueBehind);
            return valueBehind;
        }
    }

    /**
     * Fabian Witschi
     * <br>
     * Since we found the first water card on the way to the target card it might be that on the following
     * card it has more water cards and this method is checking if there is on the next pathId more than one card
     * if so we want to get the one at the top which is cardType NOT water and is on top. If we get only one card
     * on the pathId we recall the method (recursive) in order to iterate through the following cards until we get
     * a "normal" path card.
     *
     * @param pathId
     * @return valueOfCardAfter
     */
    private int getValueFromCardAfter(int pathId) {
        int pathIdAfter = pathId + 1;
        int valueAfter = 0;
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
                        valueAfter = pathCard.getValue();
                    }
                }
            } else {
                valueAfter = getValueFromCardAfter(pathIdAfter);
            }
        }
        return valueAfter;
    }

    /**
     * Fabian Witschi
     * <br>
     * In the method ifWaterOnTheWay we check already each path card until we get to the first water card
     * therefore it is not necessary to iterate backwards until we find the next "normal" card - so just getting
     * the value of the card behind is enough for calculating the price for passing
     *
     * @param pathId
     * @return valueOfCardBehind
     */
    private int getValueFromCardBehind(int pathId) {
        int pathIdBehind = pathId - 1;
        for (Card pathCard : pathCards) {
            if (pathCard.getPathId() == pathIdBehind) {
                if (pathCard.getCardType() != CardType.WATER && pathCard.isOnTop()) {
                    System.out.println("Card Behind -> " + pathCard.getValue());
                    return pathCard.getValue();
                }
            }
        }
        return -1;
    }

    /**
     * Hermann Grieder
     * <br>
     * Removes the pathCard at the index behind the targetPathId. In case that there is water behind, the method
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
        // another card underneath we will set it to be on top for the next turn.
        int score = 0;
        ArrayList<Card> foundCardsAtPathId = new ArrayList<>();
        Card cardToRemove = null;
        Card cardToShow = null;

        for (Card pathCard : pathCards) {
            if (pathCard.getPathId() == startPathId) {
                foundCardsAtPathId.add(pathCard);
            }
        }
        // Recursive call in case we find less than two cards, which means there is only water
        // and nothing to be picked up. So we continue our search.
        if (foundCardsAtPathId.size() == 1 && foundCardsAtPathId.get(0).getPathId() != -1) {
            System.out.println("GameModel - > There is only " + foundCardsAtPathId.size() + " card at " + startPathId);
            return removePathCardFromPath(startPathId);
        }
        // If we found more than one card, we then need to check if is already occupied. If so, we need to keep searching
        // otherwise we can mark those cards as the ones we were looking for.
        if (foundCardsAtPathId.size() > 1) {
            int pathId = foundCardsAtPathId.get(0).getPathId();
            for (Player player : players) {
                for (GamePiece gamePiece : player.getGamePieces()) {
                    if (gamePiece.getCurrentPathId() == pathId) {
                        return removePathCardFromPath(startPathId);
                    }
                }
            }
            for (Card card : foundCardsAtPathId) {
                if (card.getCardType() != CardType.WATER && card.getCardType() != CardType.START && card.getCardType() != CardType.END) {
                    if (card.isOnTop()) {
                        cardToRemove = card;
                        score = cardToRemove.getValue();
                    } else {
                        cardToShow = card;
                    }
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

    /**
     * Hermann Grieder
     * <br>
     */
    private void addCardFromDeckToPlayer() {
        if (deck.size() == 0) {
            deck = discardedCards;
            Collections.shuffle(deck);
        }
        newCardFromDeck = deck.get(0);
        players.get(currentTurnLocal).getMovementCards().add(newCardFromDeck);
        System.out.println("GameModel -> Card with value " + newCardFromDeck.getColorSet() + " added to the player");
        deck.remove(0);
    }

    /**
     * Hermann Grieder
     * <br>
     * Reads the incoming Game State Map
     *
     * @param gameStateMap The incoming HashMap
     */
    @SuppressWarnings("unchecked")
    public void readGameStateMap(HashMap<String, Object> gameStateMap) {
        playedCardsIndices = null;
        targetPathId = 0;
        currentTurnRemote = (int) gameStateMap.get("CurrentTurn");
        activePlayerId = (int) gameStateMap.get("PlayerId");
        selectedGamePieceIndex = (int) gameStateMap.get("GamePieceIndex");
        targetPathIdsRemote = (ArrayList<Integer>) gameStateMap.get("TargetPathIds");
        playedCardsIndices = (ArrayList<Integer>) gameStateMap.get("PlayedCardsIndices");
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
        gameStateMap.put("Score", players.get(activePlayerId).getScore());
        gameStateMap.put("GamePieceUsedIndex", selectedGamePieceIndex);
        gameStateMap.put("TargetPathId", targetPathId);
        gameStateMap.put("IndexOfCardToRemove", indexOfCardToRemove);
        gameStateMap.put("IndexOfCardToShow", indexOfCardToShow);
        gameStateMap.put("DeckCardToAdd", newCardFromDeck);
        return gameStateMap;
    }


    /**
     * Adds a player to the players list
     *
     * @param player Player object to add
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Removes a player from the players list
     *
     * @param player Player object to remove
     */
    public void remove(Player player) {
        players.remove(player);
    }

    /**
     * Get the players list
     *
     * @return ArrayList of all players
     */
    public ArrayList<Player> getPlayers() {
        return players;
    }

}

