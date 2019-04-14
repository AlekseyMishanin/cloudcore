package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.User;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class OperatingPanelController {

    @FXML private GridPane rootGrid;
    @FXML private TreeView<extFile> treeVievClient;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label labelStatus;
    @FXML private ContextMenu cMenu;
    @FXML private ContextMenuController contextMenuController;

    public void initialize() throws IOException {
        File[] roots = File.listRoots();
        treeVievClient.setRoot(new TreeItem<>(new extFile(User.SETTING.getName())));
        for (int i = 0; i < roots.length; i++) {
            TreeItem<extFile> rootChild = new TreeItem<>(new extFile(roots[i].getPath()));
            treeVievClient.getRoot().getChildren().addAll(rootChild);
        }
        treeVievClient.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TreeItem<extFile> newV = newValue;
                    if(!newV.getValue().canRead()) return;
                    if (!newV.getValue().isDirectory()) {
                    } else {
                        ObservableList<TreeItem<extFile>> list = FXCollections.observableArrayList();
                        if (newV.getValue().list()==null) return;
                        int size = newV.getValue().list().length;

                        Platform.runLater(() -> {
                            progressIndicator.setProgress(0f);
                            labelStatus.setText("Чтение каталога " + newV.getValue());
                        });

                        for (int i = 0; i < size; i++) {
                            int loc = i + 1;
                            Platform.runLater(() -> progressIndicator.setProgress((loc) * 1.0 / size));
                            list.add(new TreeItem<>(new extFile(newValue.getValue(), newValue.getValue().list()[loc-1])));
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
    }
    class extFile extends File {
        public extFile(String pathname) {
            super(pathname);
        }

        public extFile(File parent, String child) {
            super(parent, child);
        }

        @Override
        public String toString() {
            return getName().isEmpty() ? getPath() : getName();
        }
    }

    //класс можно адаптировать для поиска или удаления файлов/каталогов
    class MyFileVision extends SimpleFileVisitor<Path> {

        private String parentDirectory = new String();

        public MyFileVision(String parentDirectory){
            this.parentDirectory=parentDirectory;
        }

        public FileVisitResult visitFile(Path path, BasicFileAttributes attrib) throws IOException {
            //treeVievClient.
            //System.out.println(path);
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrib) throws IOException{
           // System.out.println(path);
            return FileVisitResult.CONTINUE;
        }
    }

    //экспериментальный класс, загружает рекурсивно в приложение файловую структуру локального компьютера. Не производительный метод
    private void createItemForTree(TreeItem<String> parent, File file){
        TreeItem<String> child = new TreeItem<>(file.toPath().getFileName().toString());
        parent.getChildren().addAll(child);
        if(file.isDirectory()){
            if(file.list()==null) return;
            for (String str:
                    file.list()) {
                createItemForTree(child, new File(file.getAbsolutePath() + File.separator + str));
            }
        }
    }
}

