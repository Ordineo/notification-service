package be.ordina.ordineo.model.projection;

import be.ordina.ordineo.model.Message;
import org.springframework.data.rest.core.config.Projection;

/**
 * Created by PhDa on 12/05/2016.
 */
@Projection(name= "messageView", types = {Message.class})
public interface MessageView {

    String getMessage();

    String getSubscriber();

    String getMessageType();

    boolean getRead();
}
