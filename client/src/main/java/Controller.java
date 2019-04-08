import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Controller {

    @FXML
    private TreeView<String> treeVievClient;

    public void initialize(){
        File[] roots = File.listRoots();
        treeVievClient.setRoot(new TreeItem<>("user"));
        for (int i = 0; i < roots.length; i++) {
            TreeItem<String> rootChild = new TreeItem<>(roots[i].toString());
            treeVievClient.getRoot().getChildren().addAll(rootChild);
            if(roots[i].list()==null) break;
            for (String str:
                    roots[i].list()) {
                TreeItem<String> child = new TreeItem<>(str);
                rootChild.getChildren().addAll(child);
                //System.out.println(roots[i] + str);
                try{
                    Files.walkFileTree(Paths.get(roots[i] + str),new MyFileVision(str));
                } catch (IOException e){

                }
            }
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
            System.out.println(path);
            return FileVisitResult.CONTINUE;
        }
    }
}

