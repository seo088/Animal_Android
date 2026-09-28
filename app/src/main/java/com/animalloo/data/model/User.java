package com.animalloo.data.model;

import java.util.Objects;

/**
 * Mock authenticated user profile.
 */
public class User {

    private final String id;
    private final String displayName;
    private final String email;

    public User(String id, String displayName, String email) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User)) {
            return false;
        }
        User user = (User) other;
        return Objects.equals(id, user.id)
                && Objects.equals(displayName, user.displayName)
                && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, email);
    }
}
