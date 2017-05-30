package com.github.florent37.androidnosql.datasaver;

import android.util.Log;

import java.util.Set;

/**
 * Created by florentchampigny on 24/05/2017.
 */

public class LogDataSaver implements DataSaver {

    private final String tag;

    public LogDataSaver(String tag) {
        this.tag = tag;
    }

    @Override
    public void saveNodes(String completePath, Set<String> value) {
        Log.d(tag, "nodes: " + completePath + " -> " + value.toString());
    }

    @Override
    public void saveValue(String completePath, Object value) {
        Log.d(tag, "values: " + completePath + " -> " + value.toString());
    }

    @Override
    public Set<String> getNodes() {
        return null;
    }

    @Override
    public Object getValue(String completePath) {
        return null;
    }

    @Override
    public void remove(String startingPath) {
        Log.d(tag, "remove starting with: " + startingPath);
    }
}
