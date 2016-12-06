package ch.atlantis.game;

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

    public boolean isGameOver() {
        return gameModel.isGameOver();
    }

    public HashMap<String, Object> writeGameState() {
        return gameModel.writeGameStateMap();
    }
}
