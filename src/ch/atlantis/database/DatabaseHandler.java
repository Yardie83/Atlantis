package ch.atlantis.database;

import ch.atlantis.game.Player;
import ch.atlantis.server.AtlantisServer;
import ch.atlantis.util.Message;
import ch.atlantis.util.MessageType;

import java.sql.*;
import java.util.logging.Logger;
import java.util.ArrayList;

/**
 * Created by Loris Grether on 05.08.2016.
 */

public class DatabaseHandler {

    private Statement stmt;
    private Connection cn;
    private ResultSet rs;

    private Logger logger;

    public DatabaseHandler() {

        logger = Logger.getLogger(AtlantisServer.AtlantisLogger);
        logger.info("Enter constructor.");

        createDatabase();
        connectToDatabase();
    }

    private void createDatabase() {
    }

    private void connectToDatabase() {
        cn = null;
        stmt = null;
        rs = null;

        //localhost replaces the ip-Adress (127.0.0.1 should work as well)

        String serverInfo = "jdbc:mysql://" + "localhost" + ":" + "3306" + "/";
        //String optionInfo = "?connectTimeout=5000";
        logger.info("Opening connection to " + serverInfo + "\n");

        try {

            String userName = "root";
            String password = "maschine1";

            cn = DriverManager.getConnection(serverInfo, userName, password);

            //Here we create the connection to the database
            stmt = cn.createStatement();

            stmt.execute("CREATE DATABASE IF NOT EXISTS codeMonkeysAtlantisDB");

            stmt.execute("USE codeMonkeysAtlantisDB");

            stmt.execute("CREATE TABLE IF NOT EXISTS tbl_User " +
                    "(UserID INT NOT NULL AUTO_INCREMENT, " +
                    "UserName VARCHAR(45) NOT NULL, " +
                    "Password VARCHAR(45) NOT NULL, " +
                    "CumulatedGameTime INT NULL , " +
                    "NumberOfGames INT NULL , " +
                    "PRIMARY KEY (`UserID`))");

            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) try {
                if (!rs.isClosed()) rs.close();
            } catch (Exception e) {
            }
            if (stmt != null) try {
                if (!stmt.isClosed()) stmt.close();
            } catch (Exception e) {
            }
        }
    }

    public boolean createProfile(Message message) {

        logger.info("createProfile");

        String[] userNamePassword = message.getMessageObject().toString().split(",");

        String userName = userNamePassword[0];
        String userPassword = userNamePassword[1];

        boolean isSuccess = false;

        try {

            if (checkUserEntries(userName, userPassword, message) == 0) {

                logger.info("Success creating profile.");

                String sql = "INSERT INTO tbl_User (UserName, Password, CumulatedGameTime, NumberOfGames) VALUES (?, ?, 0, 0)";
                PreparedStatement statement = cn.prepareStatement(sql);
                statement.setString(1, userName);
                statement.setString(2, userPassword);
                statement.executeUpdate();
                statement.close();

                isSuccess = true;
            } else {

                logger.info("No success creating the user profile.");

                isSuccess = false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return isSuccess;
        }
    }

    public boolean userLogin(Message message) {

        String[] userNamePassword = message.getMessageObject().toString().split(",");

        String userName = userNamePassword[0];
        String userPassword = userNamePassword[1];

        boolean isValid = false;

        try {
            if (checkUserEntries(userName, userPassword, message) == 1) {
                isValid = true;

            } else {
                isValid = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return isValid;
        }
    }

    //This method checks the user entries in case of creating a new account as welle as logging in the game
    private int checkUserEntries(String userName, String userPassword, Message message) throws SQLException {

        PreparedStatement preparedStatement = null;
        rs = null;

        String query = "SELECT COUNT(*) FROM tbl_User WHERE userName = ?";


        //Here the user wants to create a new profile means we have to check the database for the new userName
        //so there are no dublicates in the database
        if (message.getMessageType() == MessageType.CREATEPROFILE) {

            preparedStatement = cn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userName);

            //Here the user want to login so we have to check if the userName and the corresponding password
            //are in the database
        } else if (message.getMessageType() == MessageType.LOGIN) {

            query = "SELECT COUNT(*) FROM tbl_User WHERE userName = ? AND password = ?";
            preparedStatement = cn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, userPassword);
        }

        rs = preparedStatement.executeQuery();
        rs.next();

        return rs.getInt(1);
    }

    //This method gets called when a user leaves the game
    public void enterGameTime(long time, String s) {

        int gameTime = (int) time + 1;

        //This line adds two '' to the string
        String userName = "'" + s + "'";

        System.out.println("THE NUMBER OF MINUTES IS: " + gameTime);

        try {

            String sql = "UPDATE codemonkeysatlantisdb.tbl_user SET " +
                    "codemonkeysatlantisdb.tbl_user.CumulatedGameTime = codemonkeysatlantisdb.tbl_user.CumulatedGameTime + " + gameTime +
                    " WHERE codemonkeysatlantisdb.tbl_user.UserName = " + userName;

            PreparedStatement statement = null;
            statement = cn.prepareStatement(sql);
            statement.execute();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("The player: " + userName + " wasted " + gameTime + " minutes in the game.");
    }

    public void increaseNumberOfGames(ArrayList<Player> players) {

        try {

            for (Player player : players) {

                String userName = "'" + player.getPlayerName() + "'";

                String sql = "UPDATE codemonkeysatlantisdb.tbl_user SET " +
                        "codemonkeysatlantisdb.tbl_user.NumberOfGames = codemonkeysatlantisdb.tbl_user.NumberOfGames + " + 1 +
                        " WHERE codemonkeysatlantisdb.tbl_user.UserName = " + userName;

                PreparedStatement statement = null;
                statement = cn.prepareStatement(sql);
                statement.execute();
                statement.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getInformations(String currentPlayerName) {

        PreparedStatement preparedStatement = null;
        rs = null;

        //String query = "SELECT COUNT(*) FROM tbl_User WHERE userName = ?";

        String query = "select codemonkeysatlantisdb.tbl_user.NumberOfGames\n" +
                "from codemonkeysatlantisdb.tbl_user\n" +
                "where codemonkeysatlantisdb.tbl_user.UserName = ?";

        try {

            preparedStatement = cn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, currentPlayerName);

            rs = preparedStatement.executeQuery();

            //rs.

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}