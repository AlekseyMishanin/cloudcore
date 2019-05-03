import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.NettyNetwork;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("authorisationWindow.fxml")));
        primaryStage.setTitle("Cloud core authorization");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

//    public static void main(String[] args) throws InterruptedException {
//        NettyNetwork.getInstance().start();
//        Scanner sc = new Scanner(System.in);
//        while (sc.hasNext()){
//            if(sc.nextInt()==1){
//                NettyNetwork.getInstance().sendData("test", Paths.get("clientA/","Irozuku Sekai no Ashita kara - 01 [1080p].mkv"));
//            }
//            if(sc.nextInt()==2){
//                NettyNetwork.getInstance().loadData("test", Paths.get("clientA/", "Irozuku Sekai no Ashita kara - 01 [1080p].mkv"));
//            }
//        }
//    }
}
