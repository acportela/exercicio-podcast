package br.ufpe.cin.if710.podcast.db;

/**
 * Created by acpr on 07/10/17.
 */

public enum DMLCommandsEnum {

    QUERYALL("QUERYALL"),
    INSERT("INSERT"),
    DELETE("DELETE"),
    INSERTBATCH("INSERTBATCH"),
    DELETEALL("DELETEALL");

    private final String description;

    DMLCommandsEnum(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

}
