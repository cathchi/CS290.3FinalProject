package compsci290.edu.duke.todolist;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Katherine on 4/14/2017.
 */

public class NewListCreater {

    private String title;

    public NewListCreater(String listname) {
        title = listname;
    }

    public NewListCreater() {
        this(null);
    }

    public String addToFirebase() {
        String id = createId();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference().child(id);
        return id;
    }

    private String createId() {
        String s = title.replaceAll("\\s+","");
        return s;
    }
}
