package nl.esciencecenter.e3dchem.sstea.integrationtests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.knime.core.data.container.BufferTracker;
import org.knime.core.node.AbstractNodeView;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.Node;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.util.ViewUtils;
import org.knime.core.node.workflow.NativeNodeContainer;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.NodeContainerState;
import org.knime.core.node.workflow.NodeMessage;
import org.knime.core.node.workflow.NodeMessage.Type;
import org.knime.core.node.workflow.SingleNodeContainer;
import org.knime.core.node.workflow.SubNodeContainer;
import org.knime.core.node.workflow.UnsupportedWorkflowVersionException;
import org.knime.core.node.workflow.WorkflowContext;
import org.knime.core.node.workflow.WorkflowLoadHelper;
import org.knime.core.node.workflow.WorkflowManager;
import org.knime.core.node.workflow.WorkflowPersistor.LoadResultEntry.LoadResultEntryType;
import org.knime.core.node.workflow.WorkflowPersistor.WorkflowLoadResult;
import org.knime.core.util.LockFailedException;
import org.knime.core.util.Pair;
import org.knime.testing.core.TestrunConfiguration;
import org.knime.testing.core.ng.TestflowConfiguration;
import org.knime.testing.core.ng.WorkflowTestContext;
import org.knime.testing.node.config.TestConfigNodeModel;
import org.knime.workbench.core.util.ImageRepository;
import org.knime.workbench.core.util.ImageRepository.SharedImages;
import org.knime.workbench.repository.RepositoryManager;

import junit.framework.AssertionFailedError;

/**
 * Test which will execute a single workflow.
 * 
 * Based on org.knime.testing.core.ng.TestflowRunnerApplication
 * 
 * Because that class is not exported by org.knime.testing plugin we copied the reusable classes.
 * 
 * Because that implementation is running the test harness it conflicts with the tycho test harness, so will take the run method
 * of the tests the TestflowRunnerApplication generates and copy them.
 *
 */
public class SimpleTest {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SimpleTest.class);
	
    @Rule
    public ErrorCollector collector = new ErrorCollector();
    private WorkflowTestContext testContext;
	private TestrunConfiguration runConfiguration;

    @Before
    public void setUp() {
        runConfiguration = new TestrunConfiguration();
        runConfiguration.setLoadSaveLoad(false); // set to true has not been implemented
        runConfiguration.setTestDialogs(false); // set to true has not been implemented
    }
    
    @Test
    public void test_simple() throws IOException, InvalidSettingsException, CanceledExecutionException,
            UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
        File workflowDir = new File("src/knime/sstea-simple-test");
        runTestWorkflow(workflowDir);
    }
    
