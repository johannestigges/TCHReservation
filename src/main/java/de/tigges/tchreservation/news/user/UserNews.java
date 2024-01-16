package de.tigges.tchreservation.news.user;

public record UserNews(long userId, long newsId, boolean acknowledged) {
}
