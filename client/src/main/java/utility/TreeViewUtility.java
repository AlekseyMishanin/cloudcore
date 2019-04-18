package utility;

import dialog.StaticAlert;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import lombok.NonNull;
import model.EnumOption;
import model.ExtFile;
import utility.file.FileVisionDelete;
import utility.file.FileVisionSearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class TreeViewUtility {


    private static FileVisionDelete fileVision = new FileVisionDelete();
    private static FileVisionSearch fileSearch = new FileVisionSearch();
    private static Path bufferForMove = null;
    private static Path bufferForCopyAndCut = null;
    private static TreeItem<ExtFile> itemForCut;
    private static EnumOption option = null;

    public static <T> String getPathFromTreeItem(@NonNull TreeItem<T> item){
        if(item.getParent() == null) return "";
        return  (getPathFromTreeItem(item.getParent()).equals("") ? "" : getPathFromTreeItem(item.getParent()) + File.separator) + item.getValue().toString();
    }

    public static void  renameFileAndTreeItem(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            Path source = Paths.get(TreeViewUtility.getPathFromTreeItem(selectedItem));                                //получаем полный путь к выбранному элементу
            Optional<String> res = StaticAlert.getNewName(EnumOption.RENAME);
            if(res.isPresent())
            {
                String newName = res.get();
                try {
                    Files.move(source, source.resolveSibling(newName));                           //переименовываем элемент
                    TreeItem<ExtFile> objP = selectedItem.getParent();                                       //определяем родителя выбранного элемента
                    ObservableList<TreeItem<ExtFile>> listChild = selectedItem.getChildren();                //определяем детей выбранного элемента

                    Platform.runLater(() ->{
                        objP.getChildren().remove(selectedItem);                                             //удаляем выбранный элемент
                        TreeItem<ExtFile> var = new TreeItem<>(new ExtFile(newName));     //создаем элемент с новым именем
                        var.getChildren().setAll(listChild);                                            //принимаем всех детей
                        objP.getChildren().add(var);                                                    //привязываем родителя
                    });
                } catch (FileAlreadyExistsException e){
                    StaticAlert.showAlertFileExists();
                } catch (IOException e) {
                    StaticAlert.showAlertError(e);
                }
            }
        }
    }

    public static void copyFileAndTreeItem(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            bufferForCopyAndCut =  Paths.get(TreeViewUtility.getPathFromTreeItem(selectedItem));                                //получаем полный путь к выбранному элементу
            option = EnumOption.COPY;
        }
    }

    public static void cutFileAndTreeItem(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            bufferForCopyAndCut =  Paths.get(TreeViewUtility.getPathFromTreeItem(selectedItem));                                //получаем полный путь к выбранному элементу
            itemForCut = selectedItem;
            option = EnumOption.CUT;
        }
    }

    public static void moveFileAndTreeItem(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            bufferForMove =  Paths.get(TreeViewUtility.getPathFromTreeItem(selectedItem));                                //получаем полный путь к выбранному элементу
        }
    }

    //нужно потестить
    public static void pasteFileAndTreeItem(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(option == EnumOption.COPY && bufferForCopyAndCut !=null && selectedItem != null){
            TreeItem<ExtFile> parentForNewItemTree = getParentCatalog(selectedItem);
            if(parentForNewItemTree == null) return;
            try {
                Files.copy(bufferForCopyAndCut,Paths.get(getPathFromTreeItem(selectedItem)));
                Platform.runLater(() -> {
                    TreeItem<ExtFile> newTreeItem = new TreeItem<>(new ExtFile(bufferForCopyAndCut.getFileName().toString()));     //создаем элемент с новым именем
                    parentForNewItemTree.getChildren().add(newTreeItem);
                    tree.getSelectionModel().select(newTreeItem);
                });
            } catch (IOException e) {
                StaticAlert.showAlertError(e);
            } finally {
                bufferForCopyAndCut = null;
                option = null;
            }
        } else if(option == EnumOption.CUT && bufferForCopyAndCut !=null && selectedItem != null){
            TreeItem<ExtFile> parentForNewItemTree = getParentCatalog(selectedItem);
            if(parentForNewItemTree == null) return;
            try {
                Files.copy(bufferForCopyAndCut,Paths.get(getPathFromTreeItem(selectedItem)));
                Files.walkFileTree(bufferForCopyAndCut, fileVision);
                Platform.runLater(() -> {
                    TreeItem<ExtFile> newTreeItem = new TreeItem<>(new ExtFile(bufferForCopyAndCut.getFileName().toString()));     //создаем элемент с новым именем
                    parentForNewItemTree.getChildren().add(newTreeItem);
                    tree.getSelectionModel().select(newTreeItem);
                });
                itemForCut.getParent().getChildren().remove(itemForCut); //удаляем из дерева вырезанный элемент
                itemForCut = null;
            } catch (IOException e) {
                StaticAlert.showAlertError(e);
            } finally {
                bufferForCopyAndCut = null;
                option = null;
                fileVision.clear();
            }
        }
    }

    public static void createNewFileAndTreeItem(@NonNull TreeView<ExtFile> tree){
        createNewObjectAndTreeItem(tree,EnumOption.CREATEFILE);
    }

    public static void createNewCatalogAndTreeItem(@NonNull TreeView<ExtFile> tree){
        createNewObjectAndTreeItem(tree,EnumOption.CREATECATALOG);
    }

    private static void createNewObjectAndTreeItem(@NonNull TreeView<ExtFile> tree, EnumOption option){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            TreeItem<ExtFile> parentForNewItemTree = getParentCatalog(selectedItem);
            if(parentForNewItemTree == null) return;
            Optional<String> res = StaticAlert.getNewName(EnumOption.CREATEFILE);
            if(res.isPresent())
            {
                String newNameFile = res.get();
                String catalog = TreeViewUtility.getPathFromTreeItem(parentForNewItemTree);
                try {
                    if(option.equals(EnumOption.CREATEFILE))Files.createFile(Paths.get(catalog, newNameFile));
                    if(option.equals(EnumOption.CREATECATALOG))Files.createDirectory(Paths.get(catalog, newNameFile));
                    Platform.runLater(() -> {
                        TreeItem<ExtFile> newTreeItem = new TreeItem<>(new ExtFile(newNameFile));     //создаем элемент с новым именем
                        parentForNewItemTree.getChildren().add(newTreeItem);
                        tree.getSelectionModel().select(newTreeItem);
                    });
                } catch (FileAlreadyExistsException e){
                    StaticAlert.showAlertFileExists();
                } catch (IOException e) {
                    StaticAlert.showAlertError(e);
                }
            }
        }
    }

    //протестировать
    public static void searchObjectAndTreeItem(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        String strPath = getPathFromTreeItem(selectedItem);
        if(selectedItem != null && Files.isDirectory(Paths.get(strPath))) {
            Optional<String> res = StaticAlert.showSearchDialog();
            if (res.isPresent()) {
                String nameSearchObject = res.get();
                fileSearch.setNameSearchObject(nameSearchObject);
                try {
                    Files.walkFileTree(Paths.get(strPath), fileSearch);
                } catch (IOException e) {
                    StaticAlert.showAlertError(e);
                } finally {
                    StaticAlert.showReport(fileSearch.getReport().toString());
                    fileSearch.clear();
                }
            }
        }
    }

    public static void deleteObjectAndTreeItem(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){


            if(StaticAlert.confirmOperation()== ButtonBar.ButtonData.NO) return;
            Path source = Paths.get(getPathFromTreeItem(selectedItem));
            try {
                Files.walkFileTree(source, fileVision);
            } catch (IOException e) {
                StaticAlert.showAlertError(e);
            } finally {
                StaticAlert.showReport(fileVision.getReport().toString());
                fileVision.clear();
            }
        }
    }

    private static TreeItem<ExtFile> getParentCatalog(@NonNull TreeItem<ExtFile> selectedItem){
        if(Files.isDirectory(Paths.get(getPathFromTreeItem(selectedItem)))){
            return selectedItem;
        } else {
            return selectedItem.getParent();
        }
    }

//    private static void deleteTreeItem(@NonNull Path path, @NonNull TreeView<ExtFile> tree){
//        ObservableList<TreeItem<ExtFile>> list = null;tree.
//        for (TreeItem<ExtFile> item:
//                tree.getRoot().getChildren()) {
//            if(item.getValue().getName().equals(path.getRoot())) {
//                list = item.getChildren();
//                break;
//            }
//        }
//        for (int i = 0; i < path.getNameCount() && list != null; i++) {
//            if(i == path.getNameCount() - 1){
//                for (TreeItem<ExtFile> item:
//                        list) {
//                    if(item.getValue().getName().equals(path.getName(i).toString())) {
//                        list.remove(item);
//                        return;
//                    }
//                }
//            } else{
//                ObservableList<TreeItem<ExtFile>> temp = null;
//                for (TreeItem<ExtFile> item:
//                        list) {
//                    if(item.getValue().getName().equals(path.getName(i).toString())) {
//                        temp = item.getChildren();
//                        break;
//                    }
//                }
//                list = temp;
//            }
//        }
//    }
}
