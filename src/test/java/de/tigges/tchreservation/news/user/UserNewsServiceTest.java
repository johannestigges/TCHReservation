package de.tigges.tchreservation.news.user;

import de.tigges.tchreservation.UserTest;
import de.tigges.tchreservation.news.jpa.NewsEntity;
import de.tigges.tchreservation.news.jpa.NewsRepository;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserNewsServiceTest extends UserTest {

    @Autowired private NewsRepository newsRepository;

    private static List<UserEntity> users;

    @BeforeEach
    void addUsers() {
        users = Stream.of(UserRole.ADMIN, UserRole.REGISTERED, UserRole.TRAINER)
                .map(this::addUser)
                .toList();
    }

    @AfterEach
    void removeUsers() {
        deleteAllUsers();
    }

    @BeforeEach void addNews() {
        newsRepository.save(new NewsEntity(
                1L,
                "my subject",
                "my text",
                "my url",
                LocalDateTime.now(),
                Collections.emptySet()
        ));
    }
    @AfterEach() void removeNews() {
        newsRepository.deleteById(1L);
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    void acknowledge() throws Exception {
        performPost("/rest/news/user/acknowledge", 1L)
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void acknowledgeWithoutLogin() throws Exception {
        performPost("/rest/news/user/acknowledge", 1L)
                .andExpect(status().isUnauthorized())
                .andReturn();
    }
}
