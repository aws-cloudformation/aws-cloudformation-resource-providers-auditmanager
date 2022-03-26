package software.amazon.auditmanager.assessment;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
import software.amazon.awssdk.services.auditmanager.model.Assessment;
import software.amazon.awssdk.services.auditmanager.model.UpdateAssessmentRequest;
import software.amazon.awssdk.services.auditmanager.model.UpdateAssessmentResponse;
import software.amazon.awssdk.services.auditmanager.model.UpdateAssessmentStatusRequest;
import software.amazon.awssdk.services.auditmanager.model.UpdateAssessmentStatusResponse;
import software.amazon.awssdk.services.auditmanager.model.AssessmentStatus;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.util.Optional;

public class UpdateHandler extends BaseHandlerStd {

  public static final String INACTIVE_ASSESSMENT_STATUS = "INACTIVE";
  protected static final String INACTIVE_ASSESSMENT_ERROR_MESSAGE = "Assessment already in INACTIVE state";
  protected static final String MULTIPLE_UPDATES_ERROR_MESSAGE =
      "Updating assessment metadata and status cannot be coupled";

  private Logger logger;

  @Override
  public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
      final AmazonWebServicesClientProxy proxy,
      final ResourceHandlerRequest<ResourceModel> request,
      final CallbackContext callbackContext,
      final ProxyClient<AuditManagerClient> proxyClient,
      final Logger logger) {
    this.logger = logger;

    final ResourceModel currentModel = request.getDesiredResourceState();
    final ResourceModel previousModel = request.getPreviousResourceState();

    /* Update-able fields are status of assessment, name, description
     * scope, roles, assessment reports destination
     */
    verifyNonUpdatableFields(currentModel, previousModel);
    verifyAssessmentStatus(currentModel, previousModel);

    try {
      final Assessment outputAssessment = updateAssessment(currentModel, proxy, proxyClient);
      return ProgressEvent.<ResourceModel, CallbackContext>builder()
          .resourceModel(Utils.transformToAssessmentResourceModel(currentModel, outputAssessment))
          .status(OperationStatus.SUCCESS)
          .build();
    } catch (AwsServiceException e) {
      return ExceptionTranslator.translateToCfnException(e, currentModel.getAssessmentId());
    }

  }

  private Assessment updateAssessment(
      final ResourceModel currentModel,
      final AmazonWebServicesClientProxy proxy,
      final ProxyClient<AuditManagerClient> proxyClient) {
    if (currentModel != null
        && currentModel.getStatus() != null
        && currentModel.getStatus().equals(INACTIVE_ASSESSMENT_STATUS)) {
      final UpdateAssessmentStatusRequest updateAssessmentStatusRequest = UpdateAssessmentStatusRequest.builder()
          .assessmentId(currentModel.getAssessmentId())
          .status(AssessmentStatus.INACTIVE)
          .build();
      UpdateAssessmentStatusResponse updateAssessmentStatusResponse =
          proxy.injectCredentialsAndInvokeV2(updateAssessmentStatusRequest,
              proxyClient.client()::updateAssessmentStatus);
      return updateAssessmentStatusResponse.assessment();

    } else {
      final UpdateAssessmentRequest updateAssessmentRequest = UpdateAssessmentRequest.builder()
          .assessmentId(currentModel.getAssessmentId())
          .scope(AssessmentMetadataUtils.cfnScopeToSdkScope(currentModel.getScope()))
          //optional attributes
          .assessmentDescription((currentModel.getDescription() != null) ? currentModel.getDescription() : null)
          .assessmentName((currentModel.getName() != null) ? currentModel.getName() : null)
          .roles((currentModel.getRoles() != null) ?
              AssessmentMetadataUtils.cfnRoleToSdkRoles(currentModel.getRoles()) : null)
          .build();

      UpdateAssessmentResponse updateAssessmentResponse =
          proxy.injectCredentialsAndInvokeV2(updateAssessmentRequest, proxyClient.client()::updateAssessment);
      return updateAssessmentResponse.assessment();
    }
  }

  private void verifyAssessmentStatus(final ResourceModel currentModel, final ResourceModel previousModel) {
    if (previousModel != null
        && previousModel.getStatus() != null
        && previousModel.getStatus().toString().equals(INACTIVE_ASSESSMENT_STATUS)) {
      throw new CfnInvalidRequestException(INACTIVE_ASSESSMENT_ERROR_MESSAGE);
    }
    if (currentModel != null
        && previousModel != null
        && currentModel.getStatus() != null
        && currentModel.getStatus().toString().equals(INACTIVE_ASSESSMENT_STATUS)
        && ((!Optional.ofNullable(currentModel.getName())
          .equals(Optional.ofNullable(previousModel.getName())))
          || (!Optional.ofNullable(currentModel.getDescription())
          .equals(Optional.ofNullable(previousModel.getDescription())))
          || (!Optional.ofNullable(currentModel.getScope())
          .equals(Optional.ofNullable(previousModel.getScope())))
          || (!Optional.ofNullable(currentModel.getRoles())
          .equals(Optional.ofNullable(previousModel.getRoles())))
          || (!Optional.ofNullable(currentModel.getAssessmentReportsDestination())
          .equals(Optional.ofNullable(previousModel.getAssessmentReportsDestination()))))) {
      throw new CfnInvalidRequestException(MULTIPLE_UPDATES_ERROR_MESSAGE);
    }
  }

  /**
   * Checks if the create only fields have been updated and throws an exception if it is the case
   *
   * @param currentModel the current resource model
   * @param previousModel the previous resource model
   */
  private void verifyNonUpdatableFields(final ResourceModel currentModel, final ResourceModel previousModel) {
    if (previousModel != null) {
      if (!Optional.ofNullable(currentModel.getCreationTime())
          .equals(Optional.ofNullable(previousModel.getCreationTime()))) {
        throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, "CreationTime");
      }
      if (!Optional.ofNullable(currentModel.getDelegations())
          .equals(Optional.ofNullable(previousModel.getDelegations()))) {
        throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, "Delegations");
      }
      if (!Optional.ofNullable(currentModel.getAwsAccount())
          .equals(Optional.ofNullable(previousModel.getAwsAccount()))) {
        throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, "AWS Account");
      }
      if (!Optional.ofNullable(currentModel.getTags())
          .equals(Optional.ofNullable(previousModel.getTags()))) {
        throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, "Tags");
      }
      if (!Optional.ofNullable(currentModel.getArn())
          .equals(Optional.ofNullable(previousModel.getArn()))) {
        throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, "Assessment ARN");
      }
      if (!Optional.ofNullable(currentModel.getFrameworkId())
          .equals(Optional.ofNullable(previousModel.getFrameworkId()))) {
        throw new CfnNotUpdatableException(ResourceModel.TYPE_NAME, "FrameworkId");
      }
    }
  }
}
