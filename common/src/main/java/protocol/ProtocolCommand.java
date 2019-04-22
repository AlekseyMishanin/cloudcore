package protocol;

public enum ProtocolCommand {
    FILE((byte)1),
    FILEREQUEST((byte)2),
    FILERESPONSE((byte)3),
    FILEERROR((byte)4);

    ProtocolCommand(byte i){
        data = i;
    }

    private byte data;

    public byte getData() {
        return data;
    }

    static ProtocolCommand getCommand(byte value){
        for (ProtocolCommand temp:
             ProtocolCommand.values()) {
            if(temp.data==value) return temp;
        }
        return null;
    }
}
