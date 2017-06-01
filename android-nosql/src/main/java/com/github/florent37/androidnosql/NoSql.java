package com.github.florent37.androidnosql;

import android.util.Pair;

import com.github.florent37.androidnosql.datasaver.DataSaver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NoSql {

    public static final String PATH_SEPARATOR = "/";
    private static NoSql INSTANCE;
    public boolean autoSave = true;
    private Map<String, Reference<Listener>> listeners = new HashMap<>();
    private Node root;

    private NoSql() {
        root = new Node("");
    }

    public static NoSql getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoSql();
        }
        return INSTANCE;
    }

    private Node getNodeOrCreate(String completeFieldPath) {
        final Pair<Node, String> nodeDesc = getNode(completeFieldPath);
        return getOrCreate(nodeDesc.first, nodeDesc.second);
    }

    public NoSql remove(String path) {
        if (path.equals(PATH_SEPARATOR)) {
            root.removeAll();
        } else {
            final Pair<Node, String> nodeDesc = getNode(path);
            nodeDesc.first.remove(nodeDesc.second);
        }
        if (autoSave) {
            for (DataSaver dataSaver : AndroidNoSql.getDataSaver()) {
                dataSaver.remove(path);
            }
        }
        return this;
    }

    private Node getOrCreate(Node node, String name) {
        if (node.has(name)) {
            if (node.get(name) instanceof Node) {
                return node.child(name);
            } else {
                return null;
            }
        } else {
            final Node node1 = new Node(node.path + PATH_SEPARATOR + name);
            node.put(name, node1);
            return node1;
        }
    }

    private NodeDescritption split(String uri) {
        uri = uri.replace("//", "/");
        final String[] strings = uri.split("/");
        final List<String> path = new ArrayList<>();
        for (String string : strings) {
            if (string.length() > 0) {
                path.add(string);
            }
        }

        final int indexLast = path.size() - 1;
        final String name = path.get(indexLast);
        path.remove(indexLast);

        return new NodeDescritption(path, name);
    }

    public Value get(String uri) {
        final Pair<Node, String> node_and_name = getNode(uri);
        final Node node = node_and_name.first;
        final String name = node_and_name.second;
        return node.value(name);
    }

    public Pair<Node, String> getNode(String uri) {
        Node node = root;

        final NodeDescritption nodeDescritption = split(uri);
        for (String nodeName : nodeDescritption.path) {
            node = getOrCreate(node, nodeName);
        }

        return Pair.create(node, nodeDescritption.name);
    }

    public void load() {
        final Collection<DataSaver> dataSavers = AndroidNoSql.getDataSaver();
        if (!dataSavers.isEmpty()) {
            for (DataSaver dataSaver : dataSavers) {
                final Set<String> nodes = dataSaver.getNodes();
                if (nodes != null) {
                    for (String node : nodes) {
                        final Object value = dataSaver.getValue(node);
                        if (value != null) {
                            put(node, value);
                        }
                    }
                }
            }
        }
    }

    public NoSql reset(){
        root = new Node("");
        clearDataSavers();
        return this;
    }

    public NoSql clearDataSavers() {
        AndroidNoSql.clearDataSavers();
        return this;
    }

    public NoSql save() {
        clearDataSavers();
        root.save(AndroidNoSql.getDataSaver());
        return this;
    }

    /*
    public NoSql putXml(String uri, Object value) {

    }
    */

    public NoSql put(String uri, Object value) {
        final Pair<Node, String> node_and_name = getNode(uri);
        final Node node = node_and_name.first;
        final String name = node_and_name.second;

        if (NoSqlSerializerUtils.isValueObject(value)) {
            node.put(name, value);
            notifyListeners(node.path + PATH_SEPARATOR + name);
        } else {
            getOrCreate(node, name).write(value);
        }

        if (autoSave) {
            node.save(AndroidNoSql.getDataSaver());
        }

        return this;
    }

    private void notifyListeners(String path) {
        for (String key : listeners.keySet()) {
            if (path.startsWith(key)) {
                final Reference<Listener> listener = listeners.get(key);
                if (listener != null) {
                    final Listener listenerValue = listener.get();
                    if (listenerValue != null) {
                        listenerValue.nodeChanged(path, get(path));
                    }
                }
            }
        }


    }

    public <T> T get(String path, Class<T> aClass) {
        try {
            final Object instance = aClass.newInstance();
            final Set<Field> fields = NoSqlSerializerUtils.getAllFields(aClass);
            for (Field field : fields) {
                field.setAccessible(true);
                final String fieldName = field.getName();
                final String completeFieldPath = path + PATH_SEPARATOR + fieldName;

                if (NoSqlSerializerUtils.isPrimitiveField(field)) {
                    final Value value = get(completeFieldPath);

                    final Object fetchedValue = value.object();

                    try {
                        field.set(instance, fetchedValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (NoSqlSerializerUtils.isArray(field)) {

                } else if (NoSqlSerializerUtils.isCollection(field)) {
                    final List list;
                    if (NoSqlSerializerUtils.isInterface(field)) {
                        list = new ArrayList();
                    } else {
                        list = (List) field.getType().newInstance();
                    }

                    final Node node = getNodeOrCreate(completeFieldPath);
                    final Class childClass = NoSqlSerializerUtils.genericType(field);
                    if (NoSqlSerializerUtils.isPrimitive(childClass)) {
                        for (String key : node.values.keySet()) {
                            list.add(node.get(key));
                        }
                    } else {
                        for (String key : node.values.keySet()) {
                            final String pathChild = completeFieldPath + PATH_SEPARATOR + key;
                            final Object child = get(pathChild, childClass);
                            list.add(child);
                        }
                    }

                    try {
                        field.set(instance, list);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    final Object child = get(completeFieldPath, field.getType());
                    try {
                        field.set(instance, child);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
            return (T) instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void notify(String path, Listener listener) {
        listeners.put(path, new SoftReference<>(listener));
    }

    public void removeNotifier(Listener listener) {
        for (String key : listeners.keySet()) {
            final Reference<Listener> listenerReference = listeners.get(key);
            if (listener.equals(listenerReference.get())) {
                listeners.remove(key);
                break;
            }
        }
    }

    public Node node(String path) {
        return getNodeOrCreate(path);
    }

    public static class Value {

        private final Object object;

        public Value(Object object) {
            this.object = object;
        }

        public Object object() {
            return object;
        }

        public boolean isNode() {
            return object instanceof Node;
        }

        public Node node() {
            return ((Node) object);
        }

        public String string() {
            return String.valueOf(object);
        }

        public Boolean bool() {
            if (object instanceof Boolean) {
                return (Boolean) object;
            }
            return null;
        }

        public Integer integer() {
            if (object instanceof Integer) {
                return (Integer) object;
            }
            return null;
        }

        @Override
        public String toString() {
            return object.toString();
        }
    }

    public class Node {
        private final String path;
        private final Map<String, Object> values;

        public Node(String path) {
            this.path = path;
            this.values = new HashMap<>();
        }

        public String getPath() {
            return path;
        }

        @Override
        public String toString() {
            return path + " - " + values.toString();
        }

        public Object get(String key) {
            if (values.containsKey(key)) {
                return values.get(key);
            } else {
                final Node node = new Node(path + PATH_SEPARATOR + key);
                values.put(key, node);
                return node;
            }
        }

        public boolean has(String key) {
            return values.containsKey(key);
        }

        public Collection<String> keys() {
            return values.keySet();
        }

        public List<Node> childNodes() {
            final List<Node> nodes = new ArrayList<>();
            for (String key : keys()) {
                final Object child = values.get(key);
                if (child instanceof Node) {
                    nodes.add((Node) child);
                }
            }
            return nodes;
        }

        public Node child(String key) {
            return ((Node) get(key));
        }

        public void put(String name, Object node) {
            values.put(name, node);
            if (autoSave) {
                save(AndroidNoSql.getDataSaver());
            }
        }

        public void save(Collection<DataSaver> dataSaver) {
            {
                //save keys
                final String p = path + "/";
                final Set<String> keys = values.keySet();
                for (DataSaver saver : dataSaver) {
                    saver.saveNodes(p, keys);
                }
            }


            for (final String key : values.keySet()) {
                final Object value = values.get(key);

                final String completePath = path + PATH_SEPARATOR + key;
                if (NoSqlSerializerUtils.isPrimitiveObject(value)) {
                    for (DataSaver saver : dataSaver) {
                        saver.saveValue(completePath, value);
                    }
                } else if (value instanceof Node) {
                    ((Node) value).save(dataSaver);
                }
            }
        }

        public void remove(String key) {
            this.values.remove(key);
        }

        public void removeAll() {
            this.values.clear();
        }

        public Value value(String key) {
            return new Value(get(key));
        }

        protected void write(Object value) {
            if (value instanceof JSONObject) { //json object
                final JSONObject jsonObject = (JSONObject) value;
                final Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    final String key = keys.next();
                    try {
                        final Object childObject = jsonObject.get(key);
                        if (NoSqlSerializerUtils.isValueObject(childObject)) {
                            put(key, childObject);
                            notifyListeners(key);
                        } else {
                            child(key).write(childObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (value instanceof JSONArray) { //json array
                final JSONArray jsonArray = (JSONArray) value;
                final int length = jsonArray.length();
                for (int i = 0; i < length; ++i) {
                    try {
                        final Object childObject = jsonArray.get(i);
                        final String key = String.valueOf(i);

                        if (NoSqlSerializerUtils.isValueObject(childObject)) {
                            put(key, childObject);
                            notifyListeners(key);
                        } else {
                            child(key).write(childObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else { //custom object, write all fields
                final Set<Field> allFields = NoSqlSerializerUtils.getAllFields(value.getClass());
                for (Field field : allFields) {
                    field.setAccessible(true);

                    try {
                        final String fieldName = field.getName();
                        final Object fieldValue = field.get(value);
                        if (fieldValue != null) {

                            final String completePath = path + PATH_SEPARATOR + fieldName;

                            if (NoSqlSerializerUtils.isValueObject(fieldValue)) {
                                put(fieldName, fieldValue);
                                notifyListeners(completePath);
                            } else {
                                final Node node1 = new Node(completePath);
                                put(fieldName, node1);
                                if (NoSqlSerializerUtils.isCollection(field)) {
                                    final Iterable collection = (Iterable) field.get(value);
                                    int index = 0;
                                    for (Object childObject : collection) {
                                        final String key = String.valueOf(index);
                                        if (NoSqlSerializerUtils.isValueObject(childObject)) {
                                            node1.put(key, childObject);
                                            notifyListeners(key);
                                        } else {
                                            node1.child(key).write(childObject);
                                        }
                                        index++;
                                    }
                                } else if (NoSqlSerializerUtils.isArray(field)) {
                                    final Object[] array = (Object[]) field.get(value);
                                    int index = 0;
                                    for (Object childObject : array) {
                                        final String key = String.valueOf(index);
                                        if (NoSqlSerializerUtils.isValueObject(childObject)) {
                                            node1.put(key, childObject);
                                            notifyListeners(key);
                                        } else {
                                            node1.child(key).write(childObject);
                                        }
                                        index++;
                                    }
                                } else {
                                    node1.write(fieldValue);
                                }
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            notifyListeners(path);
        }
    }

    private class NodeDescritption {
        final List<String> path;
        final String name;

        public NodeDescritption(List<String> path, String name) {
            this.path = path;
            this.name = name;
        }
    }
}
