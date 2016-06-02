package com.epam.aemtraining.impl;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static com.day.cq.dam.commons.process.AbstractAssetWorkflowProcess.TYPE_JCR_PATH;

/**
 *
 * Created by Aliaksandr_Li on 5/19/2016.
 * new branch
 */
@Component
@Service(WorkflowProcess.class)
@Property(name = "process.label", value = "Move to path WF Process")
public class MoveToPathProcess implements WorkflowProcess {
    private static Logger log = LoggerFactory.getLogger(MoveToPathProcess.class);

    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        WorkflowData workflowData = workItem.getWorkflowData();
        if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
            String path = workflowData.getPayload().toString();
            try {
                Session jcrSession = workflowSession.adaptTo(Session.class);
                Node node = (Node) jcrSession.getItem(path + "/jcr:content");
                if (node != null){
                    String nodeName = node.getParent().getName();
                    String parentPath = node.getParent().getParent().getPath();
                    String pathToMove = "";
                    if (node.hasProperty("pathToMove")) {
                        pathToMove = node.getProperty("pathToMove").getString();
                        node.setProperty("pathToMove", "");//to exclude second EventHandler call
                    }
                    if (!"".equals(pathToMove)&&!pathToMove.equals(path)&&!pathToMove.equals(parentPath)) {
                        if (jcrSession.itemExists(pathToMove)){
                            jcrSession.move(path, pathToMove + "/" + nodeName);
                            jcrSession.save();
                        }
                    }
                }
            } catch (RepositoryException e) {
                throw new WorkflowException(e.getMessage(), e);
            }
        }
        log.info("Move to path WF Process executed");
    }

}