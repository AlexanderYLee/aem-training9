package com.epam.aemtraining.impl;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.Workflow;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.model.WorkflowModel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 *
 * Created by Aliaksandr_Li on 5/18/2016.
 */
@Component
@Service(JobConsumer.class)
@Property(name= JobConsumer.PROPERTY_TOPICS, value="com/epam/aemtraining9/workflowJob")
public class WorkflowStarter  implements JobConsumer{
    private static final Logger log = LoggerFactory.getLogger(WorkflowStarter.class);

    private final static String wfModelName = "/etc/workflow/models/trainingwfmodel/jcr:content/model";

    @Reference
    private WorkflowService workflowService;
    @Reference
    private
    SlingRepository repository;

    public JobResult process(Job job) {
        log.debug("Workflow starter is initialized!");
        try {
            Session session = repository.loginAdministrative(null);
            //Create a workflow session
            WorkflowSession wfSession = workflowService.getWorkflowSession(session);
            // Get the workflow model
            WorkflowModel wfModel = wfSession.getModel(wfModelName);
            // Get the workflow data
            // The first param in the newWorkflowData method is the payloadType.
            String workflowContent = job.getProperty("path", String.class);
            WorkflowData wfData = wfSession.newWorkflowData("JCR_PATH", workflowContent);
            // Run the Workflow.
            Workflow wf = wfSession.startWorkflow(wfModel, wfData);
            log.debug("Workflow starter is executed!");
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return JobResult.OK;
    }
}
