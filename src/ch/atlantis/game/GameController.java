package ch.atlantis.game;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Fabian on 14/11/16.
 *
 */
public class GameController {

    private GameModel gameModel;

    public GameController(GameModel gameModel) {
        this.gameModel = gameModel;
    }

    public boolean handleMove(HashMap<String, Object> gameStateMap) {
        gameModel.readGameStateMap(gameStateMap);
        return gameModel.handleMove();
    }

    /**
     *
     * Can Heval Cokyasar
     *
     *
     * @param indexOfCard
     * @return
     */
    public ArrayList<Card> handleUserCardPurchase(int indexOfCard) {
        return gameModel.handleUserCardPurchase(indexOfCard);
    }

    public ArrayList<Card> handleCantMove() {
        return gameModel.handleCantMove();
    }

    public int handleNewTurn() {
        return gameModel.handleNewMove();
    }

    public boolean isGameOver() {
        return gameModel.isGameOver();
    }

    public HashMap<String, Object> writeGameState() {
        return gameModel.writeGameStateMap();
    }
}
