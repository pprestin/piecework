package piecework.form;

import piecework.model.Content;

import java.io.IOException;
import java.util.List;

/**
 * @author James Renfro
 */
public interface ContentRepository {

    Content findOne(String contentId);

    Content findByLocation(String location);

    List<Content> findByLocationPattern(String locationPattern) throws IOException;

    Content save(Content content);

}
