package org.creators.android.data;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by Damian Wieczorek <damianw@umich.edu> on 7/26/14.
 */
@ParseClassName(User.CLASS)
public class User extends ParseUser {
  public static final String CLASS = "_User";

  public static final String FIRST_NAME = "firstName";
  public static final String LAST_NAME = "lastName";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String EMAIL_VERIFIED = "emailVerified";
  public static final String BIO = "bio";
  public static final String MAJOR = "major";
  public static final String SELFIE = "selfie";
  public static final String PHONE = "phone";

  public User() {

  }

  public static User getCurrentUser() {
    return (User) ParseUser.getCurrentUser();
  }

  public static ParseQuery<User> query() {
    return ParseQuery.getQuery(User.class);
  }

  public String getFirstName() {
    return getString(FIRST_NAME);
  }

  public User setFirstName(String firstName) {
    put(FIRST_NAME, firstName);
    return this;
  }

  public String getLastName() {
    return getString(LAST_NAME);
  }

  public User setLastName(String lastName) {
    put(LAST_NAME, lastName);
    return this;
  }

  public String getFullName() {
    return getFirstName() + " " + getLastName();
  }

  public boolean isEmailVerified() {
    return getBoolean(EMAIL_VERIFIED);
  }

  public User setEmailVerified(boolean verified) {
    put(EMAIL_VERIFIED, verified);
    return this;
  }

  public String getBio() {
    return getString(BIO);
  }

  public User setBio(String bio) {
    put(BIO, bio);
    return this;
  }

  public String getMajor() {
    return getString(MAJOR);
  }

  public User setMajor(String major) {
    put(MAJOR, major);
    return this;
  }

  public ParseFile getSelfie() {
    return getParseFile(SELFIE);
  }

  public User setSelfie(ParseFile selfie) {
    put(SELFIE, selfie);
    return this;
  }

  public String getPhone() {
    return getString(PHONE);
  }

  public User setPhone(String phone) {
    put(PHONE, phone);
    return this;
  }

}
