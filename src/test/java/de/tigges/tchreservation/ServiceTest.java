package de.tigges.tchreservation;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class ServiceTest {

    private final MediaType contentTypeJson = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8);

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setupServiceTest() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    protected String json(Object o) {
        if (o != null) {
            return objectMapper.writeValueAsString(o);
        }
        return null;
    }

    protected <T> T getResponseJson(ResultActions resultActions, Class<T> c) {
        return objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), c);
    }

    public ResultActions performPost(String url, Object content) throws Exception {
        return mockMvc.perform(post(url).content(json(content)).contentType(contentTypeJson).with(csrf()));
    }

    public ResultActions performPut(String url) throws Exception {
        return mockMvc.perform(put(url).with(csrf()));
    }

    public ResultActions performPut(String url, Object content) throws Exception {
        return mockMvc.perform(put(url).content(json(content)).contentType(contentTypeJson).with(csrf()));
    }

    public ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url).with(csrf()));
    }

    public ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url).with(csrf()));
    }
}
