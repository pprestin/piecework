package piecework.process;

import org.springframework.data.mongodb.repository.MongoRepository;
import piecework.model.Attachment;
import piecework.model.FormRequest;

/**
 * @author James Renfro
 */
public interface AttachmentRepository extends MongoRepository<Attachment, String> {

}
