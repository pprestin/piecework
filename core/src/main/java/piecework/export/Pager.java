package piecework.export;

import org.springframework.data.domain.Page;
import piecework.model.ProcessInstance;

/**
 * @author James Renfro
 */
public interface Pager<T> {

    Page<T> nextPage();

    boolean hasNext();

    void reset();

}
