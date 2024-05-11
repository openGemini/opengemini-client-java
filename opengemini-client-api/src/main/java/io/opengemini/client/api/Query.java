package io.opengemini.client.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
    };


    public Query(String command, String database) {
        this.command = command;
        this.database = database;
        this.retentionPolicy = "autogen";
    }

    public String getCommandWithUrlEncoded() {
        return encode(command);
    }

    public static String encode(final String command) {
        try {
            return URLEncoder.encode(command, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Every JRE must support UTF-8", e);
        }
    }


    @SuppressWarnings("checkstyle:avoidinlineconditionals")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result
                + ((database == null) ? 0 : database.hashCode());
        return result;
    }


    @SuppressWarnings("checkstyle:needbraces")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Query other = (Query) obj;
        if (command == null) {
            if (other.command != null)
                return false;
        } else if (!command.equals(other.command))
            return false;
        if (database == null) {
            if (other.database != null)
                return false;
        } else if (!database.equals(other.database))
            return false;
        return true;
    }
}
