package piecework.persistence;

import piecework.model.Entity;
import piecework.model.Process;
import piecework.model.ProcessDeployment;
import piecework.model.ProcessInstance;

/**
 * Interface for classes that provide access to data model objects. These
 * classes should be created by the {@see ModelProviderFactory}
 *
 * @author James Renfro
 */
public interface ModelProvider {

    Entity principal();

}
