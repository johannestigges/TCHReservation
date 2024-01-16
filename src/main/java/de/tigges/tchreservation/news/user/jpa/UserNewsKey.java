package de.tigges.tchreservation.news.user.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Embeddable
@Getter
@AllArgsConstructor
public class UserNewsKey implements Serializable {
    @Column(name = "user_id")
    Long userId;
    @Column(name = "news_id")
    Long newsId;
}
