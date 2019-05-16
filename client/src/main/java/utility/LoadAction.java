package utility;

import javafx.scene.control.TreeView;
import javafx.stage.Window;
import model.ExtFile;

/**
 * Интерфейс описывает основные операции с файлами между клиентом и сервером
 * */
public interface LoadAction {

    void uploadFileClient(final Window ownerWindow);                                //клиент отправляет файл
    void downloadFileClient(final Window ownerWindow);                              //клиент скачивает файл
    void uploadFileServer(TreeView<ExtFile> tree);                                  //сервер отправляет файл
    void downloadFileServer(TreeView<ExtFile> tree) throws InterruptedException;    //сервер скачивает файл
}
