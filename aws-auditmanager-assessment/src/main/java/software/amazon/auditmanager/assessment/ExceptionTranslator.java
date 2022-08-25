package software.amazon.auditmanager.assessment;

import software.amazon.awssdk.services.auditmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.auditmanager.model.AccessDeniedException;
import software.amazon.awssdk.services.auditmanager.model.AuditManagerException;
import software.amazon.awssdk.services.auditmanager.model.InternalServerException;
import software.amazon.awssdk.services.auditmanager.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.ProgressEvent;
  import software.amazon.cloudformation.proxy.ProgressEvent.ProgressEventBuilder;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class ExceptionTranslator {

  private ExceptionTranslator() {}

  protected static final String AUDIT_MANAGER_NOT_ENABLED_MESSAGE =
    "Please complete AWS Audit Manager setup from home page to enable this action in this account";

  public static ProgressEvent<ResourceModel, CallbackContext>  translateToCfnException(
      final AwsServiceException exception,
      final String identifier) {
    final ProgressEventBuilder<ResourceModel, CallbackContext> builder =
        ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.FAILED);

    if (exception instanceof AccessDeniedException) {
      builder.errorCode(HandlerErrorCode.AccessDenied);
      if (exception.getMessage().contains(AUDIT_MANAGER_NOT_ENABLED_MESSAGE)) {
        builder.message(AUDIT_MANAGER_NOT_ENABLED_MESSAGE);
      }
      return builder.build();
    }
    if (exception instanceof ResourceNotFoundException) {
      throw new CfnNotFoundException(ResourceModel.TYPE_NAME, identifier, exception);
    }
    if (exception instanceof ValidationException) {
      throw new CfnInvalidRequestException(exception);
    } if (exception instanceof AuditManagerException
        || exception instanceof InternalServerException) {
      throw new CfnServiceInternalErrorException(exception);
    }
    throw new CfnGeneralServiceException(exception.getMessage(), exception);

  }
}
