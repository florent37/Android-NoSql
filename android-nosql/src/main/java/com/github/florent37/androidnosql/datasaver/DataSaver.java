package com.github.florent37.androidnosql.datasaver;

import java.util.Set;

/**
 * Created by florentchampigny on 24/05/2017.
 */

public interface DataSaver {
    void saveNodes(String completePath, Set<String> value);

    void saveValue(String completePath, Object value);

    Set<String> getNodes();

    Object getValue(String completePath);

    void remove(String startingPath);
}
