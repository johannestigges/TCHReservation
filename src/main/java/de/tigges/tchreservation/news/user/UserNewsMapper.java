package de.tigges.tchreservation.news.user;

import de.tigges.tchreservation.news.user.jpa.UserNewsEntity;

public class UserNewsMapper {
    public static UserNews map(UserNewsEntity e) {
        return new UserNews(e.getId().getUserId(), e.getId().getNewsId(), e.isAcknowledged());
    }

    public static UserNewsEntity map(UserNews u) {
        return new UserNewsEntity(u.userId(), u.newsId(), u.acknowledged());
    }
}
