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
package piecework.engine;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Command;
import piecework.command.CommandListener;
import piecework.exception.AbortCommandException;
import piecework.model.Process;
import piecework.util.ManyMap;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

/**
 * Allows command or event listeners to be registered by being included in the application context
 * with @Service annotation on startup. During PostConstruct these are stuck into maps keyed by the
 * process definition key.
 *
 * @author James Renfro
 */
@Service
public class Mediator {

    private static final Logger LOG = Logger.getLogger(Mediator.class);

    @Autowired(required = false)
    Set<CommandListener> commandListeners;

    @Autowired(required = false)
    Set<EventListener> eventListeners;

    private ManyMap<String, CommandListener> commandListenerMap;

    private ManyMap<String, EventListener> eventListenerMap;

    @PostConstruct
    public void init() {
        this.commandListenerMap = new ManyMap<String, CommandListener>();
        if (commandListeners != null) {
            for (CommandListener listener : commandListeners) {
                commandListenerMap.putOne(listener.getProcessDefinitionKey(), listener);
            }
        }
        this.eventListenerMap = new ManyMap<String, EventListener>();
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                eventListenerMap.putOne(listener.getProcessDefinitionKey(), listener);
            }
        }
    }

    public <T> Command<T> before(piecework.Command<T> command) {
        String processDefinitionKey = command.getProcessDefinitionKey();
        piecework.Command<T> updatedCommand = command;
        if (StringUtils.isNotEmpty(processDefinitionKey)) {
            List<CommandListener> listenerList = commandListenerMap.get(processDefinitionKey);
            if (listenerList != null) {
                for (CommandListener listener : listenerList) {
                    try {
                        piecework.Command<T> beforeCommand = updatedCommand;
                        updatedCommand = listener.before(beforeCommand);
                        if (updatedCommand == null)
                            updatedCommand = beforeCommand;

                    } catch (AbortCommandException ace) {
                        LOG.info("Aborting command because listener told me to", ace);
                        updatedCommand = null;
                        break;
                    } catch (Exception e) {
                        LOG.error("Caught exception notifying eventListeners", e);
                    }
                }
            }
        }

        return updatedCommand;
    }

    public void notify(StateChangeEvent event) {
        Process process = event.getProcess();
        if (process != null) {
            String processDefinitionKey = process.getProcessDefinitionKey();
            if (StringUtils.isNotEmpty(processDefinitionKey)) {
                List<EventListener> listenerList = eventListenerMap.get(processDefinitionKey);
                if (listenerList != null) {
                    for (EventListener listener : listenerList) {
                        try {
                            listener.notify(event);
                        } catch (Exception e) {
                            LOG.error("Caught exception notifying eventListeners", e);
                        }
                    }
                }
            }
        }
    }

}
