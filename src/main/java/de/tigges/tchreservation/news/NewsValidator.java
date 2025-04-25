package de.tigges.tchreservation.news;

import de.tigges.tchreservation.exception.InvalidDataException;
import de.tigges.tchreservation.news.model.News;
import de.tigges.tchreservation.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsValidator {
    public final Validator validator;

    public  void validate(News news) throws InvalidDataException {
        validator.clearErrorMessages();
        validator.checkNotEmpty("subject",news.subject());
        validator.checkNotEmpty("text", news.text());
        validator.checkErrorMessages();
    }
}
