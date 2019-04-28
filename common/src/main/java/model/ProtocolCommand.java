package model;

/**
 * Перечисление описывает основные команды доступные для использования в протоколе
 *
 * @author Mishanin Aleksey
 * */
public enum ProtocolCommand {
    FILE((byte)1),              //отправка файла на сервер
    FILEREQUEST((byte)2),       //запрос файла у сервера
    FILERESPONSE((byte)3),      //ответ сервера клиенту. Успешная отправка файла от сервера клиенту
    FILEERROR((byte)4);         //сбой при отправке файла

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
