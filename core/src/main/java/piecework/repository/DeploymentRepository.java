package piecework.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import piecework.model.ProcessDeployment;

/**
 * @author James Renfro
 */
public interface DeploymentRepository extends MongoRepository<ProcessDeployment, String> {

}
