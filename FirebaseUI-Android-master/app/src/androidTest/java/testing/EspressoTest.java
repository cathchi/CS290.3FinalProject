package testing;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.firebase.uidemo.R;
import com.firebase.uidemo.todolist.ToDoListActivity;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by JasmineLu on 4/29/17.
 */
@RunWith(AndroidJUnit4.class)
public class EspressoTest {
    public static final String STRING_TO_BE_TYPED = "Espresso";

    @Rule
    public ActivityTestRule<ToDoListActivity> mActivityRule = new ActivityTestRule<>(
            ToDoListActivity.class);

    @Test
    public void test_changeText_sameActivity() {
        // Type text and then press the button.
        onView(withId(R.id.todoText))
                .perform(typeText(STRING_TO_BE_TYPED), closeSoftKeyboard());
        //onView(withId(R.id.addButton)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.todoText)).check(matches(withText(STRING_TO_BE_TYPED)));
    }
}
