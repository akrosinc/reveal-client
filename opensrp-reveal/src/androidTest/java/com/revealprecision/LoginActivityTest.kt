package com.revealprecision

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.revealprecision.data.SampleData
import org.junit.*
import org.junit.runner.*
import org.smartregister.reveal.R
import org.smartregister.reveal.activity.LoginActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun successLogin() {
        onView(withId(R.id.login_user_name_edit_text)).perform(typeText(SampleData.VALID_USER_NAME), closeSoftKeyboard())
        onView(withId(R.id.login_password_edit_text)).perform(typeText(SampleData.VALID_PASSWORD), closeSoftKeyboard())
        clickLoginButton()
        onView(withId(R.id.kujakuMapView)).check(matches(isDisplayed()))
    }

    @Test
    fun invalidUserLogin(){
        onView(withId(R.id.login_user_name_edit_text)).perform(typeText(SampleData.INVALID_USER), closeSoftKeyboard())
        onView(withId(R.id.login_password_edit_text)).perform(typeText(SampleData.INVALID_PASSWORD), closeSoftKeyboard())
        clickLoginButton()
        onView(withText(SampleData.LOGIN_FAILED_MESSAGE)).check(matches(isDisplayed()))
    }

    private fun clickLoginButton() {
        onView(withId(R.id.login_login_btn)).perform(click())
    }
}