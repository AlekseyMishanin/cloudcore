package dialog;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.EnumOption;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class StaticAlert {

    private final static Logger logger = Logger.getLogger(StaticAlert.class);

    public static void showAlertFileExists(){
        Alert alert = new Alert(Alert.AlertType.WARNING, "File exists!", ButtonType.OK);
        alert.setHeaderText("Operation could not be performed");
        alert.showAndWait();
    }

    public static Optional<String> getNewName(EnumOption typeInputDialog){
        TextInputDialog textInputDialog = new TextInputDialog("");
        textInputDialog.setTitle(typeInputDialog.getValue());
        textInputDialog.setHeaderText("Help: the rules for writing a name are defined by the file system");
        textInputDialog.setContentText("Please, enter new name:");
        return textInputDialog.showAndWait();
    }

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
