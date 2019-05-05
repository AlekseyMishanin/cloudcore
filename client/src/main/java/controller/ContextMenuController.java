package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import model.MenuCommand;

import java.util.Observable;

public class ContextMenuController extends Observable {

    @FXML private MenuItem newFile;
    @FXML private MenuItem newCatalog;
    @FXML private MenuItem copy;
    @FXML private MenuItem cut;
    @FXML private MenuItem paste;
    @FXML private MenuItem remname;
    @FXML private MenuItem delete;
    @FXML private MenuItem search;

    public void getMenuCommand(ActionEvent actionEvent) {
        setChanged();
        switch (((MenuItem)actionEvent.getSource()).getId()){
            case "newFile":
                notifyObservers(MenuCommand.NEWFILE);
                break;
            case "newCatalog":
                notifyObservers(MenuCommand.NEWCATALOG);
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
            case "upload":
                notifyObservers(MenuCommand.UPLOAD);
                break;
            case "download":
                notifyObservers(MenuCommand.DOWNLOAD);
                break;
        }
    }

    public void hideNewFile(){
        newFile.setDisable(true);
    }

    public void hideSearch(){
        search.setDisable(true);
    }
}
