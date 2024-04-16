package de.tigges.tchreservation.news.user;

import de.tigges.tchreservation.news.model.UserNews;
import de.tigges.tchreservation.news.user.jpa.UserNewsEntity;
import de.tigges.tchreservation.news.user.jpa.UserNewsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.tigges.tchreservation.JpaUtil.stream;

@RestController
@RequestMapping("/rest/news/user")
@RequiredArgsConstructor
public class UserNewsService {
    private final UserNewsRepository repository;

    @GetMapping("/{userid}")
    public List<UserNews> getNewsForUser(@PathVariable long userid) {
        return stream(repository.findAllByIdUserId(userid))
                .map(UserNewsMapper::map)
                .toList();
    }

    @PostMapping("/{userid}")
    @Transactional
    public void acknowledge(@PathVariable long userId, @RequestBody List<Long> newsIds) {
        for (Long newsId : newsIds) {
            repository.save(new UserNewsEntity(userId, newsId, true));
        }
    }
}
