package software.amazon.auditmanager.assessment;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.awssdk.services.auditmanager.model.CreateAssessmentRequest;
import software.amazon.awssdk.services.auditmanager.model.CreateAssessmentResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class CreateHandler extends BaseHandlerStd {

  @Override
  public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
      final AmazonWebServicesClientProxy proxy,
      final ResourceHandlerRequest<ResourceModel> request,
      final CallbackContext callbackContext,
      final ProxyClient<AuditManagerClient> proxyClient,
      final Logger logger) {

    final ResourceModel model = request.getDesiredResourceState();

    if (hasReadOnlyProperties(model)) {
      throw new CfnInvalidRequestException("Attempting to set a ReadOnly Property.");
    }

    CreateAssessmentRequest createAssessmentRequest = Utils.transformToCreateAssessmentRequest(model);
    CreateAssessmentResponse createAssessmentResponse = null;

    try {
      createAssessmentResponse = proxy.injectCredentialsAndInvokeV2(createAssessmentRequest,
          proxyClient.client()::createAssessment);
    } catch (AwsServiceException e) {
      throw ExceptionTranslator.translateToCfnException(e, createAssessmentRequest.name());
    }

    return ProgressEvent.<ResourceModel, CallbackContext>builder()
        .resourceModel(Utils.transformToAssessmentResourceModel(model, createAssessmentResponse.assessment()))
        .status(OperationStatus.SUCCESS)
        .build();
  }

  private boolean hasReadOnlyProperties(final ResourceModel model) {
    return (model.getAssessmentId() != null)
        || (model.getCreationTime() != null)
        || (model.getDelegations() != null)
        || (model.getArn() != null);
  }

}
