package com.vehiclerental.model;

import java.io.Serializable;

/** What the login session remembers. Stored as the "currentUser" session attribute. */
public class SessionUser implements Serializable {

    public static final String SESSION_KEY = "currentUser";

    private final long id;
    private final String fullName;
    private final String email;
    private final Role role;

    public SessionUser(long id, String fullName, String email, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
