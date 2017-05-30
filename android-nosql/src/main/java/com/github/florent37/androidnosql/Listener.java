package com.github.florent37.androidnosql;

/**
 * Created by florentchampigny on 29/05/2017.
 */

public interface Listener {
    void nodeChanged(String path, NoSql.Value value);
}
