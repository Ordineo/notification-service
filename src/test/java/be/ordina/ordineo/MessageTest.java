package be.ordina.ordineo;

import be.ordina.ordineo.model.Message;
import be.ordina.ordineo.repository.MessageRepository;
import org.hibernate.validator.HibernateValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created by PhDa on 12/05/2016.
 */
@ContextConfiguration(classes=NotificationServiceApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest({"eureka.client.enabled:false"})
public class MessageTest {

    private LocalValidatorFactoryBean localValidatorFactory;
    private Set<ConstraintViolation<Message>> constraintViolations;
    private Message message;

    @Autowired
    MessageRepository messageRepository;

    @Before
    public void setup(){
        localValidatorFactory = new LocalValidatorFactoryBean();
        localValidatorFactory.setProviderClass(HibernateValidator.class);
        localValidatorFactory.afterPropertiesSet();

        message = createMessage();
    }

    private Message createMessage(){
        Message message = new Message();

        message.setSubscriber("PhDa");
        message.setMessage("TestMessage");
        message.setMessageType("milestone");

        return message;
    }

    @Test
    public void validateMessage() {
        constraintViolations = localValidatorFactory
                .validate(message);
        assertTrue(constraintViolations.stream().count() == 0);
    }

    @Test
    public  void subscriberIsNull(){
        message.setSubscriber(null);
        constraintViolations = localValidatorFactory
                .validate(message);
        assertTrue(constraintViolations.stream().filter(m -> m.getMessage().equals("may not be null")).count() > 0);
    }

    @Test
    public  void messageIsNull(){
        message.setMessage(null);
        constraintViolations = localValidatorFactory
                .validate(message);
        assertTrue(constraintViolations.stream().filter(m -> m.getMessage().equals("may not be null")).count() > 0);
    }

    @Test
    public  void messageTypeIsNull(){
        message.setMessageType(null);
        constraintViolations = localValidatorFactory
                .validate(message);
        assertTrue(constraintViolations.stream().filter(m -> m.getMessage().equals("may not be null")).count() > 0);
    }
}
