package io.opengemini.client.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
@AllArgsConstructor
public class Endpoint {
    private String url;

    private AtomicBoolean isDown;
}
