package dialog;

import javafx.scene.AccessibleRole;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.EnumOption;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        textInputDialog.getEditor().getStyleClass().add("password-field");
        textInputDialog.getEditor().setAccessibleRole(AccessibleRole.PASSWORD_FIELD);
        return textInputDialog.showAndWait();
    }

    /**
     * Метод запрашивает пароль для установления повторного соединения с сервером
     * */
    public static Optional<String> getPassword() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PasswordInputDialog textInputDialog = new PasswordInputDialog();
        textInputDialog.setTitle(EnumOption.PASSWORD.getValue());
        textInputDialog.setHeaderText("");
        textInputDialog.setContentText("Please, enter your password:");
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
        Alert alert = createInfoAllert("Вы действительно хотите выполнить удаление?");
        return alert != null ? alert.getResult().getButtonData() : null;
    }

    /**
     * Метод выводит подсказку
     * */
    public static ButtonBar.ButtonData tipOperation(){
        Alert alert = createInfoAllert("Для выполнения операции необходимо выбрать каталог");
        return alert != null ? alert.getResult().getButtonData() : null;
    }

    /**
     * Метод уведомляет об отказе сервера в выполнении операции
     * */
    public static void deniedOperation(){
        createInfoAllert("Сервер отказал в выполнении операции");
    }

    /**
     * Метод уведомляет об ошибке при выполнении операции с файлом
     * */
    public static void fileError(){
        createInfoAllert("При выполнении операции с файлом произошла ошибка");
    }

    /**
     * Метод уведомляет об ошибке произошедшей в сети
     * */
    public static void networkError(){
        createInfoAllert("В сети произошла ошибка. Соединение было разорвано.");
    }

    /**
     * Метод уведомляет о том что соединение успешно установлено
     * */
    public static void connectionIsCreate(){
        createInfoAllert("Соединение с сервером успешно установлено.");
    }

    /**
     * Метод уведомляет о том что соединение уже существует
     * */
    public static void connectionIsGood(){
        createInfoAllert("Соединение с сервером ранее было установлено.");
    }

    private static Alert createInfoAllert(String message){
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Внимание.", ButtonType.OK);
            alert.setHeaderText(message);
            alert.showAndWait();
            return alert;
        } catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
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
