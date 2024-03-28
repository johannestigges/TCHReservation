package de.tigges.tchreservation.news.user.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface UserNewsRepository extends CrudRepository<UserNewsEntity, Long> {
    Stream<UserNewsEntity> findAllByIdUserId(long userId);

    Optional<UserNewsEntity> findByIdUserIdAndIdNewsId(long userId, long newsId);

    void deleteByIdNewsId(long newsId);
}
