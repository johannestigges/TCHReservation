package de.tigges.tchreservation.news;

import de.tigges.tchreservation.ValidatorTest;
import de.tigges.tchreservation.news.model.News;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class NewsValidatorTest extends ValidatorTest {

    private NewsValidator newsValidator;

    @BeforeEach
    void init() {
        newsValidator = new NewsValidator(createValidator());
    }

    @Test
    void noSubject() {
        var news = createNews("", null);

        checkNewsFieldError(news, "error_null_not_allowed", "subject");
    }

    @Test
    void noText() {
        var news = createNews("my subject", null);

        checkNewsFieldError(news, "error_null_not_allowed", "text");
    }

    private void checkNewsFieldError(News news, String expectedError, String expectedField) {
        checkFieldError(() -> newsValidator.validate(news), expectedError, expectedField);
    }

    private News createNews(String subject, String text) {
        return new News(1L, subject, "url", text, LocalDateTime.now());
    }
}
