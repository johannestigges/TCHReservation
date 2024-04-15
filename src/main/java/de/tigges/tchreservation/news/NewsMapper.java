package de.tigges.tchreservation.news;

import de.tigges.tchreservation.news.jpa.NewsEntity;
import de.tigges.tchreservation.news.model.News;

import java.util.Collections;

public class NewsMapper {
    public static News map(NewsEntity n) {
        return new News(
                n.getId(),
                n.getSubject(),
                n.getUrl(),
                n.getText(),
                n.getCreatedAt());
    }

    public static NewsEntity map(News n) {
        return new NewsEntity(
                n.id(),
                n.subject(),
                n.url(),
                n.text(),
                n.createdAt(),
                Collections.emptySet());
    }
}
