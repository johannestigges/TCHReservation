package de.tigges.tchreservation.news;

import de.tigges.tchreservation.news.jpa.NewsRepository;
import de.tigges.tchreservation.news.model.News;
import de.tigges.tchreservation.news.user.UserNewsSyncService;
import de.tigges.tchreservation.news.user.jpa.UserNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static de.tigges.tchreservation.JpaUtil.stream;
import static de.tigges.tchreservation.news.NewsMapper.map;

@RestController
@RequestMapping("/rest/news")
@RequiredArgsConstructor
@Slf4j
public class NewsService {
    private final UserNewsSyncService userNewsSyncService;
    private final NewsRepository newsRepository;
    private final UserNewsRepository userNewsRepository;

    @GetMapping("/all")
    public List<News> getAll() {
        return stream(newsRepository.findAllByOrderByCreatedAtDesc())
                .map(NewsMapper::map)
                .toList();
    }



    @PostMapping("")
    public @ResponseBody News add(@RequestBody News news) {
        var result = map(newsRepository.save(map(news)));
        userNewsSyncService.syncNews(result.id());
        return result;
    }

    @DeleteMapping("/days/{days}")
    public void deleteOldNews(@PathVariable int days) {
        deleteNewsOlderThan(LocalDateTime.now().minusDays(days));
    }

    @DeleteMapping("/news_id/{newsId}")
    public void deleteByNewsId(@PathVariable long newsId) {
        userNewsRepository.deleteByIdNewsId(newsId);
        newsRepository.deleteById(newsId);
    }

    @Async
    private void deleteNewsOlderThan(LocalDateTime expired) {
        var count = stream(newsRepository.findAllByCreatedAtBefore(expired))
                .peek(newsEntity -> deleteByNewsId(newsEntity.getId()))
                .count();
        log.info("deleted {} news older than {}", count, expired);
    }
}
