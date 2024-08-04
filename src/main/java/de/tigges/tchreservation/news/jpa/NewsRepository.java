package de.tigges.tchreservation.news.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends CrudRepository<NewsEntity, Long> {

    Iterable<NewsEntity> findAllByOrderByCreatedAtDesc();
}
