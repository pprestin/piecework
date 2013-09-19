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
package piecework.persistence.concrete;

import com.mongodb.DBRef;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Service;
import piecework.common.ViewContext;
import piecework.model.*;
import piecework.persistence.custom.ProcessInstanceRepositoryCustom;
import piecework.process.ProcessInstanceQueryBuilder;
import piecework.process.ProcessInstanceSearchCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
@Service
@NoRepositoryBean
public class ProcessInstanceRepositoryCustomImpl implements ProcessInstanceRepositoryCustom {

    private static final Logger LOG = Logger.getLogger(ProcessInstanceRepositoryCustomImpl.class);

    @Autowired
    MongoTemplate mongoOperations;

    @Override
    public Page<ProcessInstance> findByCriteria(ProcessInstanceSearchCriteria criteria, Pageable pageable) {
        long start = 0;
        if (LOG.isDebugEnabled())
            start = System.currentTimeMillis();

        SearchResults.Builder resultsBuilder = new SearchResults.Builder();
        // Otherwise, look up all instances that match the query
        Query query = new ProcessInstanceQueryBuilder(criteria).build();
        query.skip(pageable.getOffset());
        query.limit(pageable.getPageSize());

        // Don't include form data in the result
        org.springframework.data.mongodb.core.query.Field field = query.fields();
        field.exclude("data");

        List<ProcessInstance> processInstances = mongoOperations.find(query, ProcessInstance.class);

        Page<ProcessInstance> page;

        if (criteria.getMaxResults() != null || criteria.getFirstResult() != null) {
            long total = mongoOperations.count(query, ProcessInstance.class);
            page = new PageImpl<ProcessInstance>(processInstances, pageable, total);
        } else {
            page = new PageImpl<ProcessInstance>(processInstances);
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieved instances by criteria in " + (System.currentTimeMillis() - start) + " ms");

        return page;
    }


    @Override
    public ProcessInstance update(String id, String label, Map<String, List<Value>> data, List<Attachment> attachments, Submission submission) {
        return updateSimply(id, label, data, attachments, submission);
    }

    private ProcessInstance updateEfficiently(String id, String label, Map<String, List<Value>> data, List<Attachment> attachments, Submission submission) {
        Query query = new Query(where("_id").is(id));
        Update update = new Update();

        include(update, attachments);
        include(update, data);
        include(update, label);
        include(update, submission);

        return mongoOperations.findAndModify(query, update, ProcessInstance.class);

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

    private static void include(Update update, Map<String, List<Value>> data) {
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

    private static void include(Update update, String label) {
        if (StringUtils.isNotEmpty(label))
            update.set("processInstanceLabel", label);
    }

    private static void include(Update update, Submission submission) {
        if (submission != null)
            update.push("submissions", submission.getSubmissionId());
    }

}
