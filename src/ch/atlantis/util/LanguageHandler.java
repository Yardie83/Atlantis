package ch.atlantis.util;

import ch.atlantis.util.Language;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by LorisGrether on 22.08.2016.
 */
public class LanguageHandler {

    private ArrayList<Language> languageList = new ArrayList<Language>();

    public LanguageHandler() {

        getFiles();
    }

    private void getFiles() {

        String[] files = new String[2];

        files[0] = "src/res/languages/Atlantis_en-en - Kopie.xml";
        files[1] = "src/res/languages/Atlantis_de-de - Kopie.xml";

        if (files != null || files.length != 0) {

            for (String file : files) {
                readLanguageFile(file);
            }
        }
    }

    private void readLanguageFile(String path) {

        File xmlLanguageFile = new File(path);
        String culture = "";

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setIgnoringComments(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlLanguageFile);

            culture = doc.getDocumentElement().getAttribute("Culture");

            this.languageList.add(new Language(culture, getFileValues(doc)));


        } catch (ParserConfigurationException parseException) {

        } catch (SAXException asf) {

        } catch (FileNotFoundException notFoundException) {
            System.out.println("Could not find one or more language XML Files");

        } catch (IOException ioException) {

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private Hashtable<String, String> getFileValues(Document doc) {

        NodeList nodes = doc.getElementsByTagName("String");
        Element element = doc.getDocumentElement();

        Hashtable<String, String> values = new Hashtable<String, String>();

        for (int i = 0; i <= nodes.getLength() - 1; i++) {

            String id = nodes.item(i).getAttributes().getNamedItem("Id").toString();
            String value = element.getElementsByTagName("String").item(i).getChildNodes().item(0).getNodeValue();

            values.put(modifyValue(id), value);
        }

        return values;
    }

    private String modifyValue(String id) {

        String[] split = id.split("\"");
        return split[1];
    }

    public ArrayList<Language> getLanguageList() {
        return this.languageList;
    }
}
