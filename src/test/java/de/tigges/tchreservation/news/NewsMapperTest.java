package de.tigges.tchreservation.news;

import de.tigges.tchreservation.news.jpa.NewsEntity;
import de.tigges.tchreservation.news.model.News;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class NewsMapperTest {

    @Test
    void mapEntityToModel() {
        var createdAt = LocalDateTime.of(2026, 4, 1, 10, 0);
        var entity = new NewsEntity(99L, "Important Update", "https://example.com", "Details here", createdAt, Collections.emptySet());

        var model = NewsMapper.map(entity);

        assertThat(model).isNotNull();
        assertThat(model.id()).isEqualTo(99L);
        assertThat(model.subject()).isEqualTo("Important Update");
        assertThat(model.url()).isEqualTo("https://example.com");
        assertThat(model.text()).isEqualTo("Details here");
        assertThat(model.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void mapEntityToModelNullUrl() {
        var entity = new NewsEntity(1L, "No URL", null, "Some text", null, Collections.emptySet());

        var model = NewsMapper.map(entity);

        assertThat(model.url()).isNull();
        assertThat(model.createdAt()).isNull();
    }

    @Test
    void mapModelToEntity() {
        var createdAt = LocalDateTime.of(2026, 3, 15, 8, 30);
        var news = new News(5L, "Club news", "https://club.de", "Welcome!", createdAt);

        var entity = NewsMapper.map(news);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getSubject()).isEqualTo("Club news");
        assertThat(entity.getUrl()).isEqualTo("https://club.de");
        assertThat(entity.getText()).isEqualTo("Welcome!");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUsers()).isEmpty();
    }

    @Test
    void mapModelToEntityNullId() {
        var news = new News(null, "Subject", null, "Text", null);

        var entity = NewsMapper.map(news);

        assertThat(entity.getId()).isNull();
    }
}
