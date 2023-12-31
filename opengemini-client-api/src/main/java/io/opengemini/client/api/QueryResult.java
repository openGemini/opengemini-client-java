package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class QueryResult {
    private List<SeriesResult> results;

    private String error;
}
