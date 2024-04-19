package de.tigges.tchreservation.news.user;

import de.tigges.tchreservation.news.jpa.NewsRepository;
import de.tigges.tchreservation.news.user.jpa.UserNewsEntity;
import de.tigges.tchreservation.news.user.jpa.UserNewsRepository;
import de.tigges.tchreservation.user.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static de.tigges.tchreservation.JpaUtil.stream;

@Service
@RequiredArgsConstructor
public class UserNewsSyncService {
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;
    private final UserNewsRepository userNewsRepository;

    @Async
    public void syncNews(long newsId) {
        stream(userRepository.findAll())
                .forEach(userEntity -> addUserNewsIfNotExisting(userEntity.getId(), newsId));
    }

    @Async
    public void syncUser(long userId) {
        stream(newsRepository.findAll())
                .forEach(newsEntity -> addUserNewsIfNotExisting(userId, newsEntity.getId()));
    }

    @Async
    public void sync() {
        stream(newsRepository.findAll())
                .forEach(newsEntity -> syncNews(newsEntity.getId()));
    }

    private void addUserNewsIfNotExisting(long userId, long newsId) {
        userNewsRepository.findByIdUserIdAndIdNewsId(userId, newsId)
                .orElseGet(() -> addUserNews(userId, newsId));
    }

    private UserNewsEntity addUserNews(long userId, long newsId) {
        return userNewsRepository.save(new UserNewsEntity(userId, newsId, false));
    }
}
