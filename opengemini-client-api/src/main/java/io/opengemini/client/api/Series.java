package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class Series {
    private String name;

    private Map<String, String> tags;

    private List<String> columns;

    private List<List<Object>> values;
}
