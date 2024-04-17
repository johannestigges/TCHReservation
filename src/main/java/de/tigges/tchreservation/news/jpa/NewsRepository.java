package de.tigges.tchreservation.news.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NewsRepository extends CrudRepository<NewsEntity, Long> {

    Iterable<NewsEntity> findAllByOrderByCreatedAtDesc();

    Iterable<NewsEntity> findAllByCreatedAtBefore(LocalDateTime expiryDate);
}
