package piecework.persistence;

import piecework.model.Entity;

/**
 * Interface for classes that provide access to data model objects. These
 * classes should be created by the {@see ModelProviderFactory}
 *
 * @author James Renfro
 */
public interface ModelProvider {

    Entity principal();

}
