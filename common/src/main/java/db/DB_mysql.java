package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB_mysql extends DB_base{

    private com.mysql.jdbc.Connection connection = null;

    public DB_mysql(){
        super("com.mysql.jdbc.Driver");
    }

    @Override
    public void setURL(String host, String database, int port){
        if(database.length()>0){
            this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        } else {
            this.url = "jdbc:mysql://" + host + ":" + port;
        }
    }

    @Override
    public Connection getConnection(){
        return connection;
    }

    @Override
    public void connect (String login, String password){
        super.connect(login, password);
        try{
            connection = (com.mysql.jdbc.Connection)DriverManager.getConnection(url,properties);
        } catch (SQLException e){
            System.err.println("SQLException: code = " + String.valueOf(e.getErrorCode()+ e.getMessage()));
        }
    }
}

