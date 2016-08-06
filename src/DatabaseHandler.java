import java.sql.*;

/**
 * Created by Loris Grether on 05.08.2016.
 */

public class DatabaseHandler {

    private Statement stmt;
    private Connection cn;
    private ResultSet rs;

    public DatabaseHandler(){

        System.out.println("Enter Constructor");

        cn = null;
        stmt = null;
        rs = null;

        String serverInfo = "jdbc:mysql://" + "localhost" + ":" + "3306" + "/atlantisdb";
        String optionInfo = "?connectTimeout=5000";
        System.out.println(("Opening connection to " + serverInfo + "\n"));
        try {
            cn = DriverManager.getConnection(serverInfo + optionInfo, "root", this.blabla);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (rs != null) try {
                if (!rs.isClosed()) rs.close();
            } catch (Exception e) {}
            if (stmt != null) try {
                if (!stmt.isClosed()) stmt.close();
            } catch (Exception e) {}
        }
    }

    public void createProfile(Message message) {

        String[] userNamePassword = message.getMessage().toString().split(",");

        String userName = userNamePassword[0];
        String userPassword = userNamePassword[1];

        try {
            String sql = "INSERT INTO user (UserName, Password) VALUES (?, ?)";
            PreparedStatement statement = cn.prepareStatement(sql);
            statement.setString(1, userName);
            statement.setString(2, userPassword);
            statement.executeUpdate();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getGameList() {
    }
















































































































































    private String blabla = "maschine1";



}