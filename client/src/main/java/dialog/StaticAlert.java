package dialog;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import logger.LoggerCloud;
import model.EnumOption;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class StaticAlert {
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
        LoggerCloud.LOGGER.warning(e.getLocalizedMessage());
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
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Вы действительно хотите выполнить удаление?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            return alert.getResult().getButtonData();
        } catch (Exception e){
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
