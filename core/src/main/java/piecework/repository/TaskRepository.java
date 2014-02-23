package piecework.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import piecework.model.Task;

import java.util.List;

/**
 * @author James Renfro
 */
public interface TaskRepository extends MongoRepository<Task, String> {

    @Query(value="{ 'processDefinitionKey' : { $in: ?0 } }")
    List<Task> findByProcessDefinitionKeyIn(Iterable<String> processDefinitionKeys);

}
