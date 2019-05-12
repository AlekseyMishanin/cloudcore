package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.ExtFile;
import model.MenuCommand;
import model.User;
import net.NettyNetwork;
import utility.ListController;
import utility.LoadFiles;
import utility.TreeViewUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Observable;
import java.util.Observer;

public class OperatingPanelController implements Observer {

    @FXML private GridPane rootNode;
    @FXML private TreeView<ExtFile> treeVievClient;
    @FXML private TreeView<ExtFile> treeVievCloud;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label labelStatus;
    @FXML private ContextMenuController clientMenuController;    //дочерний контроллер (контекстное меню)
    @FXML private ContextMenuController cloudMenuController;     //дочерний контроллер (контекстное облака)
    @FXML private MenuBarClientController barClientController;   //дочерний контроллер (меню бар клиента)
    @FXML private MenuBarCloudController barCloudController;     //дочерний контроллер (меню бар облака)

    public void initialize() throws IOException {

        //присваиваем списку контроллеров ссылку на текущий контроллер
        ListController.getInstance().setOperatingPanelController(this);


        File[] roots = File.listRoots();
        treeVievClient.setRoot(new TreeItem<>(new ExtFile(User.SETTING.getName())));
        treeVievCloud.setRoot(new TreeItem<>(new ExtFile(User.SETTING.getName())));

        //запрашиваем у сервера структуру каталогов/файлов
        NettyNetwork.getInstance().requestDirectoryStructure();

        for (int i = 0; i < roots.length; i++) {
            TreeItem<ExtFile> rootChild = new TreeItem<>(new ExtFile(roots[i].getPath()));
            treeVievClient.getRoot().getChildren().addAll(rootChild);
        }
        treeVievClient.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TreeItem<ExtFile> newV = newValue;
                    if(newV==null || !newV.getValue().canRead()) return;
                    if (!newV.getValue().isDirectory()) {
                    } else {
                        ObservableList<TreeItem<ExtFile>> list = FXCollections.observableArrayList();
                        if (newV.getValue().list()==null) return;
                        int size = newV.getValue().list().length;

                        Platform.runLater(() -> {
                            progressIndicator.setProgress(0f);
                            labelStatus.setText("Чтение каталога " + newV.getValue());
                        });

                        for (int i = 0; i < size; i++) {
                            int loc = i + 1;
                            Platform.runLater(() -> progressIndicator.setProgress((loc) * 1.0 / size));
                            list.add(new TreeItem<>(new ExtFile(newValue.getValue(), newValue.getValue().list()[loc-1])));
                        }

                        Platform.runLater(() -> {
                            labelStatus.setText("Каталог " + newV.getValue() + " обновлен.");
                            progressIndicator.setProgress(1f);
                        });
                        newV.getChildren().setAll(list);
                    }
                }
            }).start();
        });
        barClientController.addObserver(this::update);  //регистрируем слушателя
        barCloudController.addObserver(this::update);   //регистрируем слушателя
        clientMenuController.addObserver(this::update); //регистрируем слушателя
        cloudMenuController.addObserver(this::update);  //регистрируем слушателя
        cloudMenuController.hideNewFile();
        cloudMenuController.hideSearch();
    }

    @Override
    public void update(Observable o, Object arg) {
        //проверяем то, что уведомление пришло от контроллера контекстного меню
        if(((Observable)clientMenuController).equals(o) ){
            switch ((MenuCommand)arg){
                case NEWFILE:
                    TreeViewUtility.createNewFileAndTreeItem(treeVievClient);
                    break;
                case NEWCATALOG:
                    TreeViewUtility.createNewCatalogAndTreeItem(treeVievClient);
                    break;
                case COPY:
                    TreeViewUtility.copyFileAndTreeItem(treeVievClient);
                    break;
                case CUT:
                    TreeViewUtility.cutFileAndTreeItem(treeVievClient);
                    break;
                case PASTE:
                    TreeViewUtility.pasteFileAndTreeItem(treeVievClient);
                    break;
                case REMANE:
                    TreeViewUtility.renameFileAndTreeItem(treeVievClient);
                    break;
                case DELETE:
                    TreeViewUtility.deleteObjectAndTreeItem(treeVievClient);
                    break;
                case SEARCH:
                    TreeViewUtility.searchObjectAndTreeItem(treeVievClient);
                    break;
                case UPLOAD:
                    LoadFiles.getInstance().uploadFileClient(rootNode.getScene().getWindow());
                    break;
                case DOWNLOAD:
                    LoadFiles.getInstance().downloadFileClient(rootNode.getScene().getWindow());
                    break;
            }
        }
        if(((Observable)cloudMenuController).equals(o) ){
            switch ((MenuCommand)arg){
                case NEWCATALOG:
                    String newPath = TreeViewUtility.createNewCatalogInCloud(treeVievCloud);
                    if(newPath!=null){
                        NettyNetwork.getInstance().requestCteareNewCatalog(newPath);
                    }
                    break;
                case COPY:
                    TreeViewUtility.copyFileCloud(treeVievCloud);
                    break;
                case CUT:
                    TreeViewUtility.cutFileCloud(treeVievCloud);
                    break;
                case PASTE:
                    TreeViewUtility.pasteFileCloud(treeVievCloud);
                    break;
                case REMANE:
                    TreeViewUtility.renameFileCloud(treeVievCloud);
                    break;
                case DELETE:
                    String pathToCatalog = TreeViewUtility.deleteDirectoryInCloud(treeVievCloud);
                    if(pathToCatalog != null){
                        NettyNetwork.getInstance().requestDeleteCatalog(pathToCatalog);
                    }
                    break;
                case UPLOAD:
                    LoadFiles.getInstance().uploadFileServer(treeVievCloud);
                    break;
                case DOWNLOAD:
                    try {
                        LoadFiles.getInstance().downloadFileServer(treeVievCloud);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        if(((Observable)barClientController).equals(o) ){
            switch ((MenuCommand)arg){
                case TREEVISIBLE:
                    treeVievClient.setVisible(barClientController.isVisibleTree());
                    treeVievClient.setManaged(barClientController.isVisibleTree());
                    break;
                case COPY:
                    TreeViewUtility.copyFileAndTreeItem(treeVievClient);
                    break;
                case CUT:
                    TreeViewUtility.cutFileAndTreeItem(treeVievClient);
                    break;
                case PASTE:
                    TreeViewUtility.pasteFileAndTreeItem(treeVievClient);
                    break;
                case EXIT:
                    ListController.getInstance().setOperatingPanelController(null);
                    ListController.getInstance().setAuthorisationController(null);
                    NettyNetwork.getInstance().close();
                    Platform.exit();
                    break;
                case UPLOAD:
                    LoadFiles.getInstance().uploadFileClient(rootNode.getScene().getWindow());
                    break;
                case DOWNLOAD:
                    LoadFiles.getInstance().downloadFileClient(rootNode.getScene().getWindow());
                    break;
                case ABOUT:
                case MANUAL:
                case SEARCH:
                    //в разработке
            }
        }
        if(((Observable)barCloudController).equals(o) ){
            switch ((MenuCommand)arg){
                case TREEVISIBLE:
                    treeVievCloud.setVisible(barCloudController.isVisibleTree());
                    treeVievCloud.setManaged(barCloudController.isVisibleTree());
                    break;
                case COPY:
                    TreeViewUtility.copyFileCloud(treeVievCloud);
                    break;
                case CUT:
                    TreeViewUtility.cutFileCloud(treeVievCloud);
                    break;
                case PASTE:
                    TreeViewUtility.pasteFileCloud(treeVievCloud);
                    break;
                case EXIT:
                    ListController.getInstance().setOperatingPanelController(null);
                    ListController.getInstance().setAuthorisationController(null);
                    NettyNetwork.getInstance().close();
                    Platform.exit();
                    break;
                case UPLOAD:
                    LoadFiles.getInstance().uploadFileServer(treeVievCloud);
                    break;
                case DOWNLOAD:
                    try {
                        LoadFiles.getInstance().downloadFileServer(treeVievCloud);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case ABOUT:
                case MANUAL:
                case SEARCH:
                    //в разработке
            }
        }
    }

    public void updateTreeViewCloud(String struct){
        TreeViewUtility.updateStructureTreeViewCloud(treeVievCloud,struct);
    }
}

