package ch.atlantis.game;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Can Heval Cokyasar on 23.08.16.
 */

public class GameHandler{

    private LinkedHashMap<String, Integer> gameList;

    public GameHandler() {
        gameList = new LinkedHashMap<>();
    }

    public void addGame(String gameName, int players) {
        gameList.put(gameName, players);
    }

    public void removeGame(){
        //TODO Needs to be implemented
    }

    public LinkedHashMap<String,Integer> getGameList() {
        return gameList;
    }
}