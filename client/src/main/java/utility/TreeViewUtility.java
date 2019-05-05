package utility;

import dialog.StaticAlert;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import lombok.NonNull;
import model.EnumOption;
import model.ExtFile;
import net.NettyNetwork;
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
    private static Path bufferForCopyAndCut = null;
    private static Path bufferForCopyAndCutCloud = null;
    private static TreeItem<ExtFile> itemForCut;
    private static EnumOption option = null;
    private static EnumOption optionCloud = null;

    public static <T> String getPathFromTreeItem(@NonNull TreeItem<T> item){
        if(item == null || item.getParent() == null) return "";
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

    private static void copyDirOrFile(final Path src, final Path dst) throws IOException {
        final File srcFile = src.toFile();
        final File dstFile = dst.toFile();
        if (srcFile.exists() && srcFile.isDirectory() && !dstFile.exists()) {
            dstFile.mkdir();
            File nextSrcFile;
            String nextSrcFilename, nextDstFilename;
            for (String filename : srcFile.list()) {
                nextSrcFilename = srcFile.getAbsolutePath() + File.separator + filename;
                nextDstFilename = dstFile.getAbsolutePath() + File.separator + filename;
                nextSrcFile = new File(nextSrcFilename);
                if (nextSrcFile.isDirectory()) {
                    copyDirOrFile(Paths.get(nextSrcFilename), Paths.get(nextDstFilename));
                } else {
                    Files.copy(Paths.get(nextSrcFilename),Paths.get(nextDstFilename));
                }
            }
        } else if (srcFile.exists() && !srcFile.isDirectory() && !dstFile.exists()) {
            Files.copy(src,dst);
        }
    }
    //нужно потестить
    public static void pasteFileAndTreeItem(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(option == EnumOption.COPY && bufferForCopyAndCut !=null && selectedItem != null){
            TreeItem<ExtFile> parentForNewItemTree = getParentCatalog(selectedItem);
            if(parentForNewItemTree == null) return;
            try {
                Path copyToPath = Files.isDirectory(Paths.get(getPathFromTreeItem(selectedItem))) ? Paths.get(getPathFromTreeItem(selectedItem)) : Paths.get(getPathFromTreeItem(selectedItem)).getParent();
                copyDirOrFile(bufferForCopyAndCut,copyToPath.resolve(bufferForCopyAndCut.getFileName()));
                String temp = bufferForCopyAndCut.getFileName().toString();
                Platform.runLater(() -> {
                    TreeItem<ExtFile> newTreeItem = new TreeItem<>(new ExtFile(temp));     //создаем элемент с новым именем
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
                Path copyToPath = Files.isDirectory(Paths.get(getPathFromTreeItem(selectedItem))) ? Paths.get(getPathFromTreeItem(selectedItem)) : Paths.get(getPathFromTreeItem(selectedItem)).getParent();
                copyDirOrFile(bufferForCopyAndCut,copyToPath.resolve(bufferForCopyAndCut.getFileName()));
                String temp = bufferForCopyAndCut.getFileName().toString();
                Files.walkFileTree(bufferForCopyAndCut, fileVision);
                Platform.runLater(() -> {
                    TreeItem<ExtFile> newTreeItem = new TreeItem<>(new ExtFile(temp));     //создаем элемент с новым именем
                    parentForNewItemTree.getChildren().add(newTreeItem);
                    tree.getSelectionModel().select(newTreeItem);
                    if(itemForCut!=null)
                    {
                        itemForCut.getParent().getChildren().remove(itemForCut); //удаляем из дерева вырезанный элемент
                        itemForCut = null;
                    }
                });
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
            if(selectedItem.getParent() == null) return;
            TreeItem<ExtFile> parentForNewItemTree = getParentCatalog(selectedItem);
            if(parentForNewItemTree == null) return;
            Optional<String> res = StaticAlert.getNewName(option);
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
                Platform.runLater(() -> {
                    selectedItem.getParent().getChildren().remove(selectedItem); //удаляем из дерева вырезанный элемент
                });
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

    public static String createNewCatalogInCloud(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            Optional<String> res = StaticAlert.getNewName(EnumOption.CREATECATALOG);
            if(res.isPresent())
            {
                String newCatalog = res.get();
                String parentCatalog = TreeViewUtility.getPathFromTreeItem(selectedItem);
                return parentCatalog.isEmpty() ? newCatalog : parentCatalog + File.separator + newCatalog;
            }
        }
        return null;
    }

    public static void updateStructureTreeViewCloud(@NonNull TreeView<ExtFile> tree, String newStructure){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                TreeItem<ExtFile> root = tree.getRoot();
                if(newStructure!=null){
                    root.getChildren().removeAll(root.getChildren());
                    String[] pathArr = newStructure.split("\n");

                    for (int i = 0; i < pathArr.length; i++) {

                        String[] elementArr = pathArr[i].split("/");
                        TreeItem<ExtFile> previousNode = root;
                        TreeItem<ExtFile> currentNode = null;
                        for (int j = 0; j < elementArr.length; j++) {
                            currentNode = findItem(previousNode, elementArr[j]);
                            if(currentNode != null) {previousNode = currentNode;}
                            else {
                                currentNode = new TreeItem<>(new ExtFile(elementArr[j]));
                                previousNode.getChildren().add(currentNode);
                                previousNode = currentNode;
                            }
                        }
                    }
                } else {
                    //если у дерева есть корень и потомки
                    if(root != null || !root.getChildren().isEmpty()){
                        //удаляем потомков
                        root.getChildren().removeAll(root.getChildren());
                    }
                }
            }
        });
    }

    private static TreeItem<ExtFile> findItem(TreeItem<ExtFile> parent, String value){
        if(parent.getChildren().isEmpty()) return null;
        for (TreeItem<ExtFile> item:
             parent.getChildren()) {
            if (item.getValue().getName().equals(value)) return item;
        }
        return null;
    }

    public static String deleteDirectoryInCloud(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            if(StaticAlert.confirmOperation()== ButtonBar.ButtonData.NO) return null;
            return getPathFromTreeItem(selectedItem);
        }
        return null;
    }

    public static void copyFileCloud(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            bufferForCopyAndCutCloud =  Paths.get(TreeViewUtility.getPathFromTreeItem(selectedItem));                                //получаем полный путь к выбранному элементу
            optionCloud = EnumOption.COPY;
        }
    }

    public static void cutFileCloud(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            bufferForCopyAndCutCloud =  Paths.get(TreeViewUtility.getPathFromTreeItem(selectedItem));                                //получаем полный путь к выбранному элементу
            optionCloud = EnumOption.CUT;
        }
    }
    public static void pasteFileCloud(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            String newCatalog =  TreeViewUtility.getPathFromTreeItem(selectedItem);                                //получаем полный путь к выбранному элементу
            if(optionCloud == EnumOption.COPY){
                NettyNetwork.getInstance().requestCopyCatalog(bufferForCopyAndCutCloud,newCatalog);
            }
            if(optionCloud == EnumOption.CUT){
                NettyNetwork.getInstance().requestCutCatalog(bufferForCopyAndCutCloud,newCatalog);
            }
            optionCloud = null;
        }
    }

    public static void renameFileCloud(@NonNull TreeView<ExtFile> tree){
        TreeItem<ExtFile> selectedItem = tree.getSelectionModel().getSelectedItem();           //ссылка на выбранный элемент дерева
        if(selectedItem != null){
            Optional<String> res = StaticAlert.getNewName(EnumOption.RENAME);
            if(res.isPresent()) {
                String newName = res.get();
                String oldName =  TreeViewUtility.getPathFromTreeItem(selectedItem);
                NettyNetwork.getInstance().requestRenameCatalog(Paths.get(oldName),newName);
            }
        }
    }
}
