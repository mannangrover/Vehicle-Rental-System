package com.vehiclerental.config;

/** The accounts DataSeeder creates, also offered as one-click logins on the login page. */
public final class DemoAccounts {

    public static final Account ADMIN = new Account("admin@roadready.test", "Admin@123");
    public static final Account CUSTOMER = new Account("customer@roadready.test", "Customer@123");

    private DemoAccounts() {
    }

    public static class Account {

        private final String email;
        private final String password;

        Account(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }
}
