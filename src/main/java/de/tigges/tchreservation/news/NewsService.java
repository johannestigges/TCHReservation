package de.tigges.tchreservation.news;

import de.tigges.tchreservation.news.jpa.NewsRepository;
import de.tigges.tchreservation.news.model.News;
import de.tigges.tchreservation.news.user.jpa.UserNewsRepository;
import de.tigges.tchreservation.user.LoggedinUserService;
import de.tigges.tchreservation.user.model.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.tigges.tchreservation.JpaUtil.stream;
import static de.tigges.tchreservation.news.NewsMapper.map;

@RestController
@RequestMapping("/rest/news")
@RequiredArgsConstructor
@Slf4j
public class NewsService {
    private final LoggedinUserService loggedinUserService;
    private final NewsRepository newsRepository;
    private final UserNewsRepository userNewsRepository;

    @GetMapping("/all")
    public List<News> getAll() {
        return stream(newsRepository.findAllByOrderByCreatedAtDesc())
                .map(NewsMapper::map)
                .toList();
    }

    @GetMapping("/one/{id}")
    public News getOne(@PathVariable long id) {
        return newsRepository.findById(id)
                .map(NewsMapper::map)
                .orElseThrow();
    }

    @PostMapping("")
    public @ResponseBody News add(@RequestBody News news) {
        loggedinUserService.verifyHasRole(UserRole.ADMIN);
        return map(newsRepository.save(map(news)));
    }

    @PutMapping("")
    public @ResponseBody News update(@RequestBody News news) {
        loggedinUserService.verifyHasRole(UserRole.ADMIN);
        return newsRepository.findById(news.id())
                .map(db -> newsRepository.save(map(news)))
                .map(NewsMapper::map)
                .orElseThrow();
    }

    @DeleteMapping("/id/{newsId}")
    @Transactional
    public void deleteByNewsId(@PathVariable long newsId) {
        loggedinUserService.verifyHasRole(UserRole.ADMIN);
        userNewsRepository.deleteByIdNewsId(newsId);
        newsRepository.deleteById(newsId);
    }
}
