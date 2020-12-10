package software.amazon.auditmanager.assessment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static software.amazon.auditmanager.assessment.AbstractTestBase.*;
import static software.amazon.auditmanager.assessment.AssessmentMetadataUtils.*;
import static software.amazon.auditmanager.assessment.UpdateHandler.INACTIVE_ASSESSMENT_STATUS;

import java.time.Duration;
import com.google.common.collect.Lists;
import java.util.Date;
import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.auditmanager.model.Assessment;
import software.amazon.awssdk.services.auditmanager.model.AssessmentMetadata;
import software.amazon.awssdk.services.auditmanager.model.UpdateAssessmentRequest;
import software.amazon.awssdk.services.auditmanager.model.UpdateAssessmentResponse;
import software.amazon.awssdk.services.auditmanager.model.UpdateAssessmentStatusRequest;
import software.amazon.awssdk.services.auditmanager.model.UpdateAssessmentStatusResponse;
import software.amazon.awssdk.services.auditmanager.model.AssessmentStatus;
import software.amazon.awssdk.services.auditmanager.model.AuditManagerException;
import software.amazon.awssdk.services.auditmanager.model.InternalServerException;
import software.amazon.awssdk.services.auditmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.auditmanager.model.ValidationException;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

  @Mock
  AuditManagerClient sdkClient;
  private UpdateHandler handler;
  @Mock
  private AmazonWebServicesClientProxy proxy;
  @Mock
  private ProxyClient<AuditManagerClient> proxyClient;

  @BeforeEach
  public void setup() {
    proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
    sdkClient = mock(AuditManagerClient.class);
    proxyClient = MOCK_PROXY(proxy, sdkClient);
    handler = new UpdateHandler();
  }

  @Test
  public void testUpdateAssessment_simpleSuccessNameChange_shouldSucceed() {
    final ResourceModel previousResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    final ResourceModel currentResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    currentResourceModel.setName(ASSESSMENT_NAME_UPDATED);

    sendRequestAndVerifyResponses(previousResourceModel, currentResourceModel);
  }

  @Test
  public void testUpdateAssessment_simpleSuccessDescriptionChange_shouldSucceed() {
    final ResourceModel previousResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    final ResourceModel currentResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    currentResourceModel.setDescription(ASSESSMENT_DESCRIPTION_UPDATED);

    sendRequestAndVerifyResponses(previousResourceModel, currentResourceModel);
  }

  @Test
  public void testUpdateAssessment_simpleSuccessStatusChange_shouldSucceed() {
    final ResourceModel previousResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    final ResourceModel currentResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    currentResourceModel.setStatus(INACTIVE_ASSESSMENT_STATUS.toString());

    sendRequestAndVerifyResponses(previousResourceModel, currentResourceModel);
  }

  @Test
  public void testUpdateAssessment_simpleSuccessArtifactsDestinationChange_shouldSucceed() {
    final ResourceModel previousResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    final ResourceModel currentResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    currentResourceModel.getAssessmentReportsDestination().setDestination(ASSESSMENT_REPORT_DESTINATION);

    sendRequestAndVerifyResponses(previousResourceModel, currentResourceModel);
  }

  @Test
  public void testUpdateAssessment_simpleSuccessRolesChange_shouldSucceed() {
    final ResourceModel previousResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    final ResourceModel currentResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    currentResourceModel.getRoles().get(0).setRoleArn(USER_ARN_UPDATED);

    sendRequestAndVerifyResponses(previousResourceModel, currentResourceModel);
  }

  @Test
  public void testUpdateAssessment_simpleSuccessScopeChange_shouldSucceed() {
    final ResourceModel previousResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    final ResourceModel currentResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    currentResourceModel.getScope().getAwsServices().get(0).setServiceName("EC2");

    sendRequestAndVerifyResponses(previousResourceModel, currentResourceModel);
  }

  @Test
  public void testUpdateAssessment_failedUpdate_shouldThrowException() {
    final ResourceModel previousResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    final ResourceModel currentResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(currentResourceModel)
        .previousResourceState(previousResourceModel)
        .build();

    when(proxyClient.client().updateAssessment(any(UpdateAssessmentRequest.class)))
        .thenThrow(InternalServerException.class);
    assertThrows(CfnServiceInternalErrorException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().updateAssessment(any(UpdateAssessmentRequest.class)))
        .thenThrow(AuditManagerException.class);
    assertThrows(CfnServiceInternalErrorException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().updateAssessment(any(UpdateAssessmentRequest.class)))
        .thenThrow(ResourceNotFoundException.class);
    assertThrows(CfnNotFoundException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    when(proxyClient.client().updateAssessment(any(UpdateAssessmentRequest.class)))
        .thenThrow(ValidationException.class);
    assertThrows(CfnInvalidRequestException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
  }

  @Test
  public void testUpdateAssessment_multipleUpdates_shouldThrowInvalidRequest() {
    final ResourceModel previousResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    final ResourceModel currentResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    currentResourceModel.setName(ASSESSMENT_NAME_UPDATED);
    currentResourceModel.setStatus(INACTIVE_ASSESSMENT_STATUS);

    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(currentResourceModel)
        .previousResourceState(previousResourceModel)
        .build();

    assertThrows(CfnInvalidRequestException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    verifyZeroInteractions(sdkClient);
  }

  @Test
  public void testUpdateAssessment_completedAssessment_shouldThrowInvalidRequest() {
    final ResourceModel previousResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    previousResourceModel.setStatus(INACTIVE_ASSESSMENT_STATUS);
    final ResourceModel currentResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    currentResourceModel.setName("newName");

    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(currentResourceModel)
        .previousResourceState(previousResourceModel)
        .build();

    assertThrows(CfnInvalidRequestException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    verifyZeroInteractions(sdkClient);
  }

  @Test
  public void testUpdateAssessment_nonUpdateableFields_shouldThrowInvalidRequest() {
    final ResourceModel previousResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));
    final ResourceModel currentResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, null));

    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(currentResourceModel)
        .previousResourceState(previousResourceModel)
        .build();

    currentResourceModel.setAssessmentId("test");
    assertThrows(CfnNotUpdatableException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

    currentResourceModel.setAssessmentId(previousResourceModel.getAssessmentId());

    currentResourceModel.setFrameworkId("test");
    assertThrows(CfnNotUpdatableException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    verifyZeroInteractions(sdkClient);
    currentResourceModel.setFrameworkId(previousResourceModel.getFrameworkId());

    currentResourceModel.setArn("test");
    assertThrows(CfnNotUpdatableException.class, () ->
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    verifyZeroInteractions(sdkClient);
    currentResourceModel.setArn(previousResourceModel.getArn());
  }

  private void sendRequestAndVerifyResponses(final ResourceModel previousModel, final ResourceModel currentModel) {
    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(currentModel)
        .previousResourceState(previousModel)
        .build();
    final AssessmentMetadata currentModelMetadata = AssessmentMetadata.builder()
        .complianceType(COMPLIANCE_STANDARD)
        .creationTime(new Date(CREATION_TIME).toInstant())
        .assessmentReportsDestination(cfnAssessmentReportsDestinationToSdkAssessmentReportsDestination
            (currentModel.getAssessmentReportsDestination()))
        .roles(cfnRoleToSdkRoles(currentModel.getRoles()))
        .scope(cfnScopeToSdkScope(currentModel.getScope()))
        .name(currentModel.getName())
        .description(currentModel.getDescription())
        .delegations(makeDelegations(0))
        .id(ASSESSMENT_ID)
        .status(currentModel.getStatus())
        .build();

    final Assessment updatedAssessment = makeAssessment(null, null, currentModelMetadata);
    final UpdateAssessmentStatusResponse updateAssessmentStatusResponse =
        UpdateAssessmentStatusResponse.builder().assessment(updatedAssessment).build();
    final UpdateAssessmentResponse updateAssessmentResponse =
        UpdateAssessmentResponse.builder()
            .assessment(updatedAssessment)
            .build();
    if (currentModel.getStatus().equals(INACTIVE_ASSESSMENT_STATUS)) {
      when(proxyClient.client().updateAssessmentStatus(any(UpdateAssessmentStatusRequest.class)))
          .thenReturn(updateAssessmentStatusResponse);
    } else {
      when(proxyClient.client().updateAssessment(any(UpdateAssessmentRequest.class)))
          .thenReturn(updateAssessmentResponse);
    }

    final ProgressEvent<ResourceModel, CallbackContext> response
        = handler.handleRequest(proxy, request, null, proxyClient, logger);
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);

    assertThat(response.getResourceModel().getAssessmentId()).isEqualTo(ASSESSMENT_ID);
    assertThat(response.getResourceModel().getArn()).isEqualTo(ASSESSMENT_ARN);
    assertThat(response.getResourceModel().getArn()).isEqualTo(ASSESSMENT_ARN);
    if (currentModel.getTags() == null) {
      assertNull(response.getResourceModel().getTags());
    } else {
      assertEquals(currentModel.getTags(), response.getResourceModel().getTags());
    }
    assertThat(response.getResourceModel().getFrameworkId()).isEqualTo(FRAMEWORK_ID);
    assertThat(response.getResourceModel().getAssessmentReportsDestination()).
        isEqualTo(currentModel.getAssessmentReportsDestination());
    assertThat(response.getResourceModel().getScope()).isEqualTo(currentModel.getScope());
    assertThat(response.getResourceModel().getRoles()).isEqualTo(currentModel.getRoles());
    assertThat(response.getResourceModel().getName()).isEqualTo(currentModel.getName());
    assertThat(response.getResourceModel().getDescription()).isEqualTo(currentModel.getDescription());
    AssessmentMetadataUtilsTest.validateCfnDelegation(response.getResourceModel().getDelegations());

    assertThat(response.getResourceModels()).isNull();
    assertThat(response.getMessage()).isNull();
    assertThat(response.getErrorCode()).isNull();
    if (currentModel.getStatus().equals(INACTIVE_ASSESSMENT_STATUS)) {
      verify(proxyClient.client(), times(1)).updateAssessmentStatus(any(UpdateAssessmentStatusRequest.class));
      verify(proxyClient.client(), times(0)).updateAssessment(any(UpdateAssessmentRequest.class));
    } else {
      verify(proxyClient.client(), times(1)).updateAssessment(any(UpdateAssessmentRequest.class));
      verify(proxyClient.client(), times(0)).updateAssessmentStatus(any(UpdateAssessmentStatusRequest.class));
    }
  }
}
