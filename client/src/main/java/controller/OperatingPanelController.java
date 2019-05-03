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
    @FXML private ContextMenuController cloudMenuController;     //дочерний контроллер (контекстное меню)

    public void initialize() throws IOException {

        //присваиваем списку контроллеров ссылку на текущий контроллер
        ListController.getInstance().setOperatingPanelController(this);
        //запрашиваем у сервера структуру каталогов/файлов
        NettyNetwork.getInstance().requestDirectoryStructure();

        File[] roots = File.listRoots();
        treeVievClient.setRoot(new TreeItem<>(new ExtFile(User.SETTING.getName())));
        treeVievCloud.setRoot(new TreeItem<>(new ExtFile(User.SETTING.getName())));
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
        clientMenuController.addObserver(this::update); //регистрируем слушателя
        cloudMenuController.addObserver(this::update);  //регистрируем слушателя
        cloudMenuController.hideNewFile();
        cloudMenuController.hideSearch();
    }

    //класс можно адаптировать для поиска или удаления файлов/каталогов


    //экспериментальный класс, загружает рекурсивно в приложение файловую структуру локального компьютера. Не производительный метод
//    private void createItemForTree(TreeItem<String> parent, File file){
//        TreeItem<String> child = new TreeItem<>(file.toPath().getFileName().toString());
//        parent.getChildren().addAll(child);
//        if(file.isDirectory()){
//            if(file.list()==null) return;
//            for (String str:
//                    file.list()) {
//                createItemForTree(child, new File(file.getAbsolutePath() + File.separator + str));
//            }
//        }
//    }

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
            }
        }
        if(((Observable)cloudMenuController).equals(o) ){
            switch ((MenuCommand)arg){
                case NEWCATALOG:
                    TreeViewUtility.createNewCatalogInCloud(treeVievCloud);
                    break;
                case COPY:
                    System.out.println(2);
                    break;
                case CUT:
                    System.out.println(3);
                    break;
                case PASTE:
                    System.out.println(4);
                    break;
                case REMANE:
                    System.out.println(5);
                    break;
                case DELETE:
                    System.out.println(6);
                    break;
            }
        }
    }

    public void createTreeViewCloud(String struct){
        if (struct ==null){
            System.out.println("null");
        } else {
            System.out.println(struct);
        }
        //дописать метод на тот случай если возвращается не пустая строка со структурой каталогов
    }
}

