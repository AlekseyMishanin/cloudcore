package model;

public enum EnumOption{
    RENAME("New name"),
    CREATEFILE("Create new file"),
    CREATECATALOG("Create new catalog"),
    SEARCH("Search file or catalog"),
    PASSWORD("Enter password"),
    COPY(""),
    CUT("");

    private String value;

    EnumOption(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}