package ch.atlantis.database;

import ch.atlantis.util.Message;
import ch.atlantis.util.MessageType;

import java.sql.*;

/**
 * Created by Loris Grether on 05.08.2016.
 */
public class DatabaseHandler {

    private Statement stmt;
    private Connection cn;
    private ResultSet rs;

    public DatabaseHandler() {

        System.out.println("Enter Constructor");

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
        System.out.println(("Opening connection to " + serverInfo + "\n"));

        try {

            cn = DriverManager.getConnection(serverInfo, "root", "maschine1");

            //Here we create the connection to the database
            stmt = cn.createStatement();

            stmt.execute("CREATE DATABASE IF NOT EXISTS codeMonkeysAtlantisDB");

            stmt.execute("USE codeMonkeysAtlantisDB");

            stmt.execute("CREATE TABLE IF NOT EXISTS tbl_User " +
                    "(UserID INT NOT NULL AUTO_INCREMENT, " +
                    "UserName VARCHAR(45) NOT NULL, " +
                    "Password VARCHAR(45) NOT NULL, " +
                    "CumulatedGameTime TIME NULL, " +
                    "NumberOfGames INT NULL, " +
                    "PRIMARY KEY (`UserID`))");

            stmt.execute("INSERT INTO tbl_User (UserName, Password)" +
                    "VALUES ('derErste', '1234')");

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

        System.out.println("createProfile");

        String[] userNamePassword = message.getMessageObject().toString().split(",");

        String userName = userNamePassword[0];
        String userPassword = userNamePassword[1];

        boolean isSuccess = false;

        try {

            if (checkUserEntries(userName, userPassword, message) == 0) {

                System.out.println("Success creating profile");

                String sql = "INSERT INTO tbl_user (UserName, Password) VALUES (?, ?)";
                PreparedStatement statement = cn.prepareStatement(sql);
                statement.setString(1, userName);
                statement.setString(2, userPassword);
                statement.executeUpdate();
                statement.close();

                isSuccess = true;
            } else {

                System.out.println("No success creating the user profile");

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

    private int checkUserEntries(String userName, String userPassword, Message message) throws SQLException {

        PreparedStatement preparedStatement = null;
        rs = null;

        String query = "SELECT COUNT(*) FROM tbl_user WHERE userName = ?";

        if (message.getMessageType() == MessageType.CREATEPROFILE) {

            preparedStatement = cn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userName);

        } else if (message.getMessageType() == MessageType.LOGIN) {

            query = "SELECT COUNT(*) FROM tbl_user WHERE userName = ? AND password = ?";
            preparedStatement = cn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, userPassword);
        }

        rs = preparedStatement.executeQuery();
        rs.next();

        return rs.getInt(1);
    }

    public void enterGameTime(long gameTime, String userName) {

        String s = userName;
    }
}
