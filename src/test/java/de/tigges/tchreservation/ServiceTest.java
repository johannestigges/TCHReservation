package de.tigges.tchreservation;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class ServiceTest {

    private final MediaType contentTypeJson = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8);

    private MockMvc mockMvc;

    private HttpMessageConverter<Object> mappingJackson2HttpMessageConverter;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static HttpMessageConverter<Object> getMappingJackson2HttpMessageConverter(HttpMessageConverter<Object>[] converters) {
        return Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);
    }

    @Autowired
    private void setConverters(HttpMessageConverter<Object>[] converters) {
        mappingJackson2HttpMessageConverter = getMappingJackson2HttpMessageConverter(converters);
        assertThat(mappingJackson2HttpMessageConverter).isNotNull();
    }

    @BeforeEach
    public void setupServiceTest() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    protected String json(Object o) throws IOException {
        if (o != null) {
            var mockHttpOutputMessage = new MockHttpOutputMessage();
            this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
            return mockHttpOutputMessage.getBodyAsString();
        }
        return null;
    }

    protected <T> T getResponseJson(ResultActions resultActions, Class<T> c)
            throws HttpMessageNotReadableException, IOException {
        return jsonObject(resultActions.andReturn().getResponse().getContentAsByteArray(), c);
    }

    @SuppressWarnings("unchecked")
    protected <T> T jsonObject(byte[] content, Class<T> c) throws HttpMessageNotReadableException, IOException {
        var mockHttpInputMessage = new MockHttpInputMessage(content);
        return (T) this.mappingJackson2HttpMessageConverter.read(c, mockHttpInputMessage);
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
