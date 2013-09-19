package piecework.persistence.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import piecework.common.ViewContext;
import piecework.model.*;
import piecework.process.ProcessInstanceSearchCriteria;

import java.util.List;
import java.util.Map;

/**
 * @author James Renfro
 */
public interface ProcessInstanceRepositoryCustom {

    Page<ProcessInstance> findByCriteria(ProcessInstanceSearchCriteria criteria, Pageable pageable);

    ProcessInstance update(String id, String label, Map<String, List<Value>> data, List<Attachment> attachments, Submission submission);

}
