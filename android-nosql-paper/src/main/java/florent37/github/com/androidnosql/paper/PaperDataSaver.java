package florent37.github.com.androidnosql.paper;

import android.content.Context;

import com.github.florent37.androidnosql.datasaver.DataSaver;

import java.util.HashSet;
import java.util.Set;

import io.paperdb.Book;
import io.paperdb.Paper;

/**
 * Created by florentchampigny on 06/06/2017.
 */

public class PaperDataSaver implements DataSaver {

    public static final String NODES = "nodes";
    public static final String VALUES = "values";

    public PaperDataSaver(Context context){
        Paper.init(context);
    }

    @Override
    public void saveNodes(String completePath, Set<String> values) {
        final Set<String> nodes = new HashSet<>();
        nodes.add(completePath);
        nodes.addAll(getNodes());
        for (String value : values) {
            nodes.add(completePath + value);
        }

        Paper.book(NODES).write(NODES, nodes);
    }

    @Override
    public void saveValue(String completePath, Object value) {

        completePath = formatPath(completePath);

        final Book book = Paper.book(VALUES);
        if(value instanceof Integer) {
            book.write(completePath, (Integer) value);
        } else if(value instanceof Float) {
            book.write(completePath, (Float) value);
        } else if(value instanceof Boolean) {
            book.write(completePath, (Boolean) value);
        } else if(value instanceof Long) {
            book.write(completePath, (Long) value);
        } else {
            book.write(completePath, String.valueOf(value));
        }
    }

    @Override
    public Set<String> getNodes() {
        return Paper.book(NODES).read(NODES, new HashSet<String>());
    }

    @Override
    public Object getValue(String completePath) {
        return Paper.book(VALUES).read(formatPath(completePath));
    }

    private String formatPath(String path){
        return path.replace("/", "_%-");
    }

    @Override
    public void remove(String startingPath) {
        final Book bookNodes = Paper.book(NODES);
        final Book bookValues = Paper.book(VALUES);
        final Set<String> nodes = getNodes();
        final Set<String> nodesToKeep = new HashSet<>();
        //update values
        for (String node : nodes) {
            if(node.startsWith(startingPath)) {
                bookValues.delete(formatPath(node));
            } else {
                nodesToKeep.add(node);
            }
        }
        bookNodes.write(NODES, nodesToKeep);
    }
}
