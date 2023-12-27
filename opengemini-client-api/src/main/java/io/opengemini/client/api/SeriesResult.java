package io.opengemini.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonIgnoreProperties({"statement_id"})
public class SeriesResult {
    private List<Series> series;

    private String error;
}
