package de.tigges.tchreservation.news;

import de.tigges.tchreservation.ValidatorTest;
import de.tigges.tchreservation.util.exception.ErrorCode;
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
        var news = createNews("");

        checkNewsFieldErrorNullNotAllowed(news, "subject");
    }

    @Test
    void noText() {
        var news = createNews("my subject");

        checkNewsFieldErrorNullNotAllowed(news, "text");
    }

    private void checkNewsFieldErrorNullNotAllowed(News news, String expectedField) {
        checkFieldError(() -> newsValidator.validate(news), ErrorCode.NULL_NOT_ALLOWED, expectedField);
    }

    private News createNews(String subject) {
        return new News(1L, subject, "url", "", LocalDateTime.now());
    }
}
