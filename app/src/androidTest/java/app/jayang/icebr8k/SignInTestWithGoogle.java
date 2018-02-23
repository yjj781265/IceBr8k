package app.jayang.icebr8k;

import android.content.Intent;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import static android.support.test.espresso.Espresso.onView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;

import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;

import static android.support.test.espresso.matcher.ViewMatchers.withId;



/**
 * Created by Arie on 2/22/2018.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignInTestWithGoogle {


    @Rule
    public ActivityTestRule<login_page> mLoginActivityActivityTestRule =
            new ActivityTestRule(login_page.class);

    @Test
    public void listGoesOverTheFold() {
        onView(withId(R.id.email_login)).perform(click()).perform(typeTextIntoFocusedView("fake@gmail.com"));
        onView(withId(R.id.password_login)).perform(click()).perform(typeTextIntoFocusedView("yjj781265"));


    }



}
