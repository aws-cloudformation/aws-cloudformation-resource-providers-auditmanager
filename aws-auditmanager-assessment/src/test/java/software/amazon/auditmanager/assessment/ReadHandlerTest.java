package software.amazon.auditmanager.assessment;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

import java.time.Duration;
import software.amazon.awssdk.services.auditmanager.model.AccessDeniedException;
import software.amazon.awssdk.services.auditmanager.model.ListAssessmentsRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import software.amazon.awssdk.services.auditmanager.model.Assessment;
import software.amazon.awssdk.services.auditmanager.model.GetAssessmentRequest;
import software.amazon.awssdk.services.auditmanager.model.GetAssessmentResponse;
import software.amazon.awssdk.services.auditmanager.model.AuditManagerException;
import software.amazon.awssdk.services.auditmanager.model.InternalServerException;
import software.amazon.awssdk.services.auditmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.auditmanager.model.ValidationException;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

  @Mock
  AuditManagerClient sdkClient;
  private ReadHandler handler;
  @Mock
  private AmazonWebServicesClientProxy proxy;
  @Mock
  private ProxyClient<AuditManagerClient> proxyClient;

  @BeforeEach
  public void setup() {
    proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
    sdkClient = mock(AuditManagerClient.class);
    proxyClient = MOCK_PROXY(proxy, sdkClient);
    handler = new ReadHandler();
  }

  @Test
  public void testGetAssessment_simpleSuccess_shouldSucceed() {
    sendRequestAndVerifyResponses(makeAssessment(null, null));
  }

  @Test
  public void testGetAssessmentWithTags_simpleSuccess_shouldSucceed() {
    sendRequestAndVerifyResponses(makeAssessment(null, makeTags()));
  }

  @Test
  public void testGetAssessment_failedRead_ThrowsException() {
    final ResourceModel inputResourceModel = ResourceModel.builder().assessmentId(ASSESSMENT_ID).build();
    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(inputResourceModel)
        .build();

    when(proxyClient.client().getAssessment(any(GetAssessmentRequest.class)))
        .thenThrow(InternalServerException.class);
    assertThrows(CfnServiceInternalErrorException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().getAssessment(any(GetAssessmentRequest.class)))
        .thenThrow(AuditManagerException.class);
    assertThrows(CfnServiceInternalErrorException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().getAssessment(any(GetAssessmentRequest.class)))
        .thenThrow(ResourceNotFoundException.class);
    assertThrows(CfnNotFoundException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().getAssessment(any(GetAssessmentRequest.class)))
        .thenThrow(ValidationException.class);
    assertThrows(CfnInvalidRequestException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().getAssessment(any(GetAssessmentRequest.class)))
        .thenThrow(
            AccessDeniedException.builder().message(ExceptionTranslator.AUDIT_MANAGER_NOT_ENABLED_MESSAGE).build());

    final ProgressEvent<ResourceModel, CallbackContext> response =
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    assertThat(response.getMessage()).isEqualTo(ExceptionTranslator.AUDIT_MANAGER_NOT_ENABLED_MESSAGE);
    assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AccessDenied);

  }

  private void sendRequestAndVerifyResponses(final Assessment expectedAssessment) {
    final ResourceModel expectedResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), expectedAssessment);
    final ResourceModel model = ResourceModel.builder().assessmentId(ASSESSMENT_ID).build();
    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(model)
        .build();
    final GetAssessmentResponse getAssessmentResponse =
        GetAssessmentResponse.builder()
            .assessment(expectedAssessment)
            .build();
    when(proxyClient.client().getAssessment(any(GetAssessmentRequest.class)))
        .thenReturn(getAssessmentResponse);
    final ProgressEvent<ResourceModel, CallbackContext> response
        = handler.handleRequest(proxy, request, null, proxyClient, logger);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);

    assertEquals(expectedAssessment.arn(), response.getResourceModel().getArn());
    assertEquals(makeCfnAccount(), response.getResourceModel().getAwsAccount());
    assertEquals(ASSESSMENT_ID, response.getResourceModel().getAssessmentId());
    assertEquals(FRAMEWORK_ID, response.getResourceModel().getFrameworkId());
    assertEquals(makeCfnAssessmentReportsDestination(), response.getResourceModel().getAssessmentReportsDestination());
    assertEquals(makeCfnScope(), response.getResourceModel().getScope());
    assertEquals(makeCfnRoles(), response.getResourceModel().getRoles());
    assertEquals(ASSESSMENT_STATUS, response.getResourceModel().getStatus());
    AssessmentMetadataUtilsTest.validateCfnDelegation(response.getResourceModel().getDelegations());
    if (expectedAssessment.tags() == null) {
      assertNull(response.getResourceModel().getTags());
    } else {
      assertEquals(expectedResourceModel.getTags(), response.getResourceModel().getTags());
    }
    assertThat(response.getResourceModels()).isNull();
    assertThat(response.getMessage()).isNull();
    assertThat(response.getErrorCode()).isNull();
    verify(proxyClient.client(), times(1)).getAssessment(any(GetAssessmentRequest.class));
  }

}
