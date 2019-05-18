package model;

/**
 * Класс инкапсулирует протокол.
 *
 * @author Mishanin Aleksey
 * */
public class PackageBody {

    //набор состояний определяющих логику следующего шага протокола
    public enum Status{
        NONE,                   //нет команды
        READPASSWORD,           //читаем пароль
        READLENGTHUSER,         //читаем длину имени пользователя
        READNAMEUSER,           //читаем имя пользователя
        READLENGTHNAMEFILE,     //читаем длину имени файла
        READNAMEFILE,           //читаем имя файла
        READLENGTHFILE,         //читаем длину файла
        READFILE,               //читаем файл
        WRITEFILE,              //записываем файл
        READBOOLRESPONSE,       //читаем булевый ответ (используется при авторизации и регистрации)
        BUILDSTRUCTURECATALOG,  //строим структуру каталогов/файлов
        READLENGTHSTRUCTURE,    //читаем длину струкруты
        READSTRINGSTRUCTURE,    //читаем строку струкруты
        BUILDSTRUCTURE,         //строим структуру каталогов на стороне клиента
        READLENGTHPATHCATALOG,  //читаем длину нового каталога
        READPATHNEWCATALOG,     //читаем путь нового каталога
        CREATENEWCATALOG,       //создаем новый каталог
        CHANGEDSTRUCTURE,       //структура каталогов изменилась
        READLENGTHDELETECATALOG,    //читаем длину пути каталога для удаления
        READNAMEDELETECATALOG,      //читаем путь каталога для удаления
        DELETECATALOG,              //удаляем каталог
        READLENGTHCATALOGFORFILE,   //читаем длину каталога для вставки файла
        READNAMECATALOGFORFILE,     //читаем путь каталога для вставки файла
        READLENGTHOLDPATH,          //чидаем длину старого пути
        READLENGTHNEWPATH,          //читаем длину нового пути
        READNAMEOLDPATH,            //читаем старый путь
        READNAMENEWPATH,            //читаем новый путь
        OPERATIONNAMEPATH,          //переименовываем путь
        READCHECKSUM;               //читаем контрольную сумму
    }

    static public final String systemSeparator = "/";

    private ProtocolCommand command;    //команда протокола
    private Status status;              //состояние протокола
    private int lenghUserName;          //длина имени пользователя
    private int lenghFileName;          //длина имени файла
    private long lenghFile;             //длина файла
    private long checksum;              //контрольная сумма
    private int lengthStructure;        //длина структуры каталогов
    private String structureCatalog;    //структура каталогов
    private int lengthVariable;
    private String variable;
    private String nameFile;            //имя файла
    private String pasteCatalog;        //имя каталога для вставки
    private int lengthPasteCatalog;     //длина имени каталога для вставки

    public PackageBody() {
        this.command = null;
        this.status = Status.NONE;
    }

    public ProtocolCommand getCommand() {
        return command;
    }

    public void setCommand(ProtocolCommand command) {
        this.command = command;
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

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public int getLengthStructure() {
        return lengthStructure;
    }

    public void setLengthStructure(int lengthStructure) {
        this.lengthStructure = lengthStructure;
    }

    public String getStructureCatalog() {
        return structureCatalog;
    }

    public void setStructureCatalog(String structureCatalog) {
        this.structureCatalog = structureCatalog;
    }

    public int getLengthVariable() { return lengthVariable; }

    public void setLengthVariable(int lengthVariable) { this.lengthVariable = lengthVariable; }

    public String getVariable() { return variable; }

    public void setVariable(String variable) { this.variable = variable; }

    public static String getSystemSeparator() {
        return systemSeparator;
    }

    public String getPasteCatalog() {
        return pasteCatalog;
    }

    public void setPasteCatalog(String pasteCatalog) {
        this.pasteCatalog = pasteCatalog;
    }

    public int getLengthPasteCatalog() {
        return lengthPasteCatalog;
    }

    public void setLengthPasteCatalog(int lengthPasteCatalog) {
        this.lengthPasteCatalog = lengthPasteCatalog;
    }

    public long getChecksum() { return checksum; }

    public void setChecksum(long checksum) { this.checksum = checksum; }

    /**
     * Метод очищает основные поля класса.
     * */
    public void clear(){
        command = null;
        status = Status.NONE;
        lenghFile =
                checksum = 0;
        lengthPasteCatalog =
        lenghUserName =
        lenghFileName =
        lengthStructure =
        lengthVariable = 0;
        pasteCatalog =
        nameFile =
        structureCatalog =
        variable = null;
    }

    @Override
    public String toString() {
        return command.toString() + "\n" +
                status.toString() + "\n" +
                lenghUserName + "\n" +
                lenghFileName + "\n" +
                lenghFile + "\n" +
                nameFile + "\n";
    }
}
