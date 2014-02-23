package piecework.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import piecework.model.Attachment;

/**
 * @author James Renfro
 */
public interface AttachmentRepository extends MongoRepository<Attachment, String> {

}
