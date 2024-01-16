package de.tigges.tchreservation.news;

import de.tigges.tchreservation.news.jpa.NewsRepository;
import de.tigges.tchreservation.news.model.News;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/rest/news")
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    @GetMapping("/all")
    public List<News> getAll() {
        return StreamSupport
                .stream(newsRepository.findAllByOrderByCreatedAtDesc().spliterator(), false)
                .map(NewsMapper::map)
                .toList();
    }

    @PostMapping("/")
    public @ResponseBody News add(@RequestBody News news) {
        return NewsMapper.map(newsRepository.save(NewsMapper.map(news)));
    }

    @DeleteMapping("/{days}")
    public void deleteOldNews(int days) {
        var expiry = LocalDateTime.now().minusDays(days);
        newsRepository.deleteByCreatedAtBefore(expiry);
    }
}
