package software.amazon.auditmanager.assessment;

import software.amazon.awssdk.services.auditmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.auditmanager.model.AccessDeniedException;
import software.amazon.awssdk.services.auditmanager.model.AuditManagerException;
import software.amazon.awssdk.services.auditmanager.model.InternalServerException;
import software.amazon.awssdk.services.auditmanager.model.ValidationException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class ExceptionTranslator {

  private ExceptionTranslator() {}

  public static BaseHandlerException translateToCfnException(
      final AwsServiceException exception,
      final String identifier) {
    if (exception instanceof AccessDeniedException) {
      return new CfnAccessDeniedException(ResourceModel.TYPE_NAME, exception);
    }
    if (exception instanceof ResourceNotFoundException) {
      return new CfnNotFoundException(ResourceModel.TYPE_NAME, identifier, exception);
    }
    if (exception instanceof ValidationException) {
      return new CfnInvalidRequestException(exception);
    } if (exception instanceof AuditManagerException
        || exception instanceof InternalServerException) {
      return new CfnServiceInternalErrorException(exception);
    }
    return new CfnGeneralServiceException(exception.getMessage(), exception);
  }
}
