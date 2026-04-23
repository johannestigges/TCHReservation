package de.tigges.tchreservation.news.user;

import de.tigges.tchreservation.news.model.UserNews;
import de.tigges.tchreservation.news.user.jpa.UserNewsEntity;
import de.tigges.tchreservation.news.user.jpa.UserNewsKey;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserNewsMapperTest {

    @Test
    void mapEntityToModel() {
        var entity = new UserNewsEntity(new UserNewsKey(10L, 20L), true);

        var model = UserNewsMapper.map(entity);

        assertThat(model).isNotNull();
        assertThat(model.userId()).isEqualTo(10L);
        assertThat(model.newsId()).isEqualTo(20L);
        assertThat(model.acknowledged()).isTrue();
    }

    @Test
    void mapEntityToModelNotAcknowledged() {
        var entity = new UserNewsEntity(new UserNewsKey(1L, 2L), false);

        var model = UserNewsMapper.map(entity);

        assertThat(model.acknowledged()).isFalse();
    }

    @Test
    void mapModelToEntity() {
        var model = new UserNews(30L, 40L, true);

        var entity = UserNewsMapper.map(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId().getUserId()).isEqualTo(30L);
        assertThat(entity.getId().getNewsId()).isEqualTo(40L);
        assertThat(entity.isAcknowledged()).isTrue();
    }

    @Test
    void mapModelToEntityNotAcknowledged() {
        var model = new UserNews(5L, 6L, false);

        var entity = UserNewsMapper.map(model);

        assertThat(entity.isAcknowledged()).isFalse();
    }
}
