package protocol;

import java.nio.file.Path;

public class ProtocolFile implements ProtocolAction {

    private enum State{CLIENT,SERVER;}

    private ProtocolCommand command;
    private String userName;
    private Path file;
    private State state;

    public ProtocolFile(ProtocolCommand command, String userName, Path file) {
        this.command = command;
        this.userName = userName;
        this.file = file;
    }

    public void setClient(){
        state = State.CLIENT;
    }

    public void setServer(){
        state = State.SERVER;
    }

    @Override
    public void getFile() {
        switch (state){
            case CLIENT:
                break;
            case SERVER:
                break;
        }
    }

    @Override
    public void sendFile() {
        switch (state){
            case CLIENT:
                break;
            case SERVER:
                break;
        }
    }
}
