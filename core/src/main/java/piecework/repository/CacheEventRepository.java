package piecework.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import piecework.model.CacheEvent;

import java.util.List;

/**
 * @author James Renfro
 */
public interface CacheEventRepository extends MongoRepository<CacheEvent, String> {

    @Query(value="{ 'cacheAgentId' : { $ne : ?0 } }")
    List<CacheEvent> findAllOtherCacheAgentEvents(String cacheAgentId);

}
