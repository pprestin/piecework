/*
 * Code copied from org.activiti.engine.impl.persistence.StrongUuidGenerator.java
 * in Activiti 5.13, licensed under the Apache 2.0 License.
 *
 * @author Daniel Meyer
 */
package piecework.common;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import org.springframework.stereotype.Service;

/**
 * Copy of Activiti's StrongUuidGenerator, pulled into core for consistency, since it will be
 * used outside of the engine code.
 */
@Service
public class UuidGenerator {

    protected static TimeBasedGenerator timeBasedGenerator;

    public UuidGenerator() {
        ensureGeneratorInitialized();
    }

    protected void ensureGeneratorInitialized() {
        if (timeBasedGenerator == null) {
            synchronized (UuidGenerator.class) {
                if (timeBasedGenerator == null) {
                    timeBasedGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());
                }
            }
        }
    }

    public String getNextId() {
        return timeBasedGenerator.generate().toString();
    }

}
