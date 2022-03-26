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
import static software.amazon.auditmanager.assessment.AbstractTestBase.*;

import java.time.Duration;
import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import software.amazon.awssdk.services.auditmanager.model.AccessDeniedException;
import software.amazon.awssdk.services.auditmanager.model.CreateAssessmentRequest;
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
import software.amazon.awssdk.services.auditmanager.model.DeleteAssessmentRequest;
import software.amazon.awssdk.services.auditmanager.model.DeleteAssessmentResponse;
import software.amazon.awssdk.services.auditmanager.model.AuditManagerException;
import software.amazon.awssdk.services.auditmanager.model.InternalServerException;
import software.amazon.awssdk.services.auditmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.auditmanager.model.ValidationException;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {


  @Mock
  AuditManagerClient sdkClient;
  private DeleteHandler handler;
  @Mock
  private AmazonWebServicesClientProxy proxy;
  @Mock
  private ProxyClient<AuditManagerClient> proxyClient;

  @BeforeEach
  public void setup() {
    proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
    sdkClient = mock(AuditManagerClient.class);
    proxyClient = MOCK_PROXY(proxy, sdkClient);
    handler = new DeleteHandler();
  }

  @Test
  public void testDeleteAsssessment_simpleSuccess_shouldSucceed() {
    final ResourceModel model = ResourceModel.builder().assessmentId(ASSESSMENT_ID).build();
    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(model)
        .build();
    final DeleteAssessmentResponse deleteAssessmentResponse = DeleteAssessmentResponse.builder().build();

    when(proxyClient.client().deleteAssessment(any(DeleteAssessmentRequest.class)))
        .thenReturn(deleteAssessmentResponse);
    final ProgressEvent<ResourceModel, CallbackContext> response =
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
    assertThat(response.getResourceModel()).isNull();
    assertThat(response.getResourceModels()).isNull();
    assertThat(response.getMessage()).isNull();
    assertThat(response.getErrorCode()).isNull();
    verify(proxyClient.client()).deleteAssessment(any(DeleteAssessmentRequest.class));
  }

  @Test
  public void handleRequest_failedDelete_ThrowsException() {
    final ResourceModel model = ResourceModel.builder().assessmentId(ASSESSMENT_ID).build();
    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(model)
        .build();

    when(proxyClient.client().deleteAssessment(any(DeleteAssessmentRequest.class)))
        .thenThrow(InternalServerException.class);
    assertThrows(CfnServiceInternalErrorException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().deleteAssessment(any(DeleteAssessmentRequest.class)))
        .thenThrow(ResourceNotFoundException.class);
    assertThrows(CfnNotFoundException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().deleteAssessment(any(DeleteAssessmentRequest.class)))
        .thenThrow(AuditManagerException.class);
    assertThrows(CfnServiceInternalErrorException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().deleteAssessment(any(DeleteAssessmentRequest.class)))
        .thenThrow(ValidationException.class);
    assertThrows(CfnInvalidRequestException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().deleteAssessment(any(DeleteAssessmentRequest.class)))
        .thenThrow(
            AccessDeniedException.builder().message(ExceptionTranslator.AUDIT_MANAGER_NOT_ENABLED_MESSAGE).build());
    final ProgressEvent<ResourceModel, CallbackContext> response =
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    assertThat(response.getMessage()).isEqualTo(ExceptionTranslator.AUDIT_MANAGER_NOT_ENABLED_MESSAGE);
    assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AccessDenied);

  }

}
