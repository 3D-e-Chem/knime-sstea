package nl.esciencecenter.e3dchem.sstea.integrationtests;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.workflow.UnsupportedWorkflowVersionException;
import org.knime.core.util.LockFailedException;
import org.knime.testing.core.TestrunConfiguration;

import nl.esciencecenter.e3dchem.knime.testing.TestFlowRunner;

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
    @Rule
    public ErrorCollector collector = new ErrorCollector();
	private TestFlowRunner runner;

    @Before
    public void setUp() {
        TestrunConfiguration runConfiguration = new TestrunConfiguration();
        runConfiguration.setTestDialogs(true);
        runConfiguration.setReportDeprecatedNodes(true);
        runConfiguration.setCheckMemoryLeaks(true);
        runner = new TestFlowRunner(collector, runConfiguration);
    }
    
    @Test
    public void test_simple() throws IOException, InvalidSettingsException, CanceledExecutionException,
            UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
        File workflowDir = new File("src/knime/sstea-simple-test");
        runner.runTestWorkflow(workflowDir);
    }
//    
//    @Test
//    public void test_simply_broken() throws IOException, InvalidSettingsException, CanceledExecutionException,
//            UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
//        File workflowDir = new File("src/knime/simply-broken");
//        runner.runTestWorkflow(workflowDir);
//    }

}
