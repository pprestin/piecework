package piecework.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import piecework.common.SearchQueryParameters;
import piecework.model.*;
import piecework.common.SearchCriteria;
import piecework.security.Sanitizer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author James Renfro
 */
public interface ProcessInstanceRepositoryCustom {

    Page<ProcessInstance> findByCriteria(Set<String> processDefinitionKeys, SearchCriteria criteria, Pageable pageable, Sanitizer sanitizer);

    Page<ProcessInstance> findByQueryParameters(Set<String> processDefinitionKeys, SearchQueryParameters queryParameters, Pageable pageable, Sanitizer sanitizer);

    Page<ProcessInstance> findByQuery(Query query, Pageable pageable);

    ProcessInstance findByTaskId(String processDefinitionKey, String taskId);

    ProcessInstance update(String id, String label, Map<String, List<Value>> data, Map<String, List<Message>> messages, List<Attachment> attachments, Submission submission, String applicationStatusExplanation);

    ProcessInstance update(String id, Operation operation, String applicationStatus, String applicationStatusExplanation, String processStatus, Set<Task> tasks);

    boolean update(String id, String engineProcessInstanceId);

    boolean update(String id, Task task);

    ProcessInstance update(String id, String processStatus, String applicationStatus, Map<String, List<Value>> data);

}
