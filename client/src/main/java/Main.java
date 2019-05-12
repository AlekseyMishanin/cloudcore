import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.NettyNetwork;
import utility.ListController;

import java.util.Objects;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("authorisationWindow.fxml")));
        primaryStage.setTitle("Authorization");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    @Override
    public void stop(){

        ListController.getInstance().setOperatingPanelController(null);
        ListController.getInstance().setAuthorisationController(null);
        NettyNetwork.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
