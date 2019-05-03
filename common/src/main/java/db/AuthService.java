package db;

import lombok.NonNull;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuthService extends DB_mysql{

    private static AuthService authService = new AuthService();
    private Statement statement = null;

    private AuthService(){}

    public void start(){
        setURL("localhost", "test", 3306);
        connect("test", "test");
        connect();
    }

    public static AuthService getInstance(){ return authService;}

    private void connect() {
        try {
            statement = getConnection().createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int verifyLoginAndPass(@NonNull String login, int pass){
        String sql = String.format("SELECT id FROM test.users WHERE login =\"%s\" AND password = %d",login, pass);
        int resultIdUser = -1;
        try {
            ResultSet rs = statement.executeQuery(sql);
            if(rs.next()){
                resultIdUser = rs.getInt(1);
            };
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return resultIdUser;
        }
    }

    public boolean accountRegistration(@NonNull String login, int pass){
        if(!isDuplicateLogin(login)){
            String sql = String.format("INSERT INTO test.users(login, password) VALUES(\"%s\", %d)",login, pass);
            try {
                int rs = statement.executeUpdate(sql);
                return rs != 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDuplicateLogin(@NonNull String login){
        String sql = String.format("SELECT id FROM test.users WHERE login =\"%s\"",login);
        try {
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isDuplicateCatalog(@NonNull String path, int id){
        String sql = String.format("SELECT id FROM test.reestr WHERE path =\"%s\" AND iduser = %d",path,id);
        try {
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertNewCatalog(@NonNull String path, int id){
        if(!isDuplicateCatalog(path,id)){

        }
    }

    public String buildStructureCatalog(int iduser){
        if(iduser < 1) return null;
        String sql = String.format("SELECT CONCAT(rs.path,\"/\",fl.name)" +
                "                   FROM test.reestr AS rs LEFT JOIN test.files AS fl " +
                "                   ON rs.idfile = fl.id AND rs.iduser = %d", iduser);
        StringBuilder str = new StringBuilder();
        try {
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()){
                str.append(rs.getString(1)).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return str.toString();
        }
    }
}
