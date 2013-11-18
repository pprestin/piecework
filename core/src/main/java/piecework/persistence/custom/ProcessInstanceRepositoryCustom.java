package piecework.persistence.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import piecework.common.ViewContext;
import piecework.model.*;
import piecework.model.Process;
import piecework.process.ProcessInstanceSearchCriteria;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
public interface ProcessInstanceRepositoryCustom {

    Page<ProcessInstance> findByCriteria(ProcessInstanceSearchCriteria criteria, Pageable pageable);

    Page<ProcessInstance> findByQuery(Query query, Pageable pageable);

    ProcessInstance findByTaskId(String processDefinitionKey, String taskId);

    ProcessInstance update(String id, String label, Map<String, List<Value>> data, List<Attachment> attachments, Submission submission);

    boolean update(String id, Operation operation, String applicationStatus, String applicationStatusExplanation, String processStatus, Set<Task> tasks);

    boolean update(String id, String engineProcessInstanceId);

    boolean update(String id, Task task);

}
