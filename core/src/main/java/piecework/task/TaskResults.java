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
package piecework.task;

import piecework.model.Task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author James Renfro
 */
public class TaskResults {
    private final List<Task> tasks;
    private final Set<String> engineProcessInstanceIds;
    private final int firstResult;
    private final int maxResults;
    private final long total;

    private TaskResults() {
        this(new Builder());
    }

    private TaskResults(Builder builder) {
        this.tasks = builder.tasks;
        this.engineProcessInstanceIds = builder.engineProcessInstanceIds;
        this.firstResult = builder.firstResult;
        this.maxResults = builder.maxResults;
        this.total = builder.total;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public Set<String> getEngineProcessInstanceIds() {
        return engineProcessInstanceIds;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public long getTotal() {
        return total;
    }

    public final static class Builder {
        private List<Task> tasks;
        private Set<String> engineProcessInstanceIds;
        private int firstResult;
        private int maxResults;
        private long total;

        public Builder() {
            this.tasks = new ArrayList<Task>();
            this.engineProcessInstanceIds = new HashSet<String>();
        }

        public Builder(TaskResults results) {
            this.tasks = new ArrayList<Task>();
            if (results.tasks != null)
                this.tasks.addAll(results.tasks);

            this.engineProcessInstanceIds = new HashSet<String>();
            if (results.engineProcessInstanceIds != null)
                this.engineProcessInstanceIds.addAll(results.engineProcessInstanceIds);

            this.total = results.total;
            this.firstResult = results.firstResult;
            this.maxResults = results.maxResults;
        }

        public TaskResults build() {
            return new TaskResults(this);
        }

        public Builder task(Task task) {
            this.tasks.add(task);
            return this;
        }

        public Builder tasks(List<Task> tasks) {
            this.tasks.addAll(tasks);
            return this;
        }

        public Builder engineProcessInstanceId(String engineProcessInstanceId) {
            this.engineProcessInstanceIds.add(engineProcessInstanceId);
            return this;
        }

        public Builder firstResult(int firstResult) {
            this.firstResult = firstResult;
            return this;
        }

        public Builder maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder total(long total) {
            this.total = total;
            return this;
        }

        public Builder addToTotal(long total) {
            this.total += total;
            return this;
        }
    }
}
