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
package piecework.repository.concrete;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoTypeMapper;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.model.*;
import piecework.process.ProcessInstanceQueryBuilder;
import piecework.process.ProcessInstanceSearchCriteria;
import piecework.repository.custom.ProcessInstanceRepositoryCustom;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
@Service
@NoRepositoryBean
public class ProcessInstanceRepositoryCustomImpl implements ProcessInstanceRepositoryCustom {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceRepositoryCustomImpl.class);

    private static final FindAndModifyOptions OPTIONS = new FindAndModifyOptions().returnNew(true);

    @Autowired
    @Qualifier(value="mongoTemplate")
    MongoTemplate mongoOperations;

    @Override
    public Page<ProcessInstance> findByCriteria(ProcessInstanceSearchCriteria criteria, Pageable pageable) {
        // Otherwise, look up all instances that match the query
        Query query = new ProcessInstanceQueryBuilder(criteria).build();
        query.skip(pageable.getOffset());
        query.limit(pageable.getPageSize());

        org.springframework.data.mongodb.core.query.Field field = query.fields();

        // Don't include form data in the result unless it's requested
        if (! criteria.isIncludeVariables())
            field.exclude("data");

        return findByQuery(query, pageable);
    }

    @Override
    public Page<ProcessInstance> findByQuery(Query query, Pageable request) {
        long start = 0;
        if (LOG.isDebugEnabled())
            start = System.currentTimeMillis();

        List<ProcessInstance> processInstances = mongoOperations.find(query, ProcessInstance.class);

        long total = 0;

        // We only need to look up a total if we're not on the first page or the page is full
        if (query.getSkip() > 0 || processInstances.size() == query.getLimit())
            total = mongoOperations.count(query, ProcessInstance.class);
        else
            total = processInstances.size();

        Page<ProcessInstance> page = new PageImpl<ProcessInstance>(processInstances, request, total);
        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved instances by criteria in " + (System.currentTimeMillis() - start) + " ms");

        return page;
    }

    @Override
    public ProcessInstance findByTaskId(String processDefinitionKey, String taskId) {
        Query query = new Query(where("tasks." + taskId).exists(true).and("processDefinitionKey").is(processDefinitionKey));
        return mongoOperations.findOne(query, ProcessInstance.class);
    }

    @Override
    public boolean update(String id, String engineProcessInstanceId) {
        WriteResult result = mongoOperations.updateFirst(new Query(where("_id").is(id)),
                new Update().set("engineProcessInstanceId", engineProcessInstanceId),
                ProcessInstance.class);
        String error = result.getError();
        if (StringUtils.isNotEmpty(error)) {
            LOG.error("Unable to correctly save engineProcessInstanceId " + engineProcessInstanceId + " for " + id + ": " + error);
            return false;
        }
        return true;
    }

    @Override
    public ProcessInstance update(String id, String label, Map<String, List<Value>> data, Map<String, List<Message>> messages, List<Attachment> attachments, Submission submission, String applicationStatusExplanation) {
        return updateEfficiently(id, label, data, messages, attachments, submission, applicationStatusExplanation);
    }

    @Override
    public ProcessInstance update(String id, Operation operation, String applicationStatus, String applicationStatusExplanation, String processStatus, Set<Task> tasks) {
        Query query = new Query(where("_id").is(id));
        Update update = new Update();

        if (applicationStatus != null)
            update.set("applicationStatus", applicationStatus);
        if (applicationStatusExplanation != null)
            update.set("applicationStatusExplanation", applicationStatusExplanation);
        if (processStatus != null)
            update.set("processStatus", processStatus);

        if (tasks != null) {
            for (Task task : tasks) {
                update.set("tasks." + task.getTaskInstanceId(), task);
            }
        }

        update.push("operations", operation);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);

        return mongoOperations.findAndModify(query, update, options, ProcessInstance.class);
    }

    @Override
    public boolean update(String id, Task task) {
        Query query =  new Query();
        query.addCriteria(where("processInstanceId").is(id));

        Update update = new Update();
        update.set("tasks." + task.getTaskInstanceId(), task);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        ProcessInstance stored = mongoOperations.findAndModify(query, update, options, ProcessInstance.class);

        return true;
    }

    @Override
    public ProcessInstance update(String id, String processStatus, String applicationStatus, Map<String, List<Value>> data) {
        Update update = new Update().set("endTime", new Date())
                .set("applicationStatus", applicationStatus)
                .set("processStatus", Constants.ProcessStatuses.COMPLETE);
        include(update, data);
        return mongoOperations.findAndModify(new Query(where("_id").is(id)),
                update,
                OPTIONS,
                ProcessInstance.class);
    }

    private ProcessInstance updateEfficiently(String id, String label, Map<String, List<Value>> data, Map<String, List<Message>> messages, List<Attachment> attachments, Submission submission, String applicationStatusExplanation) {
        Query query = new Query(where("_id").is(id));
        Update update = new Update();

        if (applicationStatusExplanation != null)
            update.set("applicationStatusExplanation", applicationStatusExplanation);

        include(update, attachments);
        include(update, data);
        include(update, label);
        include(update, submission);
        includeMessages(update, messages);

        return mongoOperations.findAndModify(query, update, OPTIONS, ProcessInstance.class);
    }

    private ProcessInstance updateSimply(String id, String label, Map<String, List<Value>> data, List<Attachment> attachments, Submission submission) {
        long start = 0;
        if (LOG.isDebugEnabled())
            start = System.currentTimeMillis();

        ProcessInstance instance = mongoOperations.findOne(Query.query(Criteria.where("_id").is(id)), ProcessInstance.class);
        ProcessInstance.Builder builder = new ProcessInstance.Builder(instance)
                .attachments(attachments)
                .data(data)
                .submission(submission);

        if (StringUtils.isNotEmpty(label))
            builder.processInstanceLabel(label);

        ProcessInstance entity = builder.build();
        mongoOperations.save(entity);

        if (LOG.isDebugEnabled())
            LOG.debug("Updated process instance " + id + " in " + (System.currentTimeMillis() - start) + " ms");

        return entity;
    }

    private static void include(Update update, List<Attachment> attachments) {
        if (attachments != null && !attachments.isEmpty()) {
            Object[] attachmentIds = new Object[attachments.size()];
            int count = 0;
            for (Attachment attachment : attachments) {
                attachmentIds[count++] = attachment.getAttachmentId();
            }
            update.pushAll("attachmentIds", attachmentIds);
        }
    }

    private void include(Update update, Map<String, List<Value>> data) {
        if (data != null && !data.isEmpty()) {
            MongoConverter converter = mongoOperations.getConverter();
            MongoTypeMapper typeMapper = converter.getTypeMapper();

            Set<String> keywords = new HashSet<String>();
            for (Map.Entry<String, List<Value>> entry : data.entrySet()) {
                String key = "data." + entry.getKey();
                List<Value> values = entry.getValue();
                List<Object> dbObjects = new ArrayList<Object>();

                for (Value value : values) {
                    if (value != null) {
                        Object dbObject = converter.convertToMongoType(value);
                        Class<?> clz = null;
                        if (value instanceof File)
                            clz = File.class;
                        else if (value instanceof User)
                            clz = User.class;
                        else if (value instanceof Secret)
                            clz = Secret.class;
                        else {
                            String strValue = value.getValue();
                            if (StringUtils.isNotEmpty(strValue)) {
                                if (strValue.contains("-")) {
                                    keywords.add(strValue.replaceAll("-", "").toLowerCase());
                                }
                                keywords.add(strValue.toLowerCase());
                            }
                        }
                        if (clz != null) {
                            typeMapper.writeType(clz, DBObject.class.cast(dbObject));
                        }
                        dbObjects.add(dbObject);
                    }
                }

                update.set(key, dbObjects);
            }
            if (!keywords.isEmpty()) {
                BasicDBList eachList = new BasicDBList();
                for (String keyword : keywords) {
                    eachList.add(keyword);
                }
                update.addToSet(
                        "keywords",
                        BasicDBObjectBuilder.start("$each", eachList).get()
                );
            }

        }
    }

    private void includeMessages(Update update, Map<String, List<Message>> messages) {
        if (messages != null) {
            if (messages.isEmpty()) {
                update.unset("messages");
            } else {
                update.set("messages", messages);
//                MongoConverter converter = mongoOperations.getConverter();
//                for (Map.Entry<String, List<Message>> entry : messages.entrySet()) {
//                    String key = "messages." + entry.getKey();
//                    List<Message> values = entry.getValue();
//                    List<Object> dbObjects = new ArrayList<Object>();
//
//                    for (Message value : values) {
//                        if (value != null) {
//                            Object dbObject = converter.convertToMongoType(value);
//                            dbObjects.add(dbObject);
//                        }
//                    }
//
//                    update.set(key, dbObjects);
//                }
            }
        }
    }

    private static void include(Update update, String label) {
        if (StringUtils.isNotEmpty(label))
            update.set("processInstanceLabel", label);
    }

    private static void include(Update update, Submission submission) {
        if (submission != null)
            update.push("submissions", submission.getSubmissionId());
    }

}
