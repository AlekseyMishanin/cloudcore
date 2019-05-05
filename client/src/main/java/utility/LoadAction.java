package utility;

import javafx.scene.control.TreeView;
import javafx.stage.Window;
import model.ExtFile;

public interface LoadAction {

    void uploadFileClient(final Window ownerWindow);
    void downloadFileClient(final Window ownerWindow);
    void uploadFileServer(TreeView<ExtFile> tree);
    void downloadFileServer(TreeView<ExtFile> tree) throws InterruptedException;
}
