package com.example.mobilecafeapplication

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobilecafeapplication.TestingHelperFunctions.Companion.createUserAndLogin
import com.example.mobilecafeapplication.controller.LoginScreen
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileUITesting {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginScreen::class.java)

    /**
     * setupTest
     *
     * Before the test we need to create a user and login so that we can see their details
     * on the profile fragment
     * @param name the name of the user to create
     * @param email the email for the created user
     * @param password the password to assign to the created user account
     */
    private fun setupTest(name: String, email: String, password: String) {
        //Create user
        createUserAndLogin(name, email, password)

        //Navigate to profile screen
        onView(withId(R.id.navProfile)).perform(click())
    }

    @Test
    fun updateEmail() {
        val name: String = "Dan James"
        var email: String = "dan.james@outlook.com"
        val password: String = "D4J4m3s123"

        setupTest(name, email, password)
        onView(withId(R.id.edtTextUserFullName)).check(matches(withText(name)))
        onView(withId(R.id.edtTextUserEmail)).check(matches(withText(email)))
        onView(withId(R.id.edtTextPhoneNo)).check(matches(withText("")))

        //Update Email without error
        email = "james.dan@outlook.com"
        onView(withId(R.id.edtTextUserEmail)).perform(replaceText(email))
        onView(withId(R.id.btnUpdateDetails)).perform(click())
        onView(withText(R.string.enterEmail)).inRoot(ToastMatcher().apply { matches(not(isDisplayed())) })
        onView(withId(R.id.edtTextUserEmail)).check(matches(withText(email)))

        //Trigger email error
        email = "james.dan"
        onView(withId(R.id.edtTextUserEmail)).perform(replaceText(email))
        onView(withId(R.id.btnUpdateDetails)).perform(click())
        onView(withText(R.string.enterEmail)).inRoot(ToastMatcher().apply { matches(isDisplayed()) })
    }

    @Test
    fun updateName() {
        var name: String = "James Jameson"
        val email: String = "james.jameson@outlook.com"
        val password: String = "J4m3sJ4mes0n"

        setupTest(name, email, password)
        onView(withId(R.id.edtTextUserFullName)).check(matches(withText(name)))
        onView(withId(R.id.edtTextUserEmail)).check(matches(withText(email)))
        onView(withId(R.id.edtTextPhoneNo)).check(matches(withText("")))

        name = "Jamie Jameson"
        onView(withId(R.id.edtTextUserFullName)).perform(replaceText(name))
        onView(withId(R.id.btnUpdateDetails)).perform(click())
        onView(withId(R.id.edtTextUserFullName)).check(matches(withText(name)))
    }

    @Test
    fun updatePhone() {
        val name: String = "Jack Skipping"
        val email: String = "jack.skipping@gmail.com"
        val password: String = "J4ckSk1pp1ng"

        setupTest(name, email, password)
        onView(withId(R.id.edtTextUserFullName)).check(matches(withText(name)))
        onView(withId(R.id.edtTextUserEmail)).check(matches(withText(email)))
        onView(withId(R.id.edtTextPhoneNo)).check(matches(withText("")))

        val phone = "07481573562"
        onView(withId(R.id.edtTextPhoneNo)).perform(replaceText(phone))
        onView(withId(R.id.btnUpdateDetails)).perform(click())
        onView(withId(R.id.edtTextUserFullName)).check(matches(withText(name)))
    }

    @Test
    fun updatePassword() {
        val name: String = "Darcey Ismahel"
        val email: String = "darcey.ismahel@outlook.com"
        val password: String = "D4rc3yismahel"
        val newPassword: String = "D4rc3yIsm4h3l"

        setupTest(name, email, password)

        //Check password fields empty

        /*
        Fill in password fields with old Password
        Error should be raised saying you cannot replace password with same password
         */
        onView(withId(R.id.edtTxtOldPassword)).perform(replaceText(password))
        onView(withId(R.id.edtTxtNewPassword)).perform(replaceText(password))
        onView(withId(R.id.edtTxtConfirmPassword)).perform(replaceText(password))
        onView(withId(R.id.btnChangePassword)).perform(click())
        onView(withText(R.string.newOldPasswordMatch)).inRoot(ToastMatcher().apply {
            matches(
                isDisplayed()
            )
        })

        /*
        Fill in password fields with old password and new password
        Make new password confirmation different
        Error should be raised saying passwords do not match
         */
        onView(withId(R.id.edtTxtOldPassword)).perform(replaceText(password))
        onView(withId(R.id.edtTxtNewPassword)).perform(replaceText(newPassword))
        onView(withId(R.id.edtTxtConfirmPassword)).perform(replaceText("password"))
        onView(withId(R.id.btnChangePassword)).perform(click())
        onView(withText(R.string.passwordsMatch)).inRoot(ToastMatcher().apply { matches(isDisplayed()) })


        /*
        Fill in details correctly
        Should be logged out. Login with new password should land home screen
         */
        onView(withId(R.id.edtTxtOldPassword)).perform(replaceText(password))
        onView(withId(R.id.edtTxtNewPassword)).perform(replaceText(newPassword))
        onView(withId(R.id.edtTxtConfirmPassword)).perform(replaceText(newPassword))
        onView(withId(R.id.btnChangePassword)).perform(click())
        onView(withId(R.id.loginScreen)).check(matches(isDisplayed()))
    }

    @Test
    fun deleteAccount() {
        val name = "James James"
        val userToDeleteEmail = "james@gmail.com"
        val userToDeletePassword = "Password123"

        setupTest(name, userToDeleteEmail, userToDeletePassword)
        onView(withId(R.id.navProfile)).perform(click())
        onView(withId(R.id.fragProfile)).check(matches(isDisplayed()))
        onView(withId(R.id.profileScreenBtnDeleteAccount)).perform(scrollTo(), click())
        onView(withId(R.id.loginScreen)).check(matches(isDisplayed()))
        TestingHelperFunctions.fillInLoginDetails(userToDeleteEmail, userToDeletePassword)
        onView(withId(R.id.loginScreenBtnLogin)).perform(click())
        onView(withText(R.string.invalidLogin)).inRoot(ToastMatcher().apply {
            matches(isDisplayed())
        })
    }
}