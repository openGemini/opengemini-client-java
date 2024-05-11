package io.opengemini.client.okhttp.msgpack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Janle
 * @date 2024/5/10 18:19
 */
public class QueryResultModelPath {
    private List<String> names = new ArrayList<>();
    private List<Object> objects = new ArrayList<>();
    private int lastIndex = -1;

    public void add(final String name, final Object object) {
        names.add(name);
        objects.add(object);
        lastIndex++;
    }

    public <T> T getLastObject() {
        return (T) objects.get(lastIndex);
    }

    public void removeLast() {
        names.remove(lastIndex);
        objects.remove(lastIndex);
        lastIndex--;
    }

    public boolean compareEndingPath(final String... names) {
        int diff = (lastIndex + 1) - names.length;
        if (diff < 0) {
            return false;
        }
        for (int i = 0; i < names.length; i++) {
            if (!names[i].equals(this.names.get(i + diff))) {
                return false;
            }
        }

        return true;
    }
}
