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
import utility.TreeViewUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Observable;
import java.util.Observer;

public class OperatingPanelController implements Observer {

    @FXML private GridPane rootNode;
    @FXML private TreeView<ExtFile> treeVievClient;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label labelStatus;
    @FXML private ContextMenuController cMenuController;    //дочерний контроллер (контекстное меню)

    public void initialize() throws IOException {

        File[] roots = File.listRoots();
        treeVievClient.setRoot(new TreeItem<>(new ExtFile(User.SETTING.getName())));
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
        cMenuController.addObserver(this::update); //регистрируем слушателя
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
        if(((Observable)cMenuController).equals(o) ){
            switch ((MenuCommand)arg){
                case NEWFILE:
                    TreeViewUtility.createNewFileAndTreeItem(treeVievClient);
                    break;
                case NEWCATALOG:
                    TreeViewUtility.createNewCatalogAndTreeItem(treeVievClient);
                    break;
                case MOVE:
                    TreeViewUtility.moveFileAndTreeItem(treeVievClient);
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
    }
}

