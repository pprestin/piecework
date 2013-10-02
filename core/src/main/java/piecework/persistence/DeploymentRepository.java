package piecework.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import piecework.model.*;
import piecework.model.Process;

/**
 * @author James Renfro
 */
public interface DeploymentRepository extends MongoRepository<ProcessDeployment, String> {

}
