package com.vehiclerental.web;

import com.vehiclerental.model.Role;
import com.vehiclerental.model.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** MockMvc does not render JSPs, so these check the status, the redirect and the forwarded view only. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@TestPropertySource(properties = {
        "app.seed-demo-data=false",
        "spring.datasource.url=jdbc:h2:mem:page_security_test;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
class PageSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void adminPageRedirectsToLoginWithoutASession() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", startsWith("/login")));
    }

    @Test
    void adminPageIsForbiddenForACustomer() throws Exception {
        mockMvc.perform(get("/admin").session(sessionFor(Role.CUSTOMER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void bookingsPageRedirectsToLoginWithoutASession() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", startsWith("/login")));
    }

    @Test
    void vehiclesPageIsPublic() throws Exception {
        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrlPattern("/WEB-INF/jsp/**/*.jsp"));
    }

    private MockHttpSession sessionFor(Role role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionUser.SESSION_KEY, new SessionUser(1L, "Test User", "test@example.test", role));
        return session;
    }
}
