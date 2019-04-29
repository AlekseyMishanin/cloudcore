package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.User;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Класс контроллера для обработки событий окна авторизации.
 * */
public class AuthorisationController {

    @FXML private StackPane rootNode;
    @FXML private TextField login;
    @FXML private PasswordField password;
    @FXML private Label msg;
    final private Timer timer = new Timer(true);              //таймер для запуска заданий
    private String name;                                               //имя пользователя
    private String pswd;                                               //пароль пользователя

    /**
     * Набор констант перечислимого типа инкапсулирующих результат ввода пары логин/пароль
     * */
    private enum Status {
        EMPTYLOGIN,         //пустой логин
        EMPTYPASSWORD,      //пустой пароль
        EMPTYBOTH,          //пустой логин и пароль
        BADLOGINORPASSWORD, //неправильный логин или пароль
        GOOD                //правильный логин и пароль
    }

    public void initialize(){
    }
    /**
     * Метод обработки события нажатия кнопки. При нажатии кнопки считывается текс из textfield с логином и паролем.
     * Далее вызывается метод для обработки результата ввода логина и пароля.
     * @param actionEvent - объект события
     * */
    public void btnClick(ActionEvent actionEvent) {
        name = login.getText();
        pswd = password.getText();
        if(name.isEmpty() && pswd.isEmpty()) {
            statusProcessing(Status.EMPTYBOTH);
        }
        else if(name.isEmpty()) {
            statusProcessing(Status.EMPTYLOGIN);
        }
        else if(pswd.isEmpty()) {
            statusProcessing(Status.EMPTYPASSWORD);
        }
        else if(name.equals("test") && pswd.equals("test")){
            statusProcessing(Status.GOOD);
        } else {
            statusProcessing(Status.BADLOGINORPASSWORD);
        }
    }

    /**
     * Метод обработки результат ввода логина и пароля. Если введен корректный логин и пароль, то вызявается метод
     * @createNewScene() для создания новой сцены. В иных ситуациях выводится сообщение и предлагается ввести логин и
     * пароль повторно
     * @param st - объект перечислимого типа описывающий результат ввода логина и пароля
     * */
    private void statusProcessing(Status st){
        switch (st){
            case EMPTYLOGIN:
                msg.setText("Login is empty...");
                break;
            case EMPTYPASSWORD:
                msg.setText("Password is empty...");
                break;
            case EMPTYBOTH:
                msg.setText("Login and password is empty...");
                break;
            case BADLOGINORPASSWORD:
                msg.setText("Wrong login or password...");
                break;
            case GOOD:
                createNewScene();
                break;
            default:
                msg.setText("Unknown situation");
        }
        clear();
    }

    /**
     * Служебный метод для очистки текстового поля с логином и паролем. Метод также запускает задание (объект
     * класса ClearLabelTask) в рамках которого в объекте msg класса Label меняется текст сообщения.
     * */
    private void clear(){
        login.clear();
        password.clear();
        timer.schedule(new ClearLabelTask(), 2000);
    }

    /**
     * Служебный метод, который создает новую сцену и присваивает ее текущему объекту типа Stage
     * */
    private void createNewScene(){
        Parent root = null;
        try {
            User.SETTING.setName(name);                 //присваиваем логин пользователя объекту перечислимого типа
            root = FXMLLoader.load(getClass().getResource("/operatingPanel.fxml"));
            Scene scene = new Scene(root, rootNode.getWidth(), rootNode.getHeight());
            ((Stage)rootNode.getScene().getWindow()).setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Класс инкапсулирует задание которое запускается объектом типа Timer
     * */
    class ClearLabelTask extends TimerTask{
        @Override
        public void run() {
            Platform.runLater(()->msg.setText("Please, try again..."));
        }
    }
}
