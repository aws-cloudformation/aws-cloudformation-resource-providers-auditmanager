package software.amazon.auditmanager.assessment;

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
import static software.amazon.auditmanager.assessment.AbstractTestBase.makeCfnAssessmentReportsDestination;
import static software.amazon.auditmanager.assessment.AbstractTestBase.makeCfnScope;
import static software.amazon.auditmanager.assessment.Utils.sdkTagsToCfnTags;

import java.time.Duration;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.auditmanager.model.CreateAssessmentRequest;
import software.amazon.awssdk.services.auditmanager.model.CreateAssessmentResponse;
import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import software.amazon.awssdk.services.auditmanager.model.AuditManagerException;
import software.amazon.awssdk.services.auditmanager.model.InternalServerException;
import software.amazon.awssdk.services.auditmanager.model.ValidationException;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

  @Mock
  AuditManagerClient sdkClient;
  private CreateHandler handler;
  @Mock
  private AmazonWebServicesClientProxy proxy;
  @Mock
  private ProxyClient<AuditManagerClient> proxyClient;

  @BeforeEach
  public void setup() {
    proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
    sdkClient = mock(AuditManagerClient.class);
    proxyClient = MOCK_PROXY(proxy, sdkClient);
    handler = new CreateHandler();
  }

  @Test
  public void testCreateAssessment_simpleSuccess_shouldSucceed() {
    final ResourceModel inputResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, makeTags()));

    final ResourceModel createAssessmentRequestResourceModel = ResourceModel.builder()
        .tags(inputResourceModel.getTags())
        .scope(inputResourceModel.getScope())
        .roles(inputResourceModel.getRoles())
        .name(inputResourceModel.getName())
        .description(inputResourceModel.getDescription())
        .frameworkId(FRAMEWORK_ID)
        .build();

    sendRequestAndVerifyResponses(createAssessmentRequestResourceModel);
  }

  @Test
  public void testCreateAssessmentWithTags_simpleSuccess_shouldSucceed() {
    final ResourceModel inputResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, makeTags()));
    final ResourceModel createAssessmentRequestResourceModel = ResourceModel.builder()
        .tags(inputResourceModel.getTags())
        .scope(inputResourceModel.getScope())
        .roles(inputResourceModel.getRoles())
        .name(inputResourceModel.getName())
        .description(inputResourceModel.getDescription())
        .frameworkId(FRAMEWORK_ID)
        .build();
    sendRequestAndVerifyResponses(createAssessmentRequestResourceModel);
  }

  @Test
  public void testCreateAssessment_withInvalidRequest_shouldThrowInvalidRequest() {
    final ResourceModel inputResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, makeTags()));
    final ResourceModel createAssessmentRequestResourceModel = ResourceModel.builder()
        .tags(inputResourceModel.getTags())
        .scope(inputResourceModel.getScope())
        .roles(inputResourceModel.getRoles())
        .name(inputResourceModel.getName())
        .description(inputResourceModel.getDescription())
        .frameworkId(FRAMEWORK_ID)
        .assessmentId(ASSESSMENT_ID)
        .build();
    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(createAssessmentRequestResourceModel)
        .build();
    assertThrows(CfnInvalidRequestException.class,
        () ->{
          handler.handleRequest(proxy, request, null, proxyClient, logger);
        });
  }

  @Test
  public void testCreateAssessment_withFailedCreate_shouldThrowInternalError() {
    final ResourceModel inputResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, makeTags()));
    final ResourceModel createAssessmentRequestResourceModel = ResourceModel.builder()
        .tags(inputResourceModel.getTags())
        .scope(inputResourceModel.getScope())
        .roles(inputResourceModel.getRoles())
        .name(inputResourceModel.getName())
        .description(inputResourceModel.getDescription())
        .frameworkId(FRAMEWORK_ID)
        .build();
    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(createAssessmentRequestResourceModel)
        .build();

    when(proxyClient.client().createAssessment(any(CreateAssessmentRequest.class)))
        .thenThrow(AuditManagerException.class);
    assertThrows(CfnServiceInternalErrorException.class,
        () ->{
          handler.handleRequest(proxy, request, null, proxyClient, logger);
        });

    when(proxyClient.client().createAssessment(any(CreateAssessmentRequest.class)))
        .thenThrow(InternalServerException.class);
    assertThrows(CfnServiceInternalErrorException.class,
        () ->{
          handler.handleRequest(proxy, request, null, proxyClient, logger);
        });

    when(proxyClient.client().createAssessment(any(CreateAssessmentRequest.class)))
        .thenThrow(ValidationException.class);
    assertThrows(CfnInvalidRequestException.class,
        () ->{
          handler.handleRequest(proxy, request, null, proxyClient, logger);
        });
  }

  private void sendRequestAndVerifyResponses(final ResourceModel model) {
    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(model)
        .build();
    final CreateAssessmentResponse createAssessmentResponse =
        CreateAssessmentResponse.builder()
            .assessment(
                (model.getTags() != null) ? makeAssessment(null, makeTags()) : makeAssessment(null, null))
        .build();
    when(proxyClient.client().createAssessment(any(CreateAssessmentRequest.class)))
        .thenReturn(createAssessmentResponse);
    final ProgressEvent<ResourceModel, CallbackContext> response
        = handler.handleRequest(proxy, request, null, proxyClient, logger);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);

    assertThat(response.getResourceModel().getAssessmentId()).isEqualTo(ASSESSMENT_ID);
    assertThat(response.getResourceModel().getArn()).isEqualTo(ASSESSMENT_ARN);
    assertThat(response.getResourceModel().getArn()).isEqualTo(ASSESSMENT_ARN);
    if (model.getTags() == null) {
      assertNull(response.getResourceModel().getTags());
    } else {
      assertEquals(model.getTags(), response.getResourceModel().getTags());
    }
    assertThat(response.getResourceModel().getFrameworkId()).isEqualTo(FRAMEWORK_ID);
    assertThat(response.getResourceModel().getAssessmentReportsDestination()).
        isEqualTo(makeCfnAssessmentReportsDestination());
    assertThat(response.getResourceModel().getScope()).isEqualTo(makeCfnScope());
    assertThat(response.getResourceModel().getRoles()).isEqualTo(makeCfnRoles());
    assertThat(response.getResourceModel().getStatus()).isEqualTo(ASSESSMENT_STATUS);
    assertThat(response.getResourceModel().getName()).isEqualTo(ASSESSMENT_NAME);
    assertThat(response.getResourceModel().getDescription()).isEqualTo(ASSESSMENT_DESCRIPTION);
    AssessmentMetadataUtilsTest.validateCfnDelegation(response.getResourceModel().getDelegations());

    assertThat(response.getResourceModels()).isNull();
    assertThat(response.getMessage()).isNull();
    assertThat(response.getErrorCode()).isNull();
    verify(proxyClient.client(), times(1)).createAssessment(any(CreateAssessmentRequest.class));
  }
}