//    @Test
//    public void test_simply_broken() throws IOException, InvalidSettingsException, CanceledExecutionException,
//            UnsupportedWorkflowVersionException, LockFailedException {
//        File workflowDir = new File("src/knime/simply-broken");
//        runTestWorkflow(workflowDir);
//    }

	private void runTestWorkflow(File workflowDir) throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		File testcaseRoot = workflowDir;

        // this is to load the repository plug-in
        RepositoryManager.INSTANCE.toString();
        // and this initialized the image repository in the main thread; otherwise resolving old node factories
        // in FileSingleNodeContainerPersistor will fail (see bug# 4464)
        ImageRepository.getImage(SharedImages.Busy);

        // createLoadTest
        testContext = new WorkflowTestContext(runConfiguration);
        WorkflowManager wfm = loadWorkflow(workflowDir, testcaseRoot, runConfiguration);
        testContext.setWorkflowManager(wfm);

        // WorkflowDeprecationTest
        if (runConfiguration.isReportDeprecatedNodes()) {
        	checkForDeprecatedNodes(wfm);
        }
        
        // WorkflowOpenViewsTest
        if (runConfiguration.isTestViews()) {
        	openViews(wfm);
        }

        // WorkflowExecuteTest.resetTestflowConfigNode
        for (NodeContainer cont : wfm.getNodeContainers()) {
            if ((cont instanceof NativeNodeContainer)
                    && (((NativeNodeContainer) cont).getNodeModel() instanceof TestConfigNodeModel)) {
                wfm.resetAndConfigureNode(cont.getID());
            }
        }
        // WorkflowExecuteTest.run
        try {
            wfm.executeAllAndWaitUntilDone();
        } catch (Exception ex) {
            collector.addError(ex);
        }
        // WorkflowExecuteTest.checkExecutionStatus
        checkExecutionStatus(testContext.getWorkflowManager(), testContext.getTestflowConfiguration());

        if (runConfiguration.isCheckNodeMessages()) {
            // WorkflowNodeMessagesTest
            checkNodeMessages(testContext.getWorkflowManager(), testContext.getTestflowConfiguration());
        }

        // TODO WorkflowDialogsTest (low prior)

        // TODO WorkflowHiliteTest (low prior)

        // WorkflowCloseViewsTest
        if (runConfiguration.isTestViews()) {
        	closeViews();
        }
        
        // TODO save (low prior)

        // TODO WorkflowCloseTest (med prior)
        if (runConfiguration.isCloseWorkflowAfterTest()) {
        	closeWorkflow();
        }

        // TODO WorkflowLogMessagesTest (med prior)

        // TODO WorkflowUncaughtExceptionsTest (high prior)

        // TODO WorkflowMemLeakTest (low prior)
	}

	/**
	 * Copied from org.knime.testing.core.ng.WorkflowCloseTest.run()
	 */
    private void closeWorkflow() {
    	try {
            testContext.getWorkflowManager().shutdown();
            testContext.getWorkflowManager().getParent().removeNode(testContext.getWorkflowManager().getID());

            List<NodeContainer> openWorkflows = new ArrayList<NodeContainer>(WorkflowManager.ROOT.getNodeContainers());
            openWorkflows.removeAll(testContext.getAlreadyOpenWorkflows());
            if (openWorkflows.size() > 0) {
            	collector.addError(new AssertionFailedError(openWorkflows.size()
                        + " dangling workflows detected: " + openWorkflows));
            }

            Collection<Pair<NodeContainer, StackTraceElement[]>> openBuffers =
                BufferTracker.getInstance().getOpenBuffers();
            if (!openBuffers.isEmpty()) {
            	collector.addError(new AssertionFailedError(openBuffers.size() + " open buffers detected: "
                    + openBuffers.stream().map(p -> p.getFirst().getNameWithID()).collect(Collectors.joining(", "))));
            }
            BufferTracker.getInstance().clear();
        } catch (Throwable t) {
        	collector.addError(t);
        }
    }

	/**
     * Copied from org.knime.testing.core.ng.WorkflowExecuteTest
     * 
     * @param workflowManager
     * @param testflowConfiguration
     */
    private void checkExecutionStatus(WorkflowManager wfm, TestflowConfiguration flowConfiguration) {
        for (NodeContainer node : wfm.getNodeContainers()) {
            NodeContainerState status = node.getNodeContainerState();

            if (node instanceof SubNodeContainer) {
                checkExecutionStatus(((SubNodeContainer) node).getWorkflowManager(), flowConfiguration);
            } else if (node instanceof WorkflowManager) {
                checkExecutionStatus((WorkflowManager) node, flowConfiguration);
            } else if (node instanceof SingleNodeContainer) {
                if (!status.isExecuted() && !flowConfiguration.nodeMustFail(node.getID())) {
                    NodeMessage nodeMessage = node.getNodeMessage();
                    String error = "Node '" + node.getNameWithID() + "' is not executed. Error message is: "
                            + nodeMessage.getMessage();
                    collector.addError(new AssertionFailedError(error));

                    Pattern p = Pattern.compile(Pattern.quote(nodeMessage.getMessage()));
                    flowConfiguration.addNodeErrorMessage(node.getID(), p);
                    flowConfiguration.addRequiredError(p);
                } else if (status.isExecuted() && flowConfiguration.nodeMustFail(node.getID())) {
                    String error = "Node '" + node.getNameWithID() + "' is executed although it should have failed.";
                    collector.addError(new AssertionFailedError(error));
                }
            } else {
                throw new IllegalStateException("Unknown node container type: " + node.getClass());
            }
        }

    }

    /**
     * Copied from org.knime.testing.core.ng.WorkflowLoadTest
     * 
     * 
     * @param result
     * @param workflowDir
     * @param testcaseRoot
     * @param runConfig
     * @return
     * @throws IOException
     * @throws InvalidSettingsException
     * @throws CanceledExecutionException
     * @throws UnsupportedWorkflowVersionException
     * @throws LockFailedException
     */
            WorkflowManager loadWorkflow(final File workflowDir, final File testcaseRoot, final TestrunConfiguration runConfig)
                    throws IOException, InvalidSettingsException, CanceledExecutionException, UnsupportedWorkflowVersionException,
                    LockFailedException {
        WorkflowLoadHelper loadHelper = new WorkflowLoadHelper() {
            /**
             * {@inheritDoc}
             */
            @Override
            public WorkflowContext getWorkflowContext() {
                WorkflowContext.Factory fac = new WorkflowContext.Factory(workflowDir);
                fac.setMountpointRoot(testcaseRoot);
                return fac.createContext();
            }
        };

        WorkflowLoadResult loadRes = WorkflowManager.loadProject(workflowDir, new ExecutionMonitor(), loadHelper);
        if ((loadRes.getType() == LoadResultEntryType.Error)
                || ((loadRes.getType() == LoadResultEntryType.DataLoadError) && loadRes.getGUIMustReportDataLoadErrors())) {
            collector.addError(new AssertionFailedError(loadRes.getFilteredError("", LoadResultEntryType.Error)));
        }
        if (runConfig.isCheckForLoadWarnings() && loadRes.hasWarningEntries()) {
            collector.addError(new AssertionFailedError(loadRes.getFilteredError("", LoadResultEntryType.Warning)));
        }

        WorkflowManager wfm = loadRes.getWorkflowManager();
        wfm.addWorkflowVariables(true, runConfig.getFlowVariables());
        return wfm;
    }

    /**
     * Copied from org.knime.testing.core.ng.WorkflowNodeMessagesTest
     * 
     * @param result
     * @param wfm
     * @param flowConfiguration
     */
    private void checkNodeMessages(final WorkflowManager wfm, final TestflowConfiguration flowConfiguration) {
        for (NodeContainer node : wfm.getNodeContainers()) {
            if (!testContext.isPreExecutedNode(node)) {
                if (node instanceof SubNodeContainer) {
                    checkNodeMessages(((SubNodeContainer) node).getWorkflowManager(), flowConfiguration);
                } else if (node instanceof WorkflowManager) {
                    checkNodeMessages((WorkflowManager) node, flowConfiguration);
                    checkSingleNode(node, flowConfiguration);
                } else {
                    checkSingleNode(node, flowConfiguration);
                }
            }
        }
    }

    /**
     * Copied from org.knime.testing.core.ng.WorkflowNodeMessagesTest
     * 
     * @param result
     * @param node
     * @param flowConfiguration
     */
    private void checkSingleNode(final NodeContainer node, final TestflowConfiguration flowConfiguration) {
        NodeMessage nodeMessage = node.getNodeMessage();

        Pattern expectedErrorMessage = flowConfiguration.getNodeErrorMessage(node.getID());
        if (expectedErrorMessage != null) {
            if (!expectedErrorMessage.matcher(nodeMessage.getMessage().trim()).matches()) {
                String error = "Node '" + node.getNameWithID() + "' has unexpected error message: expected '"
                        + TestflowConfiguration.patternToString(expectedErrorMessage) + "', got '" + nodeMessage.getMessage()
                        + "'";
                collector.addError(new AssertionFailedError(error));
            }
        } else if (Type.ERROR.equals(nodeMessage.getMessageType())) {
            String error = "Node '" + node.getNameWithID() + "' has unexpected error message: " + nodeMessage.getMessage();
            collector.addError(new AssertionFailedError(error));
        }

        Pattern expectedWarningMessage = flowConfiguration.getNodeWarningMessage(node.getID());
        if (expectedWarningMessage != null) {
            if (!expectedWarningMessage.matcher(nodeMessage.getMessage().trim()).matches()) {
                String error = "Node '" + node.getNameWithID() + "' has unexpected warning message: expected '"
                        + TestflowConfiguration.patternToString(expectedWarningMessage) + "', got '" + nodeMessage.getMessage()
                        + "'";
                collector.addError(new AssertionFailedError(error));
            }
        } else if (Type.WARNING.equals(nodeMessage.getMessageType())) {
            String error = "Node '" + node.getNameWithID() + "' has unexpected warning message: " + nodeMessage.getMessage();
            collector.addError(new AssertionFailedError(error));
        }
    }
    
    /**
     * Copied from org.knime.testing.core.ng.WorkflowUncaughtExceptionsTest
     * @param wfm
     */
    private void checkForDeprecatedNodes(final WorkflowManager wfm) {
        for (NodeContainer node : wfm.getNodeContainers()) {
            if (node instanceof SingleNodeContainer) {
                if ("true".equals(((SingleNodeContainer)node).getXMLDescription().getAttribute("deprecated"))) {
                	collector.addError(new AssertionFailedError("Node '" + node.getName() + "' is deprecated."));
                }
            } else if (node instanceof WorkflowManager) {
                checkForDeprecatedNodes((WorkflowManager)node);
            } else {
                throw new IllegalStateException("Unknown node container type: " + node.getClass().getName());
            }
        }
    }
    
    /**
     * Copied from org.knime.testing.core.ng.WorkflowOpenViewsTest
     * 
     * @param result
     * @param wfm
     */
    private void openViews(final WorkflowManager wfm) {
        for (NodeContainer node : wfm.getNodeContainers()) {
            if (node instanceof SingleNodeContainer) {
                for (int i = 0; i < node.getNrViews(); i++) {
                    try {
                        openView((SingleNodeContainer)node, i);
                    } catch (Exception ex) {
                        String msg =
                                "View " + i + " of node '" + node.getNameWithID() + "' has thrown a "
                                        + ex.getClass().getSimpleName() + " during open: " + ex.getMessage();
                        AssertionFailedError error = new AssertionFailedError(msg);
                        error.initCause(ex);
                        collector.addError(error);
                    }
                }
                // test InteractiveNodeViews
                if (node.hasInteractiveView()) {
                    try {
                        openInteractiveView((SingleNodeContainer)node);
                    } catch (Exception ex) {
                        String msg =
                                "Interactive view of node '" + node.getNameWithID() + "' has thrown a "
                                        + ex.getClass().getSimpleName() + " during open: " + ex.getMessage();
                        AssertionFailedError error = new AssertionFailedError(msg);
                        error.initCause(ex);
                        collector.addError(error);
                    }
                }
            } else if (node instanceof WorkflowManager) {
                openViews((WorkflowManager)node);
            } else {
                throw new IllegalStateException("Unknown node container type: " + node.getClass());
            }
        }
    }

    /**
     * Copied from org.knime.testing.core.ng.WorkflowOpenViewsTest
     * 
     * @param node
     * @param index
     */
    private void openView(final SingleNodeContainer node, final int index) {
        // test NodeViews
        LOGGER.debug("opening view nr. " + index + " for node " + node.getName());
        ViewUtils.invokeAndWaitInEDT(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                final AbstractNodeView<? extends NodeModel> view = node.getView(index);
                // store the view in order to close it after the test finishes
                List<AbstractNodeView<? extends NodeModel>> l = testContext.getNodeViews().get(node);
                if (l == null) {
                    l = new ArrayList<AbstractNodeView<? extends NodeModel>>(2);
                    testContext.getNodeViews().put(node, l);
                }
                l.add(view);
                // open it now.
                Node.invokeOpenView(view, "View #" + index);
            }
        });
    }

    /**
     * Copied from org.knime.testing.core.ng.WorkflowOpenViewsTest
     * 
     * @param node
     */
    private void openInteractiveView(final SingleNodeContainer node) {
        LOGGER.debug("opening interactive view for node " + node.getName());
        final AbstractNodeView<?> view = node.getInteractiveView();
        // open it now.
        ViewUtils.invokeAndWaitInEDT(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                Node.invokeOpenView(view, "Interactive View");
            }
        });
    }
    
    /**
     * Copied from org.knime.testing.core.ng.WorkflowCloseViewsTest
     * 
     * @param result
     * @throws InterruptedException
     */
    private void closeViews() throws InterruptedException {
        Semaphore done = new Semaphore(1);
        done.acquire();
        SwingUtilities.invokeLater(() -> done.release());
        done.tryAcquire(2, TimeUnit.SECONDS);

        for (Map.Entry<SingleNodeContainer, List<AbstractNodeView<? extends NodeModel>>> e : testContext.getNodeViews()
                .entrySet()) {
            for (AbstractNodeView<? extends NodeModel> view : e.getValue()) {
                try {
                    Node.invokeCloseView(view);
                } catch (Exception ex) {
                    String msg =
                            "View '" + view + "' of node '" + e.getKey().getNameWithID() + "' has thrown a "
                                    + ex.getClass().getSimpleName() + " during close: " + ex.getMessage();
                    AssertionFailedError error = new AssertionFailedError(msg);
                    error.initCause(ex);
                    collector.addError(error);
                }
            }
        }
    }
}
