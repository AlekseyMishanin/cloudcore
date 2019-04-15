package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import logger.LoggerCloud;
import model.MenuCommand;
import model.User;
import utility.TreeViewUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

public class OperatingPanelController implements Observer {

    @FXML private GridPane rootNode;
    @FXML private TreeView<extFile> treeVievClient;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label labelStatus;
    @FXML private ContextMenuController cMenuController;    //дочерний контроллер (контекстное меню)

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
                    if(newV==null || !newV.getValue().canRead()) return;
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
        cMenuController.addObserver(this::update); //регистрируем слушателя
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

    @Override
    public void update(Observable o, Object arg) {
        //проверяем то, что уведомление пришло от контроллера контекстного меню
        if(((Observable)cMenuController).equals(o) ){
            switch ((MenuCommand)arg){
                case NEWFILE:
                    break;
                case NEWCATALOG:
                    break;
                case MOVE:
                    break;
                case COPY:
                    break;
                case CUT:
                    break;
                case PASTE:
                    break;
                case REMANE:
                    TreeItem<extFile> obj = treeVievClient.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
                    if(obj != null){
                        Path source = Paths.get(TreeViewUtility.proba(obj));                                //получаем полный путь к выбранному элементу
                        TextInputDialog textInputDialog = new TextInputDialog("");
                        textInputDialog.setTitle("New name");
                        textInputDialog.setHeaderText("Help: the rules for writing a name are defined by the file system");
                        textInputDialog.setContentText("Please, enter new name:");
                        Optional<String> res = textInputDialog.showAndWait();
                        if(res.isPresent())
                        {
                            String newName = res.get();
                            try {
                                Files.move(source, source.resolveSibling(newName));                           //переименовываем элемент
                                TreeItem<extFile> objP = obj.getParent();                                       //определяем родителя выбранного элемента
                                ObservableList<TreeItem<extFile>> listChild = obj.getChildren();                //определяем детей выбранного элемента

                                Platform.runLater(() ->{
                                    objP.getChildren().remove(obj);                                             //удаляем выбранный элемент
                                    TreeItem<extFile> var = new TreeItem<>(new extFile(newName));     //создаем элемент с новым именем
                                    var.getChildren().setAll(listChild);                                            //принимаем всех детей
                                    objP.getChildren().add(var);                                                    //привязываем родителя
                                });
                                
                            } catch (FileAlreadyExistsException e){
                                Alert alert = new Alert(Alert.AlertType.WARNING, "File exists!", ButtonType.OK);
                                alert.setHeaderText("Operation could not be performed");
                                alert.showAndWait();
                            } catch (IOException e) {
                                LoggerCloud.LOGGER.warning(e.getLocalizedMessage());
                            }
                        }
                    }
                    break;
                case DELETE:
                    break;
                case SEARCH:
                    break;
            }
        }
    }
}

