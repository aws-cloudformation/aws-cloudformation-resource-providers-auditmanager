package software.amazon.auditmanager.assessment;

import java.util.stream.Collectors;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import software.amazon.awssdk.services.auditmanager.model.ListAssessmentsRequest;
import software.amazon.awssdk.services.auditmanager.model.ListAssessmentsResponse;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandlerStd {

    private static final Integer MAX_RESULTS = 50;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<AuditManagerClient> proxyClient,
        final Logger logger) {
        final ListAssessmentsRequest listAssessmentsRequest = ListAssessmentsRequest.builder()
            .nextToken(request.getNextToken()).maxResults(MAX_RESULTS).build();
        ListAssessmentsResponse listAssessmentsResponse;
        try {
            listAssessmentsResponse = proxy.injectCredentialsAndInvokeV2(listAssessmentsRequest,
                proxyClient.client()::listAssessments);
        } catch (AwsServiceException e) {
            throw ExceptionTranslator.translateToCfnException(e, "No identifier specified");
        }

        request.setNextToken(listAssessmentsResponse.nextToken());
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(Utils.transformToListAssessmentsResponse(listAssessmentsResponse))
            .nextToken(request.getNextToken())
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
