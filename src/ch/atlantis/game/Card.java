package ch.atlantis.game;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;

/**
 * Created by Hermann Grieder on 15/08/16.
 * <p>
 * Card class that defines sets the color and the image for the individual card
 */

enum CardType {
    PATH, WATER, START, END, BRIDGE, MOVEMENT
}

public class Card extends Rectangle implements Serializable {

    private static final long serialVersionUID = 1597939850705259874L;

    private boolean isOnTop;
    private boolean isPlayed;
    private int value;
    private int colorSet;
    private CardType cardType;
    private int pathID;

    public final static int BROWN = 0;
    public final static int PINK = 1;
    public final static int GREY = 2;
    public final static int YELLOW = 3;
    public final static int GREEN = 4;
    public final static int BLUE = 5;
    public final static int WHITE = 6;

    // Constructor for Movement Cards. They do not have a value associated.
    public Card(int colorSet, CardType cardType) {

        this.setWidth(20);
        this.setHeight(40);
        this.cardType = cardType;
        this.colorSet = colorSet;
        this.setStroke(Color.BLACK);
        applyColorSet(colorSet);
        this.isPlayed = false;
    }

    // Constructor for Path Cards. They do have a value associated.
    public Card(int colorSet, int value, CardType cardType) {

        this.value = value;
        this.colorSet = colorSet;
        this.cardType = cardType;
        this.setStroke(Color.BLACK);
        applyColorSet(colorSet);
    }

    private void applyColorSet(int colorSet) {
        switch (colorSet) {
            case BROWN:
                super.setFill(Color.BROWN);
                break;
            case PINK:
                super.setFill(Color.PINK);
                break;
            case GREY:
                super.setFill(Color.GREY);
                break;
            case YELLOW:
                super.setFill(Color.YELLOW);
                break;
            case GREEN:
                super.setFill(Color.GREEN);
                break;
            case BLUE:
                super.setFill(Color.BLUE);
                break;
            case WHITE:
                super.setFill(Color.WHITE);
                break;
            case 7:
                super.setFill(null);
                break;
            default:
                super.setFill(null);
        }
    }

    public int getValue() {
        return value;
    }

    public int getColorSet() {
        return colorSet;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setPathId(int pathID) {
        this.pathID = pathID;
    }

    public int getPathId() {
        return this.pathID;
    }

    public void setIsOnTop(boolean isOnTop) {
        this.isOnTop = isOnTop;
    }

    public boolean isOnTop() {
        return this.isOnTop;
    }

    public boolean isPlayed() {
        return this.isPlayed;
    }

    public void setIsPlayed(Boolean isPlayed) {
        this.isPlayed = isPlayed;
    }

}

