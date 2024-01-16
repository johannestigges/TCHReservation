package de.tigges.tchreservation.news.jpa;

import de.tigges.tchreservation.news.user.jpa.UserNewsEntity;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@AllArgsConstructor
public class NewsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(nullable = false, length = 255)
    String subject;
    @Column(nullable = false,length = 4000)
    String text;
    @CreatedDate
    LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    Set<UserNewsEntity> users;

    public NewsEntity(@Nonnull String subject, @Nonnull String text) {
        this.subject = subject;
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }
}

