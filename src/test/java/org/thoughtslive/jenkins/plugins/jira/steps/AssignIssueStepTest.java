package org.thoughtslive.jenkins.plugins.jira.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.PrintStream;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtslive.jenkins.plugins.jira.Site;
import org.thoughtslive.jenkins.plugins.jira.api.ResponseData;
import org.thoughtslive.jenkins.plugins.jira.api.ResponseData.ResponseDataBuilder;
import org.thoughtslive.jenkins.plugins.jira.service.JiraService;

/**
 * Unit test cases for AssignIssueStep class.
 *
 * @author Naresh Rayapati
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AssignIssueStep.class, Site.class})
public class AssignIssueStepTest {

  @Mock
  TaskListener taskListenerMock;
  @Mock
  Run<?, ?> runMock;
  @Mock
  EnvVars envVarsMock;
  @Mock
  PrintStream printStreamMock;
  @Mock
  JiraService jiraServiceMock;
  @Mock
  Site siteMock;
  @Mock
  StepContext contextMock;

  private AssignIssueStep.Execution stepExecution;

  @Before
  public void setup() throws IOException, InterruptedException {

    // Prepare site.
    when(envVarsMock.get("JIRA_SITE")).thenReturn("LOCAL");
    when(envVarsMock.get("BUILD_URL")).thenReturn("http://localhost:8080/jira-testing/job/01");

    PowerMockito.mockStatic(Site.class);
    Mockito.when(Site.get(any())).thenReturn(siteMock);
    when(siteMock.getService()).thenReturn(jiraServiceMock);

    when(runMock.getCauses()).thenReturn(null);
    when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
    doNothing().when(printStreamMock).println();

    final ResponseDataBuilder<Void> builder = ResponseData.builder();
    when(jiraServiceMock.assignIssue(any(), any(), any()))
        .thenReturn(builder.successful(true).code(200).message("Success").build());

    when(contextMock.get(Run.class)).thenReturn(runMock);
    when(contextMock.get(TaskListener.class)).thenReturn(taskListenerMock);
    when(contextMock.get(EnvVars.class)).thenReturn(envVarsMock);
  }

  @Test
  public void testWithEmptyIdOrKeyThrowsAbortException() throws Exception {
    final AssignIssueStep step = new AssignIssueStep("", "testUser", "testUserId");
    stepExecution = new AssignIssueStep.Execution(step, contextMock);

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.run();
    }).withMessage("idOrKey is empty or null.").withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testWithEmptyUserNameOrAccountIdThrowsAbortException() throws Exception {
    final AssignIssueStep step = new AssignIssueStep("TEST-1", "", "");
    stepExecution = new AssignIssueStep.Execution(step, contextMock);

    // Execute Test.
    stepExecution.run();

    // Assert Test
    verify(jiraServiceMock, times(1)).assignIssue("TEST-1", null, null);
    assertThat(step.isFailOnError()).isEqualTo(true);
  }

  @Test
  public void testSuccessfulUnassignIssue() throws Exception {
    final AssignIssueStep step = new AssignIssueStep("TEST-1", null, null);
    stepExecution = new AssignIssueStep.Execution(step, contextMock);

    // Execute Test.
    stepExecution.run();

    // Assert Test
    verify(jiraServiceMock, times(1)).assignIssue("TEST-1", null, null);
    assertThat(step.isFailOnError()).isEqualTo(true);
  }

  @Test
  public void testSuccessfulAssignIssue() throws Exception {
    final AssignIssueStep step = new AssignIssueStep("TEST-1", "testUser", null);
    stepExecution = new AssignIssueStep.Execution(step, contextMock);

    // Execute Test.
    stepExecution.run();

    // Assert Test
    verify(jiraServiceMock, times(1)).assignIssue("TEST-1", "testUser", null);
    assertThat(step.isFailOnError()).isEqualTo(true);
  }

  @Test
  public void testSuccessfulAssignIssueByAccountId() throws Exception {
    final AssignIssueStep step = new AssignIssueStep("TEST-1", null, "testUser");
    stepExecution = new AssignIssueStep.Execution(step, contextMock);

    // Execute Test.
    stepExecution.run();

    // Assert Test
    verify(jiraServiceMock, times(1)).assignIssue("TEST-1", null, "testUser");
    assertThat(step.isFailOnError()).isEqualTo(true);
  }
}
