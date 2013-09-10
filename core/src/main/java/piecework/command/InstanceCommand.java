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

import piecework.Command;
import piecework.model.Attachment;
import piecework.model.Process;
import piecework.model.ProcessInstance;
import piecework.model.Submission;
import piecework.model.Value;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public abstract class InstanceCommand implements Command<ProcessInstance> {

    protected final Process process;
    protected final ProcessInstance instance;

    protected List<Attachment> attachments;
    protected Map<String, List<Value>> data;
    protected String label;
    protected Submission submission;

    public InstanceCommand(Process process) {
        this(process, null);
    }

    public InstanceCommand(Process process, ProcessInstance instance) {
        this.process = process;
        this.instance = instance;
    }

    public InstanceCommand attachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public InstanceCommand data(Map<String, List<Value>> data) {
        this.data = data;
        return this;
    }

    public InstanceCommand label(String label) {
        this.label = label;
        return this;
    }

    public InstanceCommand submission(Submission submission) {
        this.submission = submission;
        return this;
    }

}
