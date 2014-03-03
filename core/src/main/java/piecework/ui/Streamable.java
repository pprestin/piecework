package piecework.ui;

import java.io.InputStream;

/**
 * @author James Renfro
 */
public interface Streamable {

    InputStream getInputStream();

    String getContentType();

    String getName();

}
