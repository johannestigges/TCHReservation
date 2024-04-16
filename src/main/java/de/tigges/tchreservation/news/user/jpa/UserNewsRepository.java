package de.tigges.tchreservation.news.user.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserNewsRepository extends CrudRepository<UserNewsEntity, Long> {
    Iterable<UserNewsEntity> findAllByIdUserId(long userId);

    Optional<UserNewsEntity> findByIdUserIdAndIdNewsId(long userId, long newsId);

    void deleteByIdNewsId(long newsId);
}
