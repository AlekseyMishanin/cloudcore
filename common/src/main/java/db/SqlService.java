package db;

import lombok.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

/**
 * Класс отвечает за работу с БД. Содержит ряд методов с sql-запросами для выполения операций: добавить/удалить/
 * копировать/вырезать/переименовать каталог/файл и т.д.
 *
 * @author Mishanin Aleksey
 * */
public class SqlService {

    private static SqlService sqlService = new SqlService();
    private Statement statement = null;
    private Connection connection = null;

    private SqlService(){
        try {
            this.connection = DataSource.getInstance().getConnection();
            this.statement = DataSource.getInstance().getStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static SqlService getInstance(){ return sqlService;}


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

    private boolean isDuplicate(@NonNull String path, int id){
        String sql = String.format("SELECT id FROM test.reestr WHERE path = CAST(\"%s\" AS CHAR CHARACTER SET utf8) AND iduser = %d",path,id);
        try {
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertNewCatalog(@NonNull String path, String parentPath, int id){
        if(!isDuplicate(path,id) && (parentPath != null ? isDirectory(parentPath,id) : true)){
            String sql = String.format("INSERT INTO test.reestr (iduser, path) VALUES (%d, \"%s\")", id, path);
            try {
                int rs = statement.executeUpdate(sql);
                return rs != 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String buildStructureCatalog(int iduser){
        if(iduser <= 0 ) return null;
        //SELECT CASE WHEN fl.name IS NOT NULL THEN CONCAT(rs.path,"/",fl.name) ELSE rs.path END
        String sql = String.format("SELECT rs.path " +
                "                   FROM test.reestr AS rs LEFT JOIN test.files AS fl " +
                "                   ON rs.idfile = fl.id WHERE rs.iduser = %d ORDER BY 1 ASC", iduser);
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

    private boolean isDirectory(String path, int id){
        String sql = String.format("SELECT id FROM test.reestr WHERE path =\"%s\" AND iduser = %d AND idfile IS NULL", path, id);
        try {
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isFileExists(String catalog, String nameFile, int id){
        String sql = String.format("SELECT fl.id FROM test.reestr AS rs INNER JOIN test.files AS fl " +
                "ON rs.idfile=fl.id WHERE rs.path = CAST(\"%s\" AS CHAR CHARACTER SET utf8) " +
                "AND rs.iduser = %d AND fl.name = CAST(\"%s\" AS CHAR CHARACTER SET utf8) AND rs.idfile IS NOT NULL", catalog, id, nameFile);
        try {
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCatalog(String catalog, int id){
//        String sql = String.format("DELETE test.files, test.reestr " +
//                "FROM test.reestr LEFT JOIN test.files " +
//                "ON test.reestr.idfile=test.files.id WHERE test.reestr.iduser = %d AND test.reestr.path LIKE '%s%%'", id, catalog);
        String sql = String.format("DELETE FROM test.reestr " +
                "WHERE test.reestr.iduser = %d AND test.reestr.path LIKE '%s%%'", id, catalog);

        try {
            int rs = statement.executeUpdate(sql);
            return rs > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertNewFile(String catalog, String name, long sizeFile, int id, String pathToFile) {
        if(!isDuplicate(catalog + "/" + name, id) &&
                (catalog != null ? isDirectory(catalog,id) : true) &&
                !isFileExists(catalog,name,id)){
            String sql1 = "INSERT INTO test.files (name, size, file) VALUES (CAST( ? AS CHAR CHARACTER SET utf8), ?, ?)";
            String sql2 = String.format("SELECT id FROM test.files WHERE name = CAST(\"%s\" AS CHAR CHARACTER SET utf8) AND size = %d ORDER BY 1 DESC LIMIT 1", name, sizeFile);
            FileInputStream fis = null;
            PreparedStatement ps = null;
            boolean result = false;
            try{
                connection.setAutoCommit(false);
                File file = new File(pathToFile);
                fis = new FileInputStream(file);
                ps = connection.prepareStatement(sql1);
                ps.setString(1, name);
                ps.setLong(2, sizeFile);
                ps.setBinaryStream(3, fis, (int)file.length());
                ps.executeUpdate();
                connection.commit();
                connection.setAutoCommit(true);

                ResultSet rs = statement.executeQuery(sql2);
                rs.next();
                int idFile = rs.getInt(1);

                String sql3 = String.format("INSERT INTO test.reestr (iduser, idfile, path) VALUES (%d, %d, \"%s\")",id, idFile, catalog + "/" + name);
                statement.executeUpdate(sql3);
                result = true;

            } catch (Exception e){
                e.printStackTrace();
                result = false;
            } finally {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }
        return false;
    }

    public long selectSizeFile(String path, String delim, int id){

        if(path == null || path.isEmpty()) return 0;
        //String nameFile = path.substring(path.lastIndexOf(delim)+1);
        String sql = String.format("SELECT size FROM test.files AS fl INNER JOIN test.reestr AS rs " +
                "ON fl.id=rs.idfile AND rs.path =\"%s\"", path);
        try {
            ResultSet rs = statement.executeQuery(sql);
            if(rs.next()){
                long size = rs.getLong(1);
                return size;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public InputStream selectFile(String path, String delim, int id){

        if(path == null || path.isEmpty()) return null;
        String nameFile = path.substring(path.lastIndexOf(delim)+1);
        String sql = String.format("SELECT file FROM test.files AS fl INNER JOIN test.reestr AS rs " +
                "ON fl.id=rs.idfile AND rs.path =\"%s\" AND fl.name =\"%s\"", path, nameFile);
        try {
            ResultSet rs = statement.executeQuery(sql);
            if(rs.next()) {
                Blob file = rs.getBlob(1);
                return file.getBinaryStream();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertCopyPath(String oldPath, String newPath, String delim, int id){
        if (isDirectory(newPath, id)){
            newPath+=delim;
            int indexLastDelimiter = oldPath.lastIndexOf(delim)+2;
            String sql = String.format("INSERT INTO test.reestr (iduser, idfile, path) " +
                    "SELECT iduser, idfile, CONCAT(\"%s\", SUBSTRING(path, %d, LENGTH(path) - %d + 1)) " +
                    "FROM test.reestr " +
                    "WHERE test.reestr.path LIKE '%s%%' AND test.reestr.iduser =%d",newPath, indexLastDelimiter, indexLastDelimiter,oldPath, id);
            try {
                int rs = statement.executeUpdate(sql);
                return rs != 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean deleteOldPath(String oldPath, int id){
        String sql = String.format("DELETE FROM test.reestr WHERE id>0 AND iduser =%d AND path LIKE '%s%%'", id, oldPath);
        try {
            int rs = statement.executeUpdate(sql);
            return rs != 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean renamePath(String oldPath, String newPath, String delim, int id){

        int indexLastDelimiter = oldPath.lastIndexOf(delim);
        int length = oldPath.length();

        String sql = String.format("UPDATE test.reestr " +
                        "SET path = CONCAT(SUBSTRING(path,1,%d+1), \"%s\", SUBSTRING(path, %d, LENGTH(path) - %d)) " +
                        "WHERE id>0 AND iduser = %d AND path LIKE '%s%%'",
                indexLastDelimiter, newPath, length, length, id, oldPath);
        try {
            int rs = statement.executeUpdate(sql);
            return rs != 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
