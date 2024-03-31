package io.opengemini.client.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Query {
    /*
     * the query command
     */
    private String command;

    /*
     * the database name of the query command using
     */
    private String database;

    /*
     * the rp name of the query command using
     */
    private String retentionPolicy;

    public Query(String command){
        this.command = command;
    }
}
