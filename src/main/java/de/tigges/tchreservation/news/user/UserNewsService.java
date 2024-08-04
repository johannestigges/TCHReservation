package de.tigges.tchreservation.news.user;

import de.tigges.tchreservation.news.model.UserNews;
import de.tigges.tchreservation.news.user.jpa.UserNewsEntity;
import de.tigges.tchreservation.news.user.jpa.UserNewsRepository;
import de.tigges.tchreservation.user.LoggedinUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.tigges.tchreservation.JpaUtil.stream;

@RestController
@RequestMapping("/rest/news/user")
@RequiredArgsConstructor
public class UserNewsService {
    private final UserNewsRepository userNewsRepository;
    private final LoggedinUserService loggedinUserService;

    @GetMapping("")
    public List<UserNews> getMyNews() {
        var user = loggedinUserService.verifyIsLoggedIn();
        return stream(userNewsRepository.findAllByIdUserId(user.getId()))
                .map(UserNewsMapper::map)
                .toList();
    }

    @PostMapping("/acknowledge")
    @Transactional
    public void acknowledge(@RequestBody List<Long> newsIds) {
        var user = loggedinUserService.verifyIsLoggedIn();
        for(var newsId: newsIds) {
            userNewsRepository.save(new UserNewsEntity(user.getId(), newsId, true));
        }
    }
}
