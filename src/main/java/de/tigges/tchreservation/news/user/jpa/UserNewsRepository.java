package de.tigges.tchreservation.news.user.jpa;

import org.springframework.data.repository.CrudRepository;

public interface UserNewsRepository extends CrudRepository<UserNewsEntity,Long> {
    Iterable<UserNewsEntity> findAllByUserId(long userId);
}
