package de.tigges.tchreservation.news;

import de.tigges.tchreservation.news.jpa.NewsRepository;
import de.tigges.tchreservation.news.model.News;
import de.tigges.tchreservation.news.user.jpa.UserNewsRepository;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.user.LoggedInUserService;
import de.tigges.tchreservation.user.model.UserRole;
import de.tigges.tchreservation.util.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.tigges.tchreservation.news.NewsMapper.map;
import static de.tigges.tchreservation.util.StreamUtil.stream;

@RestController
@RequestMapping("/rest/news")
@RequiredArgsConstructor
@Slf4j
public class NewsService {
    private final LoggedInUserService loggedinUserService;
    private final NewsValidator newsValidator;
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
                .orElseThrow(() -> notFoundException(id));
    }

    @PostMapping("")
    public @ResponseBody News add(@RequestBody News news) {
        loggedinUserService.verifyHasRole(UserRole.ADMIN);
        newsValidator.validate(news);
        return map(newsRepository.save(map(news)));
    }

    @PutMapping("")
    public @ResponseBody News update(@RequestBody News news) {
        loggedinUserService.verifyHasRole(UserRole.ADMIN);
        newsValidator.validate(news);
        return newsRepository.findById(news.id())
                .map(db -> map(newsRepository.save(map(news))))
                .orElseThrow(() -> notFoundException(news.id()));
    }

    @DeleteMapping("/id/{newsId}")
    @Transactional
    public void deleteByNewsId(@PathVariable long newsId) {
        loggedinUserService.verifyHasRole(UserRole.ADMIN);
        newsRepository.findById(newsId).ifPresentOrElse(
                (id) -> deleteNews(newsId),
                () -> {
                    throw notFoundException(newsId);
                }
        );
    }

    private NotFoundException notFoundException(Long newsId) {
        return new NotFoundException(newsValidator.validator.messageUtil, EntityType.NEWS, newsId);
    }

    private void deleteNews(long newsId) {
        userNewsRepository.deleteByIdNewsId(newsId);
        newsRepository.deleteById(newsId);
    }
}
