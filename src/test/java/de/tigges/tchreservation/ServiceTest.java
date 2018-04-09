package de.tigges.tchreservation;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

public class ServiceTest {

	protected MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	protected MockMvc mockMvc;

	protected HttpMessageConverter<Object> mappingJackson2HttpMessageConverter;

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@SuppressWarnings("unchecked")
	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = (HttpMessageConverter<Object>) Arrays.asList(converters).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

		assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}
	
	@Before
	public void setupServiceTest() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();
	}
	
	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}

	protected <T> T getResponseJson(ResultActions resultActions, Class<T> c)
			throws HttpMessageNotReadableException, IOException {
		return jsonObject(resultActions.andReturn().getResponse().getContentAsByteArray(), c);
	}

	@SuppressWarnings("unchecked")
	protected <T> T jsonObject(byte[] content, Class<T> c) throws HttpMessageNotReadableException, IOException {
		MockHttpInputMessage mockHttpInputMessage = new MockHttpInputMessage(content);
		return (T) this.mappingJackson2HttpMessageConverter.read(c, mockHttpInputMessage);
	}
}
