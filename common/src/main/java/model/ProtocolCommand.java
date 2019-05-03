package model;

/**
 * Перечисление описывает основные команды доступные для использования в протоколе
 *
 * @author Mishanin Aleksey
 * */
public enum ProtocolCommand {
    FILE((byte)1),              //клиент отправил файл на сервер
    FILEREQUEST((byte)2),       //клиент запрашивает файл у сервера
    FILERESPONSE((byte)3),      //сервер отвечает на запрос клиента (файл)
    FILEERROR((byte)4),         //сбой при отправке файла
    AUTHORIZATION((byte)5),     //клиент запрашивает авторизацию
    REGISTRATION((byte)6),      //клиент запрашивает регистрацию
    AUTHRESPONSE((byte)7),      //ответ сервера на запрос клиента (авторизация)
    REGRESPONSE((byte)8),       //ответ сервера на запрос клиента (регистрация)
    STRUCTUREREQUEST((byte)9),  //клиент запрашивает структуру каталогов/файлов
    DENIED((byte)10),           //ответ сервера (отказано в доступе)
    STRUCTURERESPONSE((byte)11),    //положительный ответ сервера на запрос структуры каталогов/файлов
    NEWCATALOG((byte)12);           //запрос на создание нового каталога

    ProtocolCommand(byte i){
        data = i;
    }

    private byte data;

    public byte getData() {
        return data;
    }

    /**
     * Статический метод, который позволяет по значению байта получить
     * объект типа ProtocolCommand
     *
     * @param   value
     *          значение байта
     *
     * @return  объект ProtocolCommand соответствующий значению байта
     * */
    public static ProtocolCommand getCommand(byte value){
        for (ProtocolCommand temp:
             ProtocolCommand.values()) {
            if(temp.data==value) return temp;
        }
        return null;
    }
}
