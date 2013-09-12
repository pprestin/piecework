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
package piecework.command;

import com.mongodb.DBRef;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import piecework.Constants;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.model.*;
import piecework.model.Process;
import piecework.persistence.ProcessInstanceRepository;
import piecework.CommandExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
public class UpdateInstanceCommand extends InstanceCommand {

    private static final Logger LOG = Logger.getLogger(UpdateInstanceCommand.class);

    public UpdateInstanceCommand(Process process, ProcessInstance instance) {
        super(process, instance);
    }

    @Override
    public ProcessInstance execute(CommandExecutor commandExecutor) throws StatusCodeError {

        if (LOG.isDebugEnabled())
            LOG.debug("Executing update instance command " + this.toString());

        ProcessInstance instance = this.instance;

        if (instance == null)
            throw new InternalServerError();

        Environment environment = commandExecutor.getEnvironment();
        MongoTemplate operations = commandExecutor.getMongoOperations();
        ProcessInstanceRepository repository = commandExecutor.getProcessInstanceRepository();

        boolean skipOptimization = environment.getProperty(Constants.Settings.OPTIMIZATIONS_OFF, Boolean.class, Boolean.FALSE);

        if (skipOptimization)
            instance = save(repository);
        else
            instance = update(operations);

        if (LOG.isDebugEnabled())
            LOG.debug("Executed update instance command " + this.toString());

        return instance;
    }

    public String toString() {
        String processDefinitionKey = process != null ? process.getProcessDefinitionKey() : "";
        String processInstanceId = instance != null ? instance.getProcessInstanceId() : "";

        return "{ processDefinitionKey: \"" + processDefinitionKey + "\", processInstanceId: \"" + processInstanceId + "\" }";
    }


    protected ProcessInstance save(ProcessInstanceRepository processInstanceRepository) {
        ProcessInstance instance = processInstanceRepository.findOne(this.instance.getProcessInstanceId());
        ProcessInstance.Builder builder = new ProcessInstance.Builder(instance)
                .attachments(attachments)
                .data(data)
                .submission(submission);

        if (StringUtils.isNotEmpty(label))
            builder.processInstanceLabel(label);

        return processInstanceRepository.save(builder.build());
    }

    protected ProcessInstance update(MongoTemplate operations) {
        Query query = new Query(where("_id").is(instance.getProcessInstanceId()));
        Update update = new Update();

        attachments(update, operations);
        data(update);
        label(update);
        submission(update, operations);

        return operations.findAndModify(query, update, ProcessInstance.class);
    }

    private void attachments(Update update, MongoTemplate operations) {
        if (attachments != null && !attachments.isEmpty()) {
            DBRef[] attachmentRefs = new DBRef[attachments.size()];
            for (int i=0;i<attachments.size();i++) {
                attachmentRefs[i] = new DBRef(operations.getDb(), operations.getCollectionName(Attachment.class), new ObjectId(attachments.get(i).getAttachmentId()));
            }
            update.pushAll("attachments", attachmentRefs);
        }
    }

    private void data(Update update) {
        if (data != null && !data.isEmpty()) {
            for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
                String key = "data." + entry.getKey();
                List<Value> values = entry.getValue();

                List<File> files = null;
                List<User> users = null;
                if (values != null) {
                    for (Value value : values) {
                        if (value instanceof File) {
                            File file = File.class.cast(value);

                            if (StringUtils.isNotEmpty(file.getName())) {
                                update.addToSet("keywords", file.getName().toLowerCase());
                                if (files == null)
                                    files = new ArrayList<File>(values.size());
                                files.add(file);
                            }
                        } else if (value instanceof User) {
                            User user = User.class.cast(value);
                            if (user != null) {
                                if (user.getDisplayName() != null)
                                    update.addToSet("keywords", user.getDisplayName());
                                if (user.getVisibleId() != null)
                                    update.addToSet("keywords", user.getVisibleId());
                                if (user.getUserId() != null)
                                    update.addToSet("keywords", user.getUserId());
                                if (user.getEmailAddress() != null)
                                    update.addToSet("keywords", user.getEmailAddress());

                                if (users == null)
                                    users = new ArrayList<User>(values.size());
                                users.add(user);
                            }
                        } else if (! (value instanceof Secret)) {
                            if (StringUtils.isNotEmpty(value.getValue()))
                                update.addToSet("keywords", value.getValue().toLowerCase());
                        }
                    }
                }
                if (files != null)
                    update.set(key, files);
                else if (users != null)
                    update.set(key, users);
                else
                    update.set(key, values);
            }
        }
    }

    private void label(Update update) {
        if (StringUtils.isNotEmpty(label))
            update.set("processInstanceLabel", label);
    }

    private void submission(Update update, MongoTemplate operations) {
        if (submission != null)
            update.push("submissions", new DBRef(operations.getDb(), operations.getCollectionName(Submission.class), new ObjectId(submission.getSubmissionId())));
    }

}
