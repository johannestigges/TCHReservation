package de.tigges.tchreservation.news.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@Repository
public interface NewsRepository extends CrudRepository<NewsEntity, Long> {

    Stream<NewsEntity> findAllByOrderByCreatedAtDesc();

    Stream<NewsEntity> findAllByCreatedAtBefore(LocalDateTime expiryDate);
}
