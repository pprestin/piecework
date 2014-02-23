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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import piecework.command.AbstractCommand;
import piecework.command.CommandListener;
import piecework.exception.AbortCommandException;
import piecework.exception.BadRequestError;
import piecework.exception.PieceworkException;
import piecework.model.Process;
import piecework.common.ManyMap;
import piecework.persistence.ProcessProvider;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allows command or event listeners to be registered by being included in the application context
 * with @Service annotation on startup. During PostConstruct these are stuck into maps keyed by the
 * process definition key.
 *
 * @author James Renfro
 */
@Service
public class Mediator implements ApplicationContextAware, InitializingBean {

    private static final Logger LOG = Logger.getLogger(Mediator.class);

    @Autowired(required = false)
    Set<CommandListener> commandListeners;

    @Autowired(required = false)
    Set<EventListener> eventListeners;

    private ApplicationContext applicationContext;

    private ManyMap<String, CommandListener> commandListenerMap;

    private ManyMap<String, EventListener> eventListenerMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @PostConstruct
    public void init() {
        this.commandListenerMap = new ManyMap<String, CommandListener>();
        Map<String, CommandListener> commandListenersMap = this.applicationContext != null ? this.applicationContext.getBeansOfType(CommandListener.class) : null;
        Collection<CommandListener> commandListeners = commandListenersMap != null ? commandListenersMap.values() : this.commandListeners;
        if (commandListeners != null) {
            for (CommandListener listener : commandListeners) {
                commandListenerMap.putOne(listener.getProcessDefinitionKey(), listener);
            }
        }
        Map<String, EventListener> eventListenersMap = this.applicationContext != null ? this.applicationContext.getBeansOfType(EventListener.class) : null;
        Collection<EventListener> eventListeners = eventListenersMap != null ? eventListenersMap.values() : this.eventListeners;
        this.eventListenerMap = new ManyMap<String, EventListener>();
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                eventListenerMap.putOne(listener.getProcessDefinitionKey(), listener);
            }
        }
    }

    public <T, C extends AbstractCommand<T, P>, P extends ProcessProvider> C before(C command) throws PieceworkException {
        String processDefinitionKey = command.getProcessDefinitionKey();
        C updatedCommand = command;
        if (StringUtils.isNotEmpty(processDefinitionKey)) {
            List<CommandListener> listenerList = commandListenerMap.get(processDefinitionKey);
            if (listenerList != null) {
                BadRequestError badRequestError = null;
                for (CommandListener listener : listenerList) {
                    try {
                        C beforeCommand = updatedCommand;
                        updatedCommand = listener.before(beforeCommand);
                        if (updatedCommand == null)
                            updatedCommand = beforeCommand;
                    } catch (BadRequestError bre) {
                        badRequestError = bre;
                    } catch (AbortCommandException ace) {
                        LOG.info("Aborting command because listener told me to", ace);
                        updatedCommand = null;
                        break;
                    } catch (Exception e) {
                        LOG.error("Caught exception notifying eventListeners", e);
                    }
                }

                // If any of the listeners threw a bad request error, then rethrow it
                if (badRequestError != null)
                    throw badRequestError;
            }
        }

        return updatedCommand;
    }

    public void notify(StateChangeEvent event) {
        try {
            Process process = event.getInstanceProvider().process();
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
        } catch (PieceworkException pe) {
            LOG.error("Unable to retrieve process", pe);
        }
    }

}
