package ch.atlantis.game;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Hermann Grieder on 28.10.2016.
 */
public class GameModel {

    private ArrayList<Player> players;
    private ArrayList<Tile> tiles;
    private ArrayList<Card> pathCardsSetA;
    private ArrayList<Card> pathCardsSetB;
    private final ArrayList<Card> pathCards;
    private ArrayList<Card> movementCards;
    private ArrayList<Card> deck;

    public GameModel() {

        players = new ArrayList<>();
        tiles = new ArrayList<>();

        pathCardsSetA = new ArrayList<>();
        createPathCards(pathCardsSetA);
        cleanCardSetA(pathCardsSetA);
        Collections.shuffle(pathCardsSetA);

        pathCardsSetB = new ArrayList<>();
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

    public HashMap<String, ArrayList> init() {
        addMovementCardsToPlayers(movementCards);
        return createHashMapForGame();
    }

    private void addMovementCardsToPlayers(ArrayList<Card> movementCards) {

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            for (int k = 0; k < 4 + i; k++) {
                player.addMovementCard(movementCards.get(0));
                movementCards.remove(0);
            }
        }
        deck = movementCards;
    }

    private void createMovementCards() {
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 15; j++) {
                this.movementCards.add(new Card(i, CardType.MOVEMENT));
            }
        }
    }

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
     * Reads the GameBoardLayout.txt file and transfers the values into the values array
     * <p>
     * Author: Hermann Grieder
     */
    private void readLayout() {

        try {
            BufferedReader bf = new BufferedReader(new FileReader("src/ch/atlantis/res/GameBoardLayout.txt"));

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
            } catch (IOException e) {
                System.out.println("Empty Line!");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File \"GameBoardLayout.txt\" not found!");
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

    private void setIdAndAddCard(Card card, Tile tile) {
        card.setPathId(tile.getPathId());
        pathCards.add(card);
    }

    private HashMap<String, ArrayList> createHashMapForGame() {

        HashMap<String, ArrayList> initList = new HashMap<>();

        initList.put("Players", players);
        initList.put("Tiles", tiles);
        initList.put("PathCards", pathCards);
        initList.put("Deck", deck);

        return initList;

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

    public ArrayList<Card> getPathCards() {
        return pathCards;
    }


}
