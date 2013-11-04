/*
 * Copyright 2013 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package piecework.security;



import org.apache.commons.compress.utils.CountingInputStream;
import piecework.exception.MaxSizeExceededException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author James Renfro
 */
public class MaxSizeInputStream extends BufferedInputStream {

    private final long maxBytes;
    private long size;

    public MaxSizeInputStream(InputStream inputStream, long maxBytes) {
        super(inputStream);
        this.maxBytes = maxBytes;
    }

    public synchronized int read() throws IOException {
        int bytesRead = super.read();
        size += bytesRead;

        if (size > maxBytes)
            throw new MaxSizeExceededException(maxBytes);

        return bytesRead;
    }

    public synchronized int read(byte b[], int off, int len) throws IOException {
        int bytesRead = super.read(b, off, len);
        size += bytesRead;

        if (size > maxBytes)
            throw new MaxSizeExceededException(maxBytes);

        return bytesRead;
    }

}
