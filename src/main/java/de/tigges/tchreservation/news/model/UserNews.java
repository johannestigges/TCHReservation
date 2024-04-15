package de.tigges.tchreservation.news.model;

public record UserNews(long userId, long newsId, boolean acknowledged) {
}
