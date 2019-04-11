import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Controller {

    @FXML
    private TreeView<extFile> treeVievClient;

    public void initialize(){
        File[] roots = File.listRoots();
        treeVievClient.setRoot(new TreeItem<>(new extFile("user")));
        for (int i = 0; i < roots.length; i++) {
            TreeItem<extFile> rootChild = new TreeItem<>(new extFile(roots[i].getPath()));
            treeVievClient.getRoot().getChildren().addAll(rootChild);
            if(roots[i].list()==null) break;
            /*for (String str:
                    roots[i].list()) {
                if(!str.equals("boot")) continue;
                createItemForTree(rootChild, new File(roots[i] + str));
                TreeItem<String> child = new TreeItem<>(str);
                rootChild.getChildren().addAll(child);
                //System.out.println(roots[i] + str);
                try{
                    Files.walkFileTree(Paths.get(roots[i] + str),new MyFileVision(str));
                } catch (IOException e){

                }
            }*/
        }
        treeVievClient.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.getValue().isDirectory()) return;
            ObservableList<TreeItem<extFile>> list = FXCollections.observableArrayList();
            for (String s : newValue.getValue().list()) {
                list.add(new TreeItem<>(new extFile(newValue.getValue(), s)));
            }
            newValue.getChildren().setAll(list);
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

