package piecework.form.response;

import org.htmlcleaner.TagNode;

/**
 * @author James Renfro
 */
public interface TagDecorator {

    void decorate(TagNode tag, String id, String cls, String name);

    boolean canDecorate(String id, String cls, String name);

    boolean isReusable();

}
