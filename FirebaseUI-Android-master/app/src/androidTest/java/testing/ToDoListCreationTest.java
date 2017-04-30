/*package testing;

import com.firebase.uidemo.BuildConfig;
import com.firebase.uidemo.ChooserActivity;
import com.firebase.uidemo.SignInActivity;
import com.firebase.uidemo.todolist.ToDoListActivity;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.util.Collections;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class ToDoListCreationTest {
    private ChooserActivity activity;
    @Before
    public void setUp() {

        activity = Robolectric
                .setupActivity(ChooserActivity.class);
        FirebaseApp.initializeApp(activity);
    }

    @Test
    public void shouldNotBeNull() throws Exception
    {
        assertNotNull(activity);
    }*/