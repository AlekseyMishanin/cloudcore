package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Класс отвечает за создание пула соединений с БД.
 *
 * @author Mishanin Aleksey
 * */
public class DataSource {

    private static DataSource dataSource = new DataSource();
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        config.setJdbcUrl( "jdbc:mysql://localhost/test" );
        config.setUsername( "test" );
        config.setPassword( "test" );
        config.setMaximumPoolSize(10);
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( config );
    }
    private DataSource() {

    }

    public static DataSource getInstance(){
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public Statement getStatement() throws SQLException {
        return ds.getConnection().createStatement();
    }
}
