package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import model.MenuCommand;

import java.util.Observable;

/**
 * Контроллер MenuBar облака. При каждом нажатии уведомляет слушателя о наступлении события
 *
 * @author Mishanin Aleksey
 * */
public class MenuBarCloudController extends Observable {

    @FXML private MenuItem upload;
    @FXML private MenuItem download;
    @FXML private CheckMenuItem treeVisible;
    @FXML private MenuItem exit;
    @FXML private MenuItem copy;
    @FXML private MenuItem cut;
    @FXML private MenuItem paste;
    @FXML private MenuItem search;
    @FXML private MenuItem manual;
    @FXML private MenuItem about;

    public boolean isVisibleTree(){
        return treeVisible.isSelected();
    }

    public void getMenuCommand(ActionEvent actionEvent) {
        setChanged();
        switch (((MenuItem)actionEvent.getSource()).getId()){
            case "treeVisible":
                notifyObservers(MenuCommand.TREEVISIBLE);
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
            case "exit":
                notifyObservers(MenuCommand.EXIT);
                break;
            case "manual":
                notifyObservers(MenuCommand.MANUAL);
                break;
            case "about":
                notifyObservers(MenuCommand.ABOUT);
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
}
