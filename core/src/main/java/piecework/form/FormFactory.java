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
package piecework.form;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import piecework.Constants;
import piecework.Versions;
import piecework.authorization.AuthorizationRole;
import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.exception.InternalServerError;
import piecework.exception.StatusCodeError;
import piecework.security.EncryptionService;
import piecework.service.IdentityService;
import piecework.validation.FormValidation;
import piecework.model.*;
import piecework.model.Process;
import piecework.identity.IdentityHelper;
import piecework.security.concrete.PassthroughSanitizer;
import piecework.util.ConstraintUtil;
import piecework.util.ManyMap;

import java.util.*;

/**
 * @author James Renfro
 */
@Service
public class FormFactory {

    private static final Logger LOG = Logger.getLogger(FormFactory.class);

    private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();

    @Autowired
    IdentityHelper helper;

    @Autowired
    EncryptionService encryptionService;

    @Autowired
    IdentityService identityService;

    @Autowired
    Versions versions;

    public Form form(FormRequest request, Process process, ProcessInstance instance, Task task, FormValidation validation) throws StatusCodeError {
        long start = 0;

        if (LOG.isDebugEnabled())
            start = System.currentTimeMillis();

        Screen screen = request.getScreen();
        String formInstanceId = request.getRequestId();

        if (screen == null)
            screen = screen(process, task);

        ViewContext version = versions.getVersion1();
        String loggedInUserId = helper.getAuthenticatedSystemOrUserId();
        User currentUser = helper.getCurrentUser();
        boolean hasOversight = helper.hasRole(process, AuthorizationRole.OVERSEER);

        FactoryWorker worker = new FactoryWorker(process, instance, task, screen, version, loggedInUserId, currentUser, hasOversight);
        ManyMap<String, Value> data = new ManyMap<String, Value>();
        ManyMap<String, Message> results = new ManyMap<String, Message>();

        if (instance != null) {
            data.putAll(encryptionService.decrypt(instance.getData()));
        }

        if (validation != null) {
            data.putAll(encryptionService.decrypt(validation.getData()));
            results.putAll(validation.getResults());
        }

        if (request.getMessages() != null) {
            results.putAll(request.getMessages());
        }

        Form form = worker.form(formInstanceId, data, results);

        if (LOG.isDebugEnabled())
            LOG.debug("Constructed form in " + (System.currentTimeMillis() - start) + " ms");
        return form;
    }

    private Screen screen(Process process, Task task) throws StatusCodeError {
        Interaction selectedInteraction = null;
        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
        List<Interaction> interactions = deployment.getInteractions();
        if (interactions != null && !interactions.isEmpty()) {
            for (Interaction interaction : interactions) {
                if (interaction == null)
                    continue;
                if ((task == null && interaction.getTaskDefinitionKeys().isEmpty()) ||
                    interaction.getTaskDefinitionKeys().contains(task.getTaskDefinitionKey())) {
                    selectedInteraction = interaction;
                    break;
                }
            }
        }

        if (selectedInteraction != null && selectedInteraction.getScreens() != null && selectedInteraction.getScreens().containsKey(ActionType.CREATE))
            return selectedInteraction.getScreens().get(ActionType.CREATE);

        throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);
    }

    public static Field getField(Process process, Screen screen, String fieldName) throws StatusCodeError  {
        if (process == null || screen == null || StringUtils.isEmpty(fieldName))
            return null;

        ProcessDeployment deployment = process.getDeployment();
        if (deployment == null)
            throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

        Map<String, Section> sectionMap = deployment.getSectionMap();
        List<Grouping> groupings = screen.getGroupings();

        for (Grouping grouping : groupings) {
            if (grouping == null)
                continue;
            List<String> sectionsIds = grouping.getSectionIds();
            if (sectionsIds == null)
                continue;

            for (String sectionId : sectionsIds) {
                Section section = sectionMap.get(sectionId);
                if (section == null)
                    continue;

                for (Field field : section.getFields()) {
                    if (field.getName() == null)
                        continue;

                    if (fieldName.equals(field.getName()))
                        return field;
                }
            }
        }
        return null;
    }

    private static void addConfirmationNumber(Field.Builder fieldBuilder, String confirmationNumber) {
        String defaultValue = fieldBuilder.getDefaultValue();
        fieldBuilder.defaultValue(defaultValue.replaceAll("\\{ConfirmationNumber\\}", confirmationNumber));
    }

