package app.jayang.icebr8k;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import static android.support.test.espresso.Espresso.onView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * Created by Arie on 2/22/2018.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignInTestWithGoogle {
    @Rule
    public ActivityTestRule<login_page> mActivityRule =
            new ActivityTestRule(login_page.class);

    @Test
    public void listGoesOverTheFold() {
        onView(withText("Hello world!")).check(matches(isDisplayed()));
    }
}
