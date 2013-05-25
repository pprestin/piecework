package piecework.form.response;

import org.htmlcleaner.TagNode;

/**
 * @author James Renfro
 */
public interface TagDecorator {

    void decorate(TagNode tag);

    boolean canDecorate(String id, String cls, String name);

    boolean isReusable();

}
