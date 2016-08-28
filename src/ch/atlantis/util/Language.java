package ch.atlantis.util;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Created by LorisGrether on 22.08.2016.
 */
public class Language implements Serializable{

    private String culture = "";

    private Hashtable<String, String> languageTable = new Hashtable<String, String>();

    public Language(String culture, Hashtable values){
        this.culture = culture;
        this.languageTable = values;
    }

    public String getCulture() {
        return culture;
    }

    public Hashtable<String, String> getLanguageTable() {
        return languageTable;
    }
}