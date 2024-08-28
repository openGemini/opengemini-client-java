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

    /*
     * the precision of the time in query result
     */
    private Precision precision;

    public Query(String command) {
        this.command = command;
    }

    public Query(String command, String database, String retentionPolicy) {
        this.command = command;
        this.database = database;
        this.retentionPolicy = retentionPolicy;
    }
}
