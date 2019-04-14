package model;


/**
 * Класс перечислимого типа инкапсулирует пользователя облачного хранилища
 * Неленивый Singleton по мнению Joshua Bloch’а это лучший способ реализации шаблона
 * */
public enum  User {

    SETTING();

    private String name;                    //логин пользователя

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
