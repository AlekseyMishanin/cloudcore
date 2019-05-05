package utility;

import dialog.StaticAlert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import model.ExtFile;
import model.OwnerOperation;
import net.NettyNetwork;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoadFiles implements LoadAction {

    private static LoadFiles loadFiles = new LoadFiles();
    private static Path bufferForDownloadAndUpload = null;
    private static OwnerOperation owner = OwnerOperation.NONE;

    private LoadFiles(){}

    public static LoadFiles getInstance(){ return loadFiles;}

    @Override
    public void uploadFileClient(final Window ownerWindow){
        FileChooser fileChooser = new FileChooser();
        configuringFileChooser(fileChooser);
        File file = fileChooser.showOpenDialog(ownerWindow);
        if(file!=null) {
            bufferForDownloadAndUpload = file.toPath();
            owner = OwnerOperation.CLIENT;
        } else {
            bufferForDownloadAndUpload = null;
            owner = OwnerOperation.NONE;
        }
    }

    @Override
    public void downloadFileClient(final Window ownerWindow) {
        if (owner != OwnerOperation.SERVER) return;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        configuringDirectoryChooser(directoryChooser);
        File directory = directoryChooser.showDialog(ownerWindow);
        if (directory!=null){

            NettyNetwork.getInstance().loadData(bufferForDownloadAndUpload, directory.getAbsolutePath());

            owner = OwnerOperation.NONE;
            bufferForDownloadAndUpload = null;
        }
    }

    @Override
    public void uploadFileServer(TreeView<ExtFile> tree) {
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            String path = TreeViewUtility.getPathFromTreeItem(selectedItem);
            bufferForDownloadAndUpload = Paths.get(path);
            owner = OwnerOperation.SERVER;
        }
    }

    @Override
    public void downloadFileServer(TreeView<ExtFile> tree) throws InterruptedException {

        if (owner != OwnerOperation.CLIENT) return;
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            String path = TreeViewUtility.getPathFromTreeItem(selectedItem);
            NettyNetwork.getInstance().sendData(path, bufferForDownloadAndUpload);
            owner = OwnerOperation.NONE;
            bufferForDownloadAndUpload = null;
        } else {
            StaticAlert.tipOperation();
        }
    }

    private void configuringDirectoryChooser(DirectoryChooser directoryChooser) {

        directoryChooser.setTitle("Select some Directories");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    private void configuringFileChooser(FileChooser fileChooser) {

        fileChooser.setTitle("Select some Files");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }
}
