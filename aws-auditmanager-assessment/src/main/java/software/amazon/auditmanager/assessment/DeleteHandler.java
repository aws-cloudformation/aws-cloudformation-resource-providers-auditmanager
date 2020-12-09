package software.amazon.auditmanager.assessment;

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
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.awssdk.services.auditmanager.model.DeleteAssessmentRequest;
import software.amazon.awssdk.services.auditmanager.model.DeleteAssessmentResponse;
import software.amazon.awssdk.services.auditmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class DeleteHandler extends BaseHandlerStd {

  private Logger logger;

  protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
      final AmazonWebServicesClientProxy proxy,
      final ResourceHandlerRequest<ResourceModel> request,
      final CallbackContext callbackContext,
      final ProxyClient<AuditManagerClient> proxyClient,
      final Logger logger) {

    this.logger = logger;

    final ResourceModel model = request.getDesiredResourceState();
    return deleteAssessmentAndUpdateProgress(model, callbackContext, proxy, proxyClient);
  }

  private ProgressEvent<ResourceModel, CallbackContext> deleteAssessmentAndUpdateProgress
      (final ResourceModel model, final CallbackContext callbackContext, final AmazonWebServicesClientProxy proxy,
       final ProxyClient<AuditManagerClient> proxyClient) {
    final DeleteAssessmentRequest deleteAssessmentRequest =
        DeleteAssessmentRequest.builder().assessmentId(model.getAssessmentId()).build();
    try {
      proxy.injectCredentialsAndInvokeV2(deleteAssessmentRequest, proxyClient.client()::deleteAssessment);;
    } catch (AwsServiceException e) {
      throw ExceptionTranslator.translateToCfnException(e, model.getAssessmentId());
    }
    return ProgressEvent.defaultSuccessHandler(null);
  }
}