//    public static void replaceCurrentUser(Field.Builder fieldBuilder, String currentLoggedInUserId) {
//        String defaultValue = fieldBuilder.getDefaultValue();
//        if (defaultValue.contains("\\{CurrentUser\\}"))
//            fieldBuilder.defaultValue(defaultValue.replaceAll("\\{CurrentUser\\}", currentLoggedInUserId));
//    }

    private static void addStateOptions(Field.Builder fieldBuilder) {
        fieldBuilder.option(new Option.Builder().value("").name("").build())
                .option(new Option.Builder().value("AL").name("Alabama").build())
                .option(new Option.Builder().value("AK").name("Alaska").build())
                .option(new Option.Builder().value("AZ").name("Arizona").build())
                .option(new Option.Builder().value("AR").name("Arkansas").build())
                .option(new Option.Builder().value("CA").name("California").build())
                .option(new Option.Builder().value("CO").name("Colorado").build())
                .option(new Option.Builder().value("CT").name("Connecticut").build())
                .option(new Option.Builder().value("DE").name("Delaware").build())
                .option(new Option.Builder().value("DC").name("District Of Columbia").build())
                .option(new Option.Builder().value("FL").name("Florida").build())
                .option(new Option.Builder().value("GA").name("Georgia").build())
                .option(new Option.Builder().value("HI").name("Hawaii").build())
                .option(new Option.Builder().value("ID").name("Idaho").build())
                .option(new Option.Builder().value("IL").name("Illinois").build())
                .option(new Option.Builder().value("IN").name("Indiana").build())
                .option(new Option.Builder().value("IA").name("Iowa").build())
                .option(new Option.Builder().value("KS").name("Kansas").build())
                .option(new Option.Builder().value("KY").name("Kentucky").build())
                .option(new Option.Builder().value("LA").name("Louisiana").build())
                .option(new Option.Builder().value("ME").name("Maine").build())
                .option(new Option.Builder().value("MD").name("Maryland").build())
                .option(new Option.Builder().value("MA").name("Massachusetts").build())
                .option(new Option.Builder().value("MI").name("Michigan").build())
                .option(new Option.Builder().value("MN").name("Minnesota").build())
                .option(new Option.Builder().value("MS").name("Mississippi").build())
                .option(new Option.Builder().value("MO").name("Missouri").build())
                .option(new Option.Builder().value("MT").name("Montana").build())
                .option(new Option.Builder().value("NE").name("Nebraska").build())
                .option(new Option.Builder().value("NV").name("Nevada").build())
                .option(new Option.Builder().value("NH").name("New Hampshire").build())
                .option(new Option.Builder().value("NJ").name("New Jersey").build())
                .option(new Option.Builder().value("NM").name("New Mexico").build())
                .option(new Option.Builder().value("NY").name("New York").build())
                .option(new Option.Builder().value("NC").name("North Carolina").build())
                .option(new Option.Builder().value("ND").name("North Dakota").build())
                .option(new Option.Builder().value("OH").name("Ohio").build())
                .option(new Option.Builder().value("OK").name("Oklahoma").build())
                .option(new Option.Builder().value("OR").name("Oregon").build())
                .option(new Option.Builder().value("PA").name("Pennsylvania").build())
                .option(new Option.Builder().value("RI").name("Rhode Island").build())
                .option(new Option.Builder().value("SC").name("South Carolina").build())
                .option(new Option.Builder().value("SD").name("South Dakota").build())
                .option(new Option.Builder().value("TN").name("Tennessee").build())
                .option(new Option.Builder().value("TX").name("Texas").build())
                .option(new Option.Builder().value("UT").name("Utah").build())
                .option(new Option.Builder().value("VT").name("Vermont").build())
                .option(new Option.Builder().value("VA").name("Virginia").build())
                .option(new Option.Builder().value("WA").name("Washington").build())
                .option(new Option.Builder().value("WV").name("West Virginia").build())
                .option(new Option.Builder().value("WI").name("Wisconsin").build())
                .option(new Option.Builder().value("WY").name("Wyoming").build());
    }


    public static final class FactoryWorker {

        private final Process process;
        private final String processDefinitionKey;
        private final String processInstanceId;
        private final Set<Attachment> attachments;
        private final int attachmentCount;
        private final Task task;
        private final PassthroughSanitizer passthroughSanitizer = new PassthroughSanitizer();
        private final Screen screen;
        private final ViewContext version;
        private final String loggedInUserId;
        private final User currentUser;
        private final boolean hasOversight;

        public FactoryWorker(Process process, ProcessInstance instance, Task task, Screen screen, ViewContext version, String loggedInUserId, User currentUser, boolean hasOversight) {
            this.process = process;
            this.processDefinitionKey = process != null ? process.getProcessDefinitionKey() : null;
            this.processInstanceId = instance != null ? instance.getProcessInstanceId() : null;
            this.attachments = instance != null ? instance.getAttachments() : Collections.<Attachment>emptySet();
            this.attachmentCount = instance != null && instance.getAttachmentIds() != null ? instance.getAttachmentIds().size() : 0;
            this.task = task;
            this.screen = screen;
            this.version = version;
            this.loggedInUserId = loggedInUserId;
            this.currentUser = currentUser;
            this.hasOversight = hasOversight;
        }

        public Form form(String formInstanceId, ManyMap<String, Value> data, ManyMap<String, Message> results) throws StatusCodeError {
            Form.Builder builder = new Form.Builder()
                    .formInstanceId(formInstanceId)
                    .processDefinitionKey(processDefinitionKey)
                    .taskSubresources(processDefinitionKey, task, version);

            if (hasOversight || task == null || task.getAssigneeId() == null || task.getAssigneeId().equals(loggedInUserId))
                builder.screen(screen(builder, screen, data, results));

            if (processInstanceId != null)
                builder.instanceSubresources(processDefinitionKey, processInstanceId, attachments, attachmentCount, this.version);

            return builder.build(version);
        }

        private Field field(Form.Builder formBuilder, Field field, ManyMap<String, Value> data, ManyMap<String, Message> results) {
            Field.Builder fieldBuilder = new Field.Builder(field, passthroughSanitizer)
                    .processDefinitionKey(processDefinitionKey)
                    .processInstanceId(processInstanceId);

            List<Constraint> constraints = field.getConstraints();
            if (constraints != null) {
                if (!ConstraintUtil.checkAll(Constants.ConstraintTypes.IS_ONLY_VISIBLE_WHEN, null, data, constraints))
                    fieldBuilder.invisible();
                if (ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_STATE, constraints))
                    addStateOptions(fieldBuilder);
                if (StringUtils.isNotEmpty(processInstanceId) && ConstraintUtil.hasConstraint(Constants.ConstraintTypes.IS_CONFIRMATION_NUMBER, constraints))
                    addConfirmationNumber(fieldBuilder, processInstanceId);
            }
            String fieldName = field.getName();
            String defaultValue = field.getDefaultValue();

            if (StringUtils.isNotEmpty(fieldName)) {
                List<Value> values = values(fieldName, data.get(fieldName), defaultValue);
                List<Message> messages = results.get(fieldName);

                formBuilder.variable(fieldName, values);
                formBuilder.validation(fieldName, messages);
            }

            return fieldBuilder.build(version);
        }

        private Screen screen(Form.Builder formBuilder, Screen screen, ManyMap<String, Value> data, ManyMap<String, Message> results) throws StatusCodeError {
            Screen.Builder screenBuilder = new Screen.Builder(screen, passthroughSanitizer, true);
            screenBuilder.clearSections();
            ProcessDeployment deployment = process.getDeployment();
            if (deployment == null)
                throw new InternalServerError(Constants.ExceptionCodes.process_is_misconfigured);

            Map<String, Section> sectionMap = new HashMap<String, Section>(deployment.getSectionMap());
            List<Grouping> groupings = screen.getGroupings();

            for (Grouping grouping : groupings) {
                if (grouping == null)
                    continue;
                List<String> sectionsIds = grouping.getSectionIds();
                if (sectionsIds == null)
                    continue;

                for (String sectionId : sectionsIds) {
                    Section section = sectionMap.remove(sectionId);
                    if (section == null)
                        continue;

                    screenBuilder.section(section(formBuilder, section, data, results));
                }
            }

            if (task != null) {
                if (!task.isActive())
                    screenBuilder.readonly();
                else if (task.getAssignee() != null && !task.getAssignee().getUserId().equals(loggedInUserId))
                    screenBuilder.readonly();
            }

            return screenBuilder.build();
        }

        private Section section(Form.Builder formBuilder, Section section, ManyMap<String, Value> data, ManyMap<String, Message> results) {

            Section.Builder sectionBuilder = new Section.Builder(section, passthroughSanitizer, false);

            for (Field field : section.getFields()) {
                sectionBuilder.field(field(formBuilder, field, data, results));
            }

            return sectionBuilder.build();
        }

        // Rebuilds any values of type File so that their links are correct
        private List<Value> values(String fieldName, List<? extends Value> values, String defaultValue) {
            if (values == null || values.isEmpty()) {
                if (StringUtils.isNotEmpty(defaultValue)) {
                    if (defaultValue.equals("{{CurrentUser}}"))
                        return Collections.singletonList((Value)currentUser);
                    if (defaultValue.equals("{{CurrentDate}}")) {
                        Value currentDateValue = new Value(dateTimeFormatter.print(new Date().getTime()));
                        return Collections.singletonList(currentDateValue);
                    }
                    return Collections.singletonList(new Value(defaultValue));
                }

                return Collections.emptyList();
            }

            List<Value> list = new ArrayList<Value>(values.size());
            for (Value value : values) {
                if (value instanceof File) {
                    File file = File.class.cast(value);

                    list.add(new File.Builder(file, passthroughSanitizer)
                                .processDefinitionKey(processDefinitionKey)
                                .processInstanceId(processInstanceId)
                                .fieldName(fieldName)
                                .build(version));
                } else {
                    list.add(value);
                }
            }

            return list;
        }
    }


}
