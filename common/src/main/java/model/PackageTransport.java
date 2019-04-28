package model;

import java.nio.file.Path;

/**
 * Класс инкапсулирует облегченный пакет данных отправляемых на сервер
 * */
public class PackageTransport {

    private String user;            //имя пользователя
    private Path pathToFile;        //путь к файлу

    public PackageTransport(String user, Path pathToFile) {
        this.user = user;
        this.pathToFile = pathToFile;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Path getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(Path pathToFile) {
        this.pathToFile = pathToFile;
    }
}
