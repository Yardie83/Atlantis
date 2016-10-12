package ch.atlantis.util;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Hermann Grieder on 19.07.2016.
 */
public enum MessageType {
    SERVER_MESSAGE,
    CHAT,
    DB_STATEMENT,
    DISCONNECT,
    CREATEPROFILE,
    LOGIN,
    GAMELIST,
    NEWGAME,
    SUCCESSFUL,
    UNSUCCESSFUL,
    USERNAME,
    LANGUAGELIST,
    GAMEREADY,
    JOINGAME
}
