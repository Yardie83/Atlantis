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

        connectToDatabase();
    }

    private void connectToDatabase() {
        cn = null;
        stmt = null;
        rs = null;

        String serverInfo = "jdbc:mysql://" + "localhost" + ":" + "3306" + "/atlantisdb";
        String optionInfo = "?connectTimeout=5000";
        System.out.println(("Opening connection to " + serverInfo + "\n"));
        try {

            cn = DriverManager.getConnection(serverInfo, "root", "maschine1");
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

                System.out.println("Success");

                String sql = "INSERT INTO user (UserName, Password) VALUES (?, ?)";
                PreparedStatement statement = cn.prepareStatement(sql);
                statement.setString(1, userName);
                statement.setString(2, userPassword);
                statement.executeUpdate();
                statement.close();

                isSuccess = true;
            } else {

                System.out.println("not success");

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

        System.out.println("checkUserEntries");

        String query = "SELECT COUNT(*) FROM user WHERE userName = ?";

        if (message.getMessageType() == MessageType.CREATEPROFILE) {

            preparedStatement = cn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userName);

        } else if (message.getMessageType() == MessageType.LOGIN) {

            query = "SELECT COUNT(*) FROM user WHERE userName = ? AND password = ?";
            preparedStatement = cn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, userPassword);
        }

        rs = preparedStatement.executeQuery();
        rs.next();

        System.out.println("Number:" + rs.getInt(1));
        return rs.getInt(1);
    }

    public void newGame(Message message) {

        String[] gameInformation = message.getMessageObject().toString().split(",");

        String gameName = gameInformation[0];
        int nrOfPlayers = Integer.parseInt(gameInformation[1]);

        System.out.println(gameName + " " + nrOfPlayers);

        //TODO: Create game instance here
    }
}