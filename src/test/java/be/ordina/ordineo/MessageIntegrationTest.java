package be.ordina.ordineo;

import be.ordina.ordineo.model.Message;
import be.ordina.ordineo.repository.MessageRepository;
import be.ordina.ordineo.security.JwtFilter;
import be.ordina.ordineo.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by PhDa on 12/05/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NotificationServiceApplication.class)
@WebIntegrationTest({"eureka.client.enabled:false"})
public class MessageIntegrationTest {

    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @Autowired
    private MessageRepository messageRepository;

    private String authToken;

    @Autowired
    private ObjectMapper objectMapper;

    @Rule
    public RestDocumentation restDocumentation = new RestDocumentation("target/generated-snippets");
    @Autowired
    private WebApplicationContext wac;
    private RestDocumentationResultHandler document;

    @Before
    public void setup() throws Exception{
        this.document = document("{method-name}");
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(documentationConfiguration(this.restDocumentation).uris().withScheme("https")).alwaysDo(this.document)
                .addFilter(new JwtFilter(),"/*")
                .build();

        objectWriter = objectMapper.writer();
        authToken = TestUtil.getAuthToken();
        TestUtil.setAuthorities();
    }

    @Test
    public void getExistingMessage() throws Exception{
        mockMvc.perform(get("/api/messages/1")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriber", is("PhDa")))
                .andExpect(jsonPath("$.message", is("New message")))
                .andExpect(jsonPath("$.messageType", is("milestone")))
                .andExpect(jsonPath("$.messageDateTime", is("2011-12-03T10:15:30")))
                .andExpect(jsonPath("$.read", is(false)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/messages/1")))
                .andExpect(jsonPath("$._links.message.href", endsWith("/messages/1{?projection}")))
                .andDo(document("{method-name}", responseFields(
                        fieldWithPath("subscriber").description("The messages's subscriber"),
                        fieldWithPath("message").description("The actual message"),
                        fieldWithPath("messageType").description("The type of message"),
                        fieldWithPath("messageDateTime").description("The datetime the message was created"),
                        fieldWithPath("read").description("Whether or not the message has been read."),
                        fieldWithPath("_links").description("links to resources")
                )));
    }

    @Test
    public void getExistingMessageBySubscriber() throws Exception{
        mockMvc.perform(get("/api/messages/search/findBySubscriber?username=Phda")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.messages",hasSize(2)))
                .andExpect(jsonPath("$._embedded.messages[0].subscriber", is("PhDa")))
                .andExpect(jsonPath("$._embedded.messages[0].message", is("New message")))
                .andExpect(jsonPath("$._embedded.messages[0].messageType", is("milestone")))
                .andExpect(jsonPath("$._embedded.messages[0].read", is(false)))
                .andExpect(jsonPath("$._embedded.messages[0]._links.self.href", endsWith("/messages/1")))
                .andExpect(jsonPath("$._embedded.messages[0]._links.message.href", endsWith("/messages/1{?projection}")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/messages/search/findBySubscriber?username=Phda")))
                .andDo(document("{method-name}", responseFields(
                        fieldWithPath("._embedded.messages[].subscriber").description("The messages's subscriber"),
                        fieldWithPath("._embedded.messages[].message").description("The actual message"),
                        fieldWithPath("._embedded.messages[].messageType").description("The message's type"),
                        fieldWithPath("._embedded.messages[].read").description("Whether or not the message has been read."),
                        fieldWithPath("._embedded.messages[]._links").description("links to resources"),
                        fieldWithPath("._links").description("links to resources")
                )));
    }

    @Test
    public void getNonExistingMessageShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/messages/999")
                .header("Authorization", authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void list() throws Exception {
        mockMvc.perform(get("/api/messages")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.messages", hasSize(1)));
    }

    @Test
    public void postMessage() throws Exception {

        String string = "{\n" +
                "  \"message\": \"New Message\",\n" +
                "  \"subscriber\": \"PhDa\",\n" +
                "  \"messageType\": \"milestone\"\n" +
                "}";

        ConstrainedFields fields = new ConstrainedFields(Message.class);

        mockMvc.perform(post("/api/messages").content(string).contentType(MediaTypes.HAL_JSON)
                .header("Authorization", authToken))
                .andExpect(status().isCreated())
                .andDo(document("{method-name}", requestFields(
                        fields.withPath("subscriber").description("The message's subscriber"),
                        fields.withPath("messageType").description("The message's type"),
                        fields.withPath("message").description("The actual message")
                )))
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
    public void postMessageWithoutSubscriberShouldReturnBadRequest() throws Exception {

        Message message = new Message();
        message.setMessage("New Message");
        String string = objectWriter.writeValueAsString(message);

        ConstrainedFields fields = new ConstrainedFields(Message.class);

        mockMvc.perform(post("/api/messages").content(string).contentType(MediaTypes.HAL_JSON)
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
    public void postMessageWithoutMessageShouldReturnBadRequest() throws Exception {

        Message message = new Message();
        message.setSubscriber("PhDa");
        String string = objectWriter.writeValueAsString(message);

        ConstrainedFields fields = new ConstrainedFields(Message.class);

        mockMvc.perform(post("/api/messages").content(string).contentType(MediaTypes.HAL_JSON)
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
    public void updateMessage() throws Exception {
        Message message = messageRepository.findOne(1L);
        message.setMessage("New message");
        String string = objectWriter.writeValueAsString(message);

        mockMvc.perform(put("/api/messages/" +1).content(string).contentType(MediaTypes.HAL_JSON)
                .header("Authorization", authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateMessageWithNullValueShouldReturnBadRequest() throws Exception {
        Message message = messageRepository.findOne(1L);
        message.setMessage(null);
        String string = objectWriter.writeValueAsString(message);

        mockMvc.perform(put("/api/messages/" +message.getId()).content(string).contentType(MediaTypes.HAL_JSON)
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateMessageWithoutSubscriberShouldReturnBadRequest() throws Exception {
        Message message = messageRepository.findOne(1L);
        message.setSubscriber(null);
        String string = objectWriter.writeValueAsString(message);

        mockMvc.perform(put("/api/messages/" +message.getId()).content(string).contentType(APPLICATION_JSON)
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest());
    }


    private static class ConstrainedFields {
        private final ConstraintDescriptions constraintDescriptions;

        ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("constraints").value(StringUtils
                    .collectionToDelimitedString(this.constraintDescriptions
                            .descriptionsForProperty(path), ". ")));
        }
    }
}
