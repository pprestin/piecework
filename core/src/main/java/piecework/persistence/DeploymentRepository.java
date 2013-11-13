package piecework.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import piecework.model.*;
import piecework.model.Process;

import java.util.List;

/**
 * @author James Renfro
 */
public interface DeploymentRepository extends MongoRepository<ProcessDeployment, String> {

}
