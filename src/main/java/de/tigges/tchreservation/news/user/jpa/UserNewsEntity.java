package de.tigges.tchreservation.news.user.jpa;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Entity
@Getter
@AllArgsConstructor
@Table(name = "user_news")
public class UserNewsEntity {
    @EmbeddedId
    UserNewsKey id;

    boolean acknowledged;

    public UserNewsEntity(long userId, long newsId, boolean acknowledged) {
        this(new UserNewsKey(userId, newsId), acknowledged);
    }
}
