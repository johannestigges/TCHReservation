package de.tigges.tchreservation.news.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;

public record News(
        long id,
        @Nonnull String subject,
        @Nullable String url,
        @Nonnull String text,
        @Nullable LocalDateTime createdAt) {
}
