package de.tigges.tchreservation.news.user.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNewsRepository extends CrudRepository<UserNewsEntity, Long> {
    Iterable<UserNewsEntity> findAllByIdUserId(long userId);

    void deleteByIdNewsId(long newsId);
}
