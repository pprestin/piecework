package piecework.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import piecework.model.CacheEvent;
import piecework.model.Form;

import java.util.List;

/**
 * @author James Renfro
 */
public interface CacheEventRepository extends MongoRepository<CacheEvent, String> {

    @Query(value="{ 'cacheAgentId' : { $ne : ?0 } }")
    List<CacheEvent> findAllOtherCacheAgentEvents(String cacheAgentId);

}
