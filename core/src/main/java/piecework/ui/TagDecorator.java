package piecework.ui;

import org.htmlcleaner.TagNode;

/**
 * @author James Renfro
 */
public interface TagDecorator {

    void decorate(TagNode tag, String id, String cls, String name, String variable);

    boolean canDecorate(String id, String cls, String name, String variable);

    boolean isReusable();

}
