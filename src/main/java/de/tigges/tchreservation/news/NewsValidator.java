package de.tigges.tchreservation.news;

import de.tigges.tchreservation.exception.InvalidDataException;
import de.tigges.tchreservation.news.model.News;
import de.tigges.tchreservation.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class NewsValidator {
    public final Validator validator;

    public  void validate(News news) throws InvalidDataException {
        validator.startValidation();
        if (!StringUtils.hasText(news.subject())) {
            validator.addFieldErrorMessage("subject","error_null_not_allowed");
        }
        if (!StringUtils.hasText(news.text())) {
            validator.addFieldErrorMessage("text", "error_null_not_allowed");
        }
        validator.checkErrorMessages();
    }
}
