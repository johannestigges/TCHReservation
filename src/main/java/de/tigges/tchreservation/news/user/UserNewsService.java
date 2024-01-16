package de.tigges.tchreservation.news.user;

import de.tigges.tchreservation.news.user.jpa.UserNewsRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public class UserNewsService {
    private final UserNewsRepository repository;

    public List<UserNews> getNewsForUser(long userid) {
        return StreamSupport.stream(repository.findAllByUserId(userid).spliterator(), false)
                .map(UserNewsMapper::map).toList();
    }
}
