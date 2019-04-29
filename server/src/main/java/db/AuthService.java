package db;

import lombok.NonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuthService extends DB_mysql{

    private static AuthService authService = new AuthService();
    private Statement statement = null;

    private AuthService(){
        setURL("localhost", "test", 3306);
        connect("test", "test");
        connect();
    }

    public static AuthService getInstance(){ return authService;}

    public void connect() {
        try {
            statement = getConnection().createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyLoginAndPass(@NonNull String login, @NonNull String pass){
        String sql = String.format("SELECT id FROM test.users WHERE login =\"%s\" AND password = %d",login,Integer.parseInt(pass));
        try {
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
