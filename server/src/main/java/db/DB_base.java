package db;

//базовый класс модуля доступа к БД

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public abstract class DB_base {

    protected String driver = null;         //драйвер
    protected String url = null;            //строка подключения
    protected Properties properties = null; //свойства подключения

    public DB_base(String driver){
        this.driver=driver;
    }

    //регистрация драйвера
    protected void registerDriveManager(){
        try{
            Class.forName(driver).newInstance();
        }catch (Exception e){ e.printStackTrace();}
    }

    //определение строки подключения
    public abstract void setURL (String host, String database, int port);

    //получение объекта подключения
    public abstract Connection getConnection();

    //регистрация драйвера подключения к серверу БД и определение свойств
    public void connect(String login, String password){

        registerDriveManager();
        properties = new Properties();
        properties.setProperty("password", password);
        properties.setProperty("user", login);
        properties.setProperty("useUnicode", "true");
        properties.setProperty("characterEncoding", "utf8");

    }

    //отключение от сервера БД
    public void disconnect(Connection connection){
        try{
            connection.close();
            connection=null;
        }catch (SQLException e) {
            System.err.println("SQLException: code = " + String.valueOf(e.getErrorCode()+ e.getMessage()));
        }
    }

    //функция выполнениея sql-запроса
    public boolean execSQL(final String sql){
        boolean result = false;
        try{
            if(getConnection()!=null){
                Statement statement = getConnection().createStatement();
                statement.execute(sql);
                statement.close();
                statement=null;
                result=true;
            }
        } catch (SQLException e){
            System.err.println("SQLException: code = " + String.valueOf(e.getErrorCode()+ e.getMessage()));
            System.err.println("SQL: " + sql);
        }
        return result;
    }
}

