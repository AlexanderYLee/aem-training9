package com.epam.aemtraining.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by Aliaksandr_Li on 5/18/2016.
 */
@Component(immediate = true,metatype=true)
@Service(EventHandler.class)
@Properties({
        @Property(name = EventConstants.EVENT_TOPIC, value = SlingConstants.TOPIC_RESOURCE_ADDED),
        @Property(name = EventConstants.EVENT_FILTER, value =
                "(&(path=/content/geometrixx/*)(resourceType=cq:Page))")
})
public class PageCreationEventListener  implements EventHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Reference
    SlingRepository repository;
    @Reference
    JobManager jobManager;

    public void handleEvent(Event event) {
        log.debug("Event is caught:" + event.getTopic());
        try {
            Session session = repository.loginAdministrative(null);
            String pagePath = (String)event.getProperty(SlingConstants.PROPERTY_PATH);
            Node node = (Node) session.getItem(pagePath + "/jcr:content");
            if (node!=null){
                if (!node.hasProperty("pathToMove")||
                        (node.hasProperty("pathToMove")&&!"".equals(node.getProperty("pathToMove").getString()))){
                    Map<String, Object> props = new HashMap<String, Object>(2);
                    props.put("path", event.getProperty(SlingConstants.PROPERTY_PATH));
                    Job job = jobManager.addJob("com/epam/aemtraining9/workflowJob", props);
                    log.debug("Job is added:" + job.getTopic());
                }
            }
        } catch (RepositoryException e) {
            log.debug(e.getMessage());
        }
    }
}
