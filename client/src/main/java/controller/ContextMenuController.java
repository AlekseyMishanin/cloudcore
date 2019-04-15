package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import model.MenuCommand;

import java.util.Observable;

public class ContextMenuController extends Observable {

    public void getMenuCommand(ActionEvent actionEvent) {
        setChanged();
        switch (((MenuItem)actionEvent.getSource()).getId()){
            case "newFile":
                notifyObservers(MenuCommand.NEWFILE);
                break;
            case "newCatalog":
                notifyObservers(MenuCommand.NEWCATALOG);
                break;
            case "move":
                notifyObservers(MenuCommand.MOVE);
                break;
            case "copy":
                notifyObservers(MenuCommand.COPY);
                break;
            case "cut":
                notifyObservers(MenuCommand.CUT);
                break;
            case "paste":
                notifyObservers(MenuCommand.PASTE);
                break;
            case "remname":
                notifyObservers(MenuCommand.REMANE);
                break;
            case "delete":
                notifyObservers(MenuCommand.DELETE);
                break;
            case "search":
                notifyObservers(MenuCommand.SEARCH);
                break;
        }
    }
}
