package ch.atlantis.game;

import ch.atlantis.util.*;
import ch.atlantis.server.*;

import java.util.HashMap;

/**
 * Created by Fabian on 14/11/16.
 */
public class GameController {

    private GameModel gameModel;
    private Message message;
    private GamePiece gamePiece;
    private Card selectedCard;
    private Card cardToMove;
    private Card cardToStart;
    private Card cardBehind;
    private int cardToMovePathId;
    private int cardAfterPathId;
    private int cardBehindPathId;
    private int tempColorSet;

    public GameController() {

    }

    public HashMap<String, Object> handlePlayerEvent(Message message) {

        HashMap<String, Object> mapToReturn = new HashMap<>();

        if (message.getMessageObject() instanceof HashMap) {
            HashMap<String, Object> receivedMap = (HashMap<String, Object>) message.getMessageObject();
            selectedCard = (Card) receivedMap.get("Card");
            gamePiece = (GamePiece) receivedMap.get("GamePiece");

            cardToStart = possiblePathCard(gamePiece);

            cardToMovePathId = cardToMove.getPathId();
            cardAfterPathId = cardToMovePathId + 1;
            cardBehindPathId = cardToMovePathId - 1;

            if (cardToStart.getCardType() == CardType.START) {
                cardToMove = possiblePathCard(selectedCard);
                if (isOccupied(cardToMove)) {
                    while (isOccupied(cardToMove)) {
                        cardToMove = getNextCard();
                        cardAfterPathId++;
                        cardBehindPathId--;
                    }
                } else {
                    gamePiece.moveGamePiece(cardToMove.getLayoutX() + (cardToMove.getWidth() / 2) - (gamePiece.getWidth() / 2),
                            selectedCard.getLayoutY() + (cardToMove.getHeight() / 2) - (cardToMove.getHeight() / 2));
                    mapToReturn.put("GamePiece", gamePiece);

                }
            } else if (cardToStart.getCardType() == CardType.END) {

            }

            for (Card card : gameModel.getPathCards()) {
                if (card.getPathId() == cardBehindPathId && card.getCardType() != CardType.WATER && card.getCardType() != CardType.BRIDGE) {
                    cardBehind = card;
                    mapToReturn.put("CardBehind", cardBehind);
                } else if (card.getCardType() == CardType.WATER || card.getCardType() == CardType.BRIDGE) {
                    cardBehindPathId--;
                }
            }

        }

        return mapToReturn;

    }

    private Card possiblePathCard(Card handCard) {

        for (int i = 101; i < 154; i++) {
            for (Card pathCard : gameModel.getPathCards()) {
                if (pathCard.getPathId() == i) {
                    if (pathCard.getColorSet() == handCard.getColorSet()) {
                        if (pathCard.isOnTop() && pathCard.getCardType() != CardType.WATER
                                && pathCard.getCardType() != CardType.START) {
                            cardBehindPathId = i - 1;
                            return pathCard;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Card possiblePathCard(GamePiece gamePiece) {

        for (int i = gamePiece.getPathIdOfGamePiece(); i < 154; i++) {
            for (Card pathCard : gameModel.getPathCards()) {
                if (pathCard.getPathId() == i) {
                    if (pathCard.getColorSet() == tempColorSet) {
                        if (pathCard.isOnTop() && pathCard.getCardType() != CardType.WATER
                                && pathCard.getCardType() != CardType.START) {
                            cardBehindPathId = i - 1;
                            return pathCard;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Fabian Witschi
     *
     * @return
     */
    private Card getNextCard() {
        for (Card card : gameModel.getPathCards()) {
            if (card.getPathId() == cardToMove.getPathId() + 1) {
                return card;
            }
        }
        return null;
    }

    private boolean isOccupied(Card cardToMove) {

        for (Player player : gameModel.getPlayers()) {
            for (GamePiece gamePiece : player.getGamePieces()) {
                if (gamePiece.getGamePieceX() == cardToMove.getLayoutX() + (cardToMove.getWidth() / 2) - (gamePiece.getWidth() / 2) && gamePiece.getGamePieceY() ==
                        cardToMove.getLayoutY() + (cardToMove.getHeight() / 2) - (gamePiece.getHeight() / 2)) {
                    return true;
                }
            }
        }
        return false;
    }

}
