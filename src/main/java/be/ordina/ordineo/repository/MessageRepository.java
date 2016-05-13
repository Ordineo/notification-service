package be.ordina.ordineo.repository;

import be.ordina.ordineo.model.Message;
import be.ordina.ordineo.model.projection.MessageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

/**
 * Created by PhDa on 10/05/2016.
 */
@RepositoryRestResource(excerptProjection = MessageView.class)
public interface MessageRepository extends JpaRepository<Message, Long> {

    @RestResource(path="findBySubscriber",rel="findBySubscriber")
    @Query("select m from Message m where lower(m.subscriber) = lower(:username)")
    List<Message> findBySubscriber(@Param("username") String username);

}
