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

/**
 * Класс инкапсулирует механизм отправки/скачивания файла клентом/сервером
 *
 * */
public class LoadFiles implements LoadAction {

    private static LoadFiles loadFiles = new LoadFiles();
    private static Path bufferForDownloadAndUpload = null;      //путь к файлу, предназанченному для скачивания
    private static OwnerOperation owner = OwnerOperation.NONE;  //владелец файла предназначенного для скачивания

    private LoadFiles(){}

    public static LoadFiles getInstance(){ return loadFiles;}

    @Override
    public void uploadFileClient(final Window ownerWindow){
        FileChooser fileChooser = new FileChooser();
        configuringFileChooser(fileChooser);
        //получаем ссылку на файл для отправки на сервер
        File file = fileChooser.showOpenDialog(ownerWindow);
        //если клиент выбрал файл для отправки
        if(file!=null) {
            //пишем в буфер путь к файлу
            bufferForDownloadAndUpload = file.toPath();
            //определяем владельца файла
            owner = OwnerOperation.CLIENT;
        } else {
            bufferForDownloadAndUpload = null;
            owner = OwnerOperation.NONE;
        }
    }

    @Override
    public void downloadFileClient(final Window ownerWindow) {
        //если владельцм файла для загрузки является не сервер, прекращаем работу
        if (owner != OwnerOperation.SERVER) return;
        //выбираем каталог для загрузки
        DirectoryChooser directoryChooser = new DirectoryChooser();
        configuringDirectoryChooser(directoryChooser);
        File directory = directoryChooser.showDialog(ownerWindow);
        if (directory!=null){
            //загружаем файл в каталог
            NettyNetwork.getInstance().loadData(bufferForDownloadAndUpload, directory.getAbsolutePath());
            //очищаем поля класса
            owner = OwnerOperation.NONE;
            bufferForDownloadAndUpload = null;
        }
    }

    @Override
    public void uploadFileServer(TreeView<ExtFile> tree) {
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            //строим по элементам дерева путь к файлу предназначенному для скачивания с сервера
            String path = TreeViewUtility.getPathFromTreeItem(selectedItem);
            //пишем в буфер путь к файлу
            bufferForDownloadAndUpload = Paths.get(path);
            //определяем владельца файла
            owner = OwnerOperation.SERVER;
        }
    }

    @Override
    public void downloadFileServer(TreeView<ExtFile> tree) throws InterruptedException {
        //если владельцм файла для загрузки является не клиент, прекращаем работу
        if (owner != OwnerOperation.CLIENT) return;
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            String path = TreeViewUtility.getPathFromTreeItem(selectedItem);
            //отправляем на сервер файл
            NettyNetwork.getInstance().sendData(path, bufferForDownloadAndUpload);
            //очищаем поля класса
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
