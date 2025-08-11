package de.tigges.tchreservation.news;

import de.tigges.tchreservation.ValidatorTest;
import de.tigges.tchreservation.util.exception.ErrorCode;
import de.tigges.tchreservation.util.exception.NotFoundException;
import de.tigges.tchreservation.news.jpa.NewsEntity;
import de.tigges.tchreservation.news.jpa.NewsRepository;
import de.tigges.tchreservation.news.model.News;
import de.tigges.tchreservation.news.user.jpa.UserNewsRepository;
import de.tigges.tchreservation.user.LoggedinUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest extends ValidatorTest {
    @Mock
    private LoggedinUserService loggedinUserService;
    @Mock
    private NewsRepository newsRepository;
    @Mock
    private UserNewsRepository userNewsRepository;

    private NewsService newsService;

    @BeforeEach
    void createNewsService() {
        newsService = new NewsService(
                loggedinUserService,
                new NewsValidator(createValidator()),
                newsRepository,
                userNewsRepository);
    }

    @Test
    void getAll() {
        when(newsRepository.findAllByOrderByCreatedAtDesc()).thenReturn(
                List.of(
                        new NewsEntity("Subject 1", "Text 1"),
                        new NewsEntity("Subject 2", "Text 2")
                ));

        var news = newsService.getAll();

        assertThat(news).isNotNull();
        assertThat(news).hasSize(2);
    }

    @Test
    void getAllNoNews() {
        when(newsRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Collections.emptyList());

        var news = newsService.getAll();

        assertThat(news).isNotNull();
        assertThat(news).isEmpty();
    }

    @Test
    void getOne() {
        when(newsRepository.findById(1L))
                .thenReturn(Optional.of(new NewsEntity(
                        "my subject",
                        "my text")));

        var news = newsService.getOne(1L);

        assertThat(news.subject()).isEqualTo("my subject");
        assertThat(news.text()).isEqualTo("my text");
    }

    @Test
    void getOneNotFound() {
        initMessageSource(ErrorCode.NOT_FOUND, "NEWS with id 1 not found");
        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> newsService.getOne(1L));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getErrorMessages()).hasSize(1);
        assertThat(exception.getErrorMessages().stream().toList().getFirst().message())
                .isEqualTo("NEWS with id 1 not found");
    }

    @Test
    void add() {
        var captor = ArgumentCaptor.forClass(NewsEntity.class);
        when(newsRepository.save(captor.capture()))
                .thenReturn(new NewsEntity(
                        "my saved subject",
                        "my saved text"));

        var news = newsService.add(new News(
                1L,
                "my subject",
                "my url",
                "my text",
                LocalDateTime.now()));

        var entity = captor.getValue();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getSubject()).isEqualTo("my subject");
        assertThat(entity.getUrl()).isEqualTo("my url");
        assertThat(entity.getText()).isEqualTo("my text");
        assertThat(news.subject()).isEqualTo("my saved subject");
        assertThat(news.text()).isEqualTo("my saved text");
    }

    @Test
    void update() {
        var captor = ArgumentCaptor.forClass(NewsEntity.class);
        when(newsRepository.save(captor.capture()))
                .thenReturn(new NewsEntity(
                        "my saved subject",
                        "my saved text"));
        when(newsRepository.findById(2L))
                .thenReturn(Optional.of(new NewsEntity(
                        "my subject",
                        "my text")));

        var news = newsService.update(new News(
                2L,
                "my subject",
                "my url",
                "my text",
                LocalDateTime.now()));

        assertThat(news.subject()).isEqualTo("my saved subject");
        assertThat(news.text()).isEqualTo("my saved text");
    }

    @Test
    void updateNotFound() {
        initMessageSource(ErrorCode.NOT_FOUND, "NEWS with id 3 not found");
        when(newsRepository.findById(3L)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> newsService.update(new News(
                        3L,
                        "my subject",
                        "my url",
                        "my text",
                        LocalDateTime.now())));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getErrorMessages()).hasSize(1);
        assertThat(exception.getErrorMessages().stream().toList().getFirst()
                .message()).isEqualTo("NEWS with id 3 not found");
    }
}
