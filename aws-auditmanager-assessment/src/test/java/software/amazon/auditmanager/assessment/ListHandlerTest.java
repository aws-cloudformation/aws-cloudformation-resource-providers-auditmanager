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

import java.time.Duration;
import com.google.common.collect.Lists;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.auditmanager.model.AssessmentMetadata;
import software.amazon.awssdk.services.auditmanager.model.ListAssessmentsRequest;
import software.amazon.awssdk.services.auditmanager.model.ListAssessmentsResponse;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {


  @Mock
  AuditManagerClient sdkClient;
  private ListHandler handler;
  @Mock
  private AmazonWebServicesClientProxy proxy;
  @Mock
  private ProxyClient<AuditManagerClient> proxyClient;

  @BeforeEach
  public void setup() {
    proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
    sdkClient = mock(AuditManagerClient.class);
    proxyClient = MOCK_PROXY(proxy, sdkClient);
    handler = new ListHandler();
  }

  @Test
  public void handleRequest_SimpleSuccess() {
    final ResourceModel inputResourceModel = ResourceModel.builder().build();
    final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
        .desiredResourceState(inputResourceModel)
        .build();
    final ResourceModel expectedResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, makeTags()));

    final ListAssessmentsResponse listAssessmentsResponse = ListAssessmentsResponse.builder()
        .assessmentMetadata(Lists.newArrayList(makeAssessmentMetadataItem())).build();

    when(proxyClient.client().listAssessments(any(ListAssessmentsRequest.class)))
        .thenReturn(listAssessmentsResponse);
    final ProgressEvent<ResourceModel, CallbackContext> response =
        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    assertThat(response.getCallbackContext()).isNull();
    assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
    assertThat(response.getResourceModel()).isNull();
    assertThat(response.getResourceModels()).isNotNull();
    assertThat(response.getMessage()).isNull();
    assertThat(response.getErrorCode()).isNull();
    assertThat(response.getResourceModels().size()).isEqualTo(1);
    assertThat(response.getResourceModels().get(0).getAssessmentId())
        .isEqualTo(ASSESSMENT_ID);
    assertEquals(ASSESSMENT_NAME, response.getResourceModels().get(0).getName());
    assertEquals(makeCfnRoles(), response.getResourceModels().get(0).getRoles());
    AssessmentMetadataUtilsTest.validateCfnDelegation(response.getResourceModels().get(0).getDelegations());
    assertEquals(ASSESSMENT_STATUS, response.getResourceModels().get(0).getStatus());

    //Other than above all properties of resource model are always null
    assertNull(response.getResourceModels().get(0).getTags());
    assertNull(response.getResourceModels().get(0).getArn());
    assertNull(response.getResourceModels().get(0).getFrameworkId());
    assertNull(response.getResourceModels().get(0).getAwsAccount());
    assertNull(response.getResourceModels().get(0).getScope());
    assertNull(response.getResourceModels().get(0).getDescription());
    assertNull(response.getResourceModels().get(0).getAssessmentReportsDestination());
  }
}
