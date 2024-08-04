package de.tigges.tchreservation.news;

import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.news.jpa.NewsRepository;
import de.tigges.tchreservation.news.model.News;
import de.tigges.tchreservation.news.user.jpa.UserNewsRepository;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.user.LoggedinUserService;
import de.tigges.tchreservation.user.model.UserRole;
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
                .orElseThrow(() -> new NotFoundException(EntityType.NEWS, id));
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
                .map(db -> map(newsRepository.save(map(news))))
                .orElseThrow(() -> new NotFoundException(EntityType.NEWS, news.id()));
    }

    @DeleteMapping("/id/{newsId}")
    public void deleteByNewsId(@PathVariable long newsId) {
        loggedinUserService.verifyHasRole(UserRole.ADMIN);
        newsRepository.findById(newsId).ifPresentOrElse(
                (id) -> deleteNews(newsId),
                () -> {
                    throw new NotFoundException(EntityType.NEWS, newsId);
                }
        );
    }

    private void deleteNews(long newsId) {
        userNewsRepository.deleteByIdNewsId(newsId);
        newsRepository.deleteById(newsId);
    }
}
