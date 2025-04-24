package de.tigges.tchreservation.news.user;

import de.tigges.tchreservation.UserTest;
import de.tigges.tchreservation.news.jpa.NewsEntity;
import de.tigges.tchreservation.news.jpa.NewsRepository;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserNewsServiceTest extends UserTest {

    @Autowired
    private NewsRepository newsRepository;

    private NewsEntity news;

    @BeforeEach
    void addUsers() {
        Stream.of(UserRole.ADMIN, UserRole.REGISTERED, UserRole.TRAINER)
                .forEach(this::addUser);
    }

    @AfterEach
    void removeUsers() {
        deleteAllUsers();
    }

    @BeforeEach
    void addNews() {
        news = newsRepository.save(new NewsEntity("my subject", "my text"));
    }

    @AfterEach()
    void removeNews() {
        newsRepository.deleteById(news.getId());
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    void acknowledge() throws Exception {
        performPost("/rest/news/user/acknowledge", List.of(news.getId()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void acknowledgeWithoutLogin() throws Exception {
        performPost("/rest/news/user/acknowledge", List.of(news.getId()))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }
}
