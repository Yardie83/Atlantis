import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Can Heval Cokyasar on 23.08.16.
 */

public class GameHandler {

    private ObservableList<ArrayList<HashMap<String, Integer>>> gameList;
    private ArrayList<HashMap<String, Integer>> gameArray;
    private HashMap<String, Integer> gameInfo;
    private int gameID;

    public GameHandler() {
        gameID = 0;
        gameInfo = new HashMap<>();
        gameArray = new ArrayList<>();
        gameList = FXCollections.observableList(gameArray);
    }

    public void addGame(String gameName, int players) {
        gameInfo.put(gameName, players);
        gameArray.add(gameInfo);

    }

    public ObservableList<HashMap<String, Integer>> getGameList() {
        return gameList;
    }
}