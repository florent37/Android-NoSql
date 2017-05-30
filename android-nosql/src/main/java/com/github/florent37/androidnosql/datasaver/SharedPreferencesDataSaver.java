package com.github.florent37.androidnosql.datasaver;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by florentchampigny on 24/05/2017.
 */

public class SharedPreferencesDataSaver implements DataSaver {

    public static final String NODES = "%nodes%";
    public static final String NODES_SHARED_PREFS = "%nodes%";

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesDataSaver(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public SharedPreferencesDataSaver(Context context) {
        this(context.getSharedPreferences(NODES_SHARED_PREFS, Context.MODE_PRIVATE));
    }

    @Override
    public void saveNodes(String completePath, Set<String> values) {
        final Set<String> nodes = new HashSet<>();
        nodes.add(completePath);
        nodes.addAll(getNodes());
        for (String value : values) {
            nodes.add(completePath + value);
        }

        sharedPreferences.edit()
                .putStringSet(NODES, nodes)
                .apply();
    }

    @Override
    public void saveValue(String completePath, Object value) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        if(value instanceof Integer) {
            editor.putInt(completePath, (Integer) value);
        } else if(value instanceof Float) {
            editor.putFloat(completePath, (Float) value);
        } else if(value instanceof Boolean) {
            editor.putBoolean(completePath, (Boolean) value);
        } else if(value instanceof Long) {
            editor.putLong(completePath, (Long) value);
        } else {
            editor.putString(completePath, String.valueOf(value));
        }

        editor.apply();
    }

    @Override
    public Set<String> getNodes(){
        return sharedPreferences.getStringSet(NODES, new HashSet<String>());
    }

    @Override
    public Object getValue(String completePath){
        return sharedPreferences.getAll().get(completePath);
    }

    @Override
    public void remove(String startingPath) {
        final Set<String> nodes = getNodes();
        final Set<String> nodesToKeep = new HashSet<>();
        //update values
        for (String node : nodes) {
            if(node.startsWith(startingPath)) {
                sharedPreferences.edit().remove(node).apply();
            } else {
                nodesToKeep.add(node);
            }
        }
        sharedPreferences.edit()
                .putStringSet(NODES, nodesToKeep)
                .apply();
    }
}
