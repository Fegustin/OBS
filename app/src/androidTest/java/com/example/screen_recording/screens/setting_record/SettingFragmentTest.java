package com.example.screen_recording.screens.setting_record;

import android.Manifest;
import android.view.View;
import android.widget.Checkable;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.example.screen_recording.R;
import com.example.screen_recording.screens.MainActivity;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

@RunWith(AndroidJUnit4.class)
public class SettingFragmentTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO
    );

    @Before
    public void setFragment() {
        mActivityRule.getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SettingFragment())
                .commit();
    }

    public static ViewAction setChecked(final boolean checked) {
        return new ViewAction() {
            @Override
            public BaseMatcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return isA(Checkable.class).matches(item);
                    }

                    @Override
                    public void describeMismatch(Object item, Description mismatchDescription) {}

                    @Override
                    public void describeTo(Description description) {}
                };
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Checkable checkableView = (Checkable) view;
                checkableView.setChecked(checked);
            }
        };
    }

    @Test
    public void onClickQuality() {
        onView(withId(R.id.layoutQuality))
                .perform(scrollTo(), click());

        onData(allOf(is(instanceOf(String.class)), is("Высокое")))
                .perform(scrollTo(), click());

        onView(withId(R.id.textViewQuality))
                .check(matches(withText("Высокое")));
    }

    @Test
    public void onClickMicro() {
        onView(withId(R.id.checkBoxMicro))
                .perform(scrollTo(), setChecked(true));
    }

    @Test
    public void onClickFPS() {
        onView(withId(R.id.layoutFPS))
                .perform(scrollTo(), click());

        onData(allOf(is(instanceOf(String.class)), is("15FPS")))
                .perform(scrollTo(), click());

        onView(withId(R.id.textViewFPS))
                .check(matches(withText("15FPS")));
    }

    @Test
    public void onClickDarkTheme() {
        onView(withId(R.id.switchDarckTheme))
                .perform(scrollTo(), setChecked(true));
    }
}