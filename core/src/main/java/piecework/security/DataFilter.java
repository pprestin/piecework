package piecework.security;

import piecework.model.Field;
import piecework.model.Value;

import java.util.List;

/**
 * @author James Renfro
 */
public interface DataFilter {

    List<Value> filter(String key, List<Value> values);

}
