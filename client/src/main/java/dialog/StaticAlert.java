package dialog;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.EnumOption;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Класс содержит набор алертов для различных операций со структурой каталогов
 *
 * @author Mishanin Aleksey
 * */
public class StaticAlert {

    private final static Logger logger = Logger.getLogger(StaticAlert.class);

    /**
     * Метод уведомляет о невоможности выполнения операции
     * */
    public static void showAlertFileExists(){
        Alert alert = new Alert(Alert.AlertType.WARNING, "File exists!", ButtonType.OK);
        alert.setHeaderText("Operation could not be performed");
        alert.showAndWait();
    }

    /**
     * Метод запрашивает имя для нового файла/каталога
     * */
    public static Optional<String> getNewName(EnumOption typeInputDialog){
        TextInputDialog textInputDialog = new TextInputDialog("");
        textInputDialog.setTitle(typeInputDialog.getValue());
        textInputDialog.setHeaderText("Help: the rules for writing a name are defined by the file system");
        textInputDialog.setContentText("Please, enter new name:");
        return textInputDialog.showAndWait();
    }

    /**
     * Метод запрашивает имя файла/каталога для поиска
     * */
    public static Optional<String> showSearchDialog(){
        TextInputDialog textInputDialog = new TextInputDialog("");
        textInputDialog.setTitle(EnumOption.SEARCH.getValue());
        textInputDialog.setHeaderText("Help: the rules for writing a name are defined by the file system");
        textInputDialog.setContentText("Please, enter the name of the file or directory you want to find: ");
        return textInputDialog.showAndWait();
    }

    public static String getStackTrace(Exception e){
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * Метод выводит стек ошибок
     * */
    public static void showAlertError(Exception e){

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(e.getMessage());

        VBox dialogPaneContent = new VBox();

        Label label = new Label("Stack Trace:");

        String stackTrace = getStackTrace(e);
        TextArea textArea = new TextArea();
        textArea.setText(stackTrace);

        dialogPaneContent.getChildren().addAll(label, textArea);
        alert.getDialogPane().setContent(dialogPaneContent);
        alert.showAndWait();
    }

    /**
     * Метод запрашивает подтверждение выполнения операции
     * */
    public static ButtonBar.ButtonData confirmOperation(){
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Будьте внимательны.", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Вы действительно хотите выполнить удаление?");
            alert.showAndWait();
            return alert.getResult().getButtonData();
        } catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * Метод выводит подсказку
     * */
    public static ButtonBar.ButtonData tipOperation(){
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Будьте внимательны.", ButtonType.OK);
            alert.setHeaderText("Для выполнения операции необходимо выбрать каталог");
            alert.showAndWait();
            return alert.getResult().getButtonData();
        } catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * Метод уведомляет об отказе сервера в выполнении операции
     * */
    public static void deniedOperation(){
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Внимание.", ButtonType.OK);
            alert.setHeaderText("Сервер отказал в выполнении операции");
            alert.showAndWait();
        } catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showReport(String report){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("Report");
        VBox dialogPaneContent = new VBox();
        TextArea textArea = new TextArea();
        textArea.setText(report);
        dialogPaneContent.getChildren().addAll(textArea);
        alert.getDialogPane().setContent(dialogPaneContent);
        alert.showAndWait();
    }
}
