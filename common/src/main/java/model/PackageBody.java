package model;

/**
 * Класс позволяет из потока байт построить пакет на стороне сервера или клиента.
 *
 * @author Mishanin Aleksey
 * */
public class PackageBody {

    //набор состояний определяющих логику следующего шага протокола
    public enum Status{
        NONE,                   //нет команды
        READLENGTHUSER,         //читаем длину имени пользователя
        READNAMEUSER,           //читаем имя пользователя
        READLENGTHNAMEFILE,     //читаем длину имени файла
        READNAMEFILE,           //читаем имя файла
        READLENGTHFILE,         //читаем длину файла
        READFILE,               //читаем файл
        WRITEFILE;              //записываем файл
    }

    private ProtocolCommand command;    //команда протокола
    private Status status;              //состояние протокола
    private int lenghUserName;          //длина имени пользователя
    private int lenghFileName;          //длина имени файла
    private long lenghFile;             //длина файла
    private String nameUser;            //имя пользователя
    private String nameFile;            //имя файла

    @Override
    public String toString() {
        return command.toString() + "\n" +
                status.toString() + "\n" +
                lenghUserName + "\n" +
                lenghFileName + "\n" +
                lenghFile + "\n" +
                nameUser + "\n" +
                nameFile + "\n";
    }

    public PackageBody() {
        this.command = null;
        this.status = Status.NONE;
    }

    public ProtocolCommand getCommand() {
        return command;
    }

    public void setCommand(ProtocolCommand command) {

        this.command = command;
        this.status = Status.READLENGTHUSER;
    }


    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getLenghUserName() {
        return lenghUserName;
    }

    public void setLenghUserName(int lenghUserName) {
        this.lenghUserName = lenghUserName;
    }

    public int getLenghFileName() {
        return lenghFileName;
    }

    public void setLenghFileName(int lenghFileName) {
        this.lenghFileName = lenghFileName;
    }

    public long getLenghFile() {
        return lenghFile;
    }

    public void setLenghFile(long lenghFile) {
        this.lenghFile = lenghFile;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    /**
     * Метод очищает основные поля класса.
     * */
    public void clear(){
        command = null;
        status = Status.NONE;
        lenghFile = lenghFileName = lenghUserName = 0;
        nameUser = null;
        nameFile = null;
    }
}
