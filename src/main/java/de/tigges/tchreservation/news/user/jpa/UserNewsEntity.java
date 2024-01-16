package de.tigges.tchreservation.news.user.jpa;

import de.tigges.tchreservation.news.jpa.NewsEntity;
import de.tigges.tchreservation.user.jpa.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Entity
@Getter
@AllArgsConstructor
public class UserNewsEntity {
    @EmbeddedId
    UserNewsKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    UserEntity user;

    @ManyToOne
    @MapsId("newsId")
    @JoinColumn(name = "news_id")
    NewsEntity news;
    boolean acknowledged;
}
