package de.tigges.tchreservation.news.user;

import de.tigges.tchreservation.UserTest;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserNewsServiceTest extends UserTest {

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

    @Test
    void acknowledge() throws Exception {

        performPost("/rest/news/user/myUserId", userIds())
                .andExpect(status().isFound())
                .andReturn();
    }

    private List<Long> userIds() {
        return users.stream().map(UserEntity::getId).toList();
    }
}