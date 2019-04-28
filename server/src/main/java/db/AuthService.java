package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuthService extends DB_mysql{

    private Statement statement = null;

    public AuthService(){
        setURL("localhost", "chat", 3306);
        connect("root", "lelybr");
    }

    public void connect() {
        try {
            statement = getConnection().createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public String getNickByLoginAndPass(String login, String password) {
//        String sql = String.format("SELECT nick, password FROM chat.user WHERE login ='%s'", login);
//        int passHash = password.hashCode();
//        ResultSet res = null;
//        try {
//            res = statement.executeQuery(sql);
//            if(res.next()){
//                String nick = res.getString(1);
//                int passBd = res.getInt(2);
//                if(passHash==passBd) return nick;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

}
