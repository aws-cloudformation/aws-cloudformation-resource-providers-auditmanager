package software.amazon.auditmanager.assessment;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.awssdk.services.auditmanager.model.GetAssessmentRequest;
import software.amazon.awssdk.services.auditmanager.model.GetAssessmentResponse;
import software.amazon.awssdk.services.auditmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.auditmanager.model.AccessDeniedException;
import software.amazon.awssdk.services.auditmanager.model.AuditManagerException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class ReadHandler extends BaseHandlerStd {

  private Logger logger;

  @Override
  public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
      final AmazonWebServicesClientProxy proxy,
      final ResourceHandlerRequest<ResourceModel> request,
      final CallbackContext callbackContext,
      final ProxyClient<AuditManagerClient> proxyClient,
      final Logger logger) {
    final ResourceModel model = request.getDesiredResourceState();
    GetAssessmentRequest getAssessmentRequest = GetAssessmentRequest.builder()
        .assessmentId(model.getAssessmentId()).build();

    GetAssessmentResponse getAssessmentResponse = null;
    try {
      getAssessmentResponse = proxy.injectCredentialsAndInvokeV2(getAssessmentRequest,
          proxyClient.client()::getAssessment);
      logger.log(String.format("%s [%s] retrieved successfully", ResourceModel.TYPE_NAME, model.getAssessmentId()));
    } catch (AwsServiceException e) {
      return ExceptionTranslator.translateToCfnException(e, model.getAssessmentId());
    }
    return ProgressEvent.<ResourceModel, CallbackContext>builder()
        .resourceModel(Utils.transformToAssessmentResourceModel(model, getAssessmentResponse.assessment()))
        .status(OperationStatus.SUCCESS)
        .build();
  }
}
