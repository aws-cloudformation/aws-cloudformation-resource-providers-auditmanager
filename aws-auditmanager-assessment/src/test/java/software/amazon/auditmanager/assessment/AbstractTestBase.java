package software.amazon.auditmanager.assessment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.google.common.collect.Lists;

import software.amazon.awssdk.services.auditmanager.model.Assessment;
import software.amazon.awssdk.services.auditmanager.model.AssessmentMetadata;
import software.amazon.awssdk.services.auditmanager.model.AssessmentMetadataItem;
import software.amazon.awssdk.services.auditmanager.model.Delegation;
import software.amazon.awssdk.services.auditmanager.model.AssessmentFramework;
import software.amazon.awssdk.services.auditmanager.model.FrameworkMetadata;
import software.amazon.awssdk.services.auditmanager.model.Delegation;
import software.amazon.awssdk.services.auditmanager.model.DelegationStatus;
import software.amazon.awssdk.services.auditmanager.model.Role;
import software.amazon.awssdk.services.auditmanager.model.Scope;
import software.amazon.awssdk.services.auditmanager.model.AWSAccount;
import software.amazon.awssdk.services.auditmanager.model.AWSService;
import software.amazon.awssdk.services.auditmanager.model.AssessmentReportsDestination;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AbstractTestBase {

  public static final Map<String, String> TAGS_TO_ADD = Collections.unmodifiableMap(new HashMap<String, String>() {
    {
      put("key1", "value1");
      put("key2", "value2");
    }
  });
  public static final String ASSESSMENT_NAME = "assessmentName";
  public static final String ASSESSMENT_NAME_UPDATED = "assessmentNameUpdated";
  public static final String ASSESSMENT_DESCRIPTION = "assessment description";
  public static final String ASSESSMENT_DESCRIPTION_UPDATED = "assessment description updated";
  public static final String ASSESSMENT_ID = "61e675f6-b6b5-4db8-a1be-74e7a5792312";
  public static final String FRAMEWORK_ID = "B27E48646A20333342C8BCEA9C201A59";
  public static final String ASSESSMENT_ARN =
      "arn:aws:bedrock:us-west-2:111111111111:assessment/" + ASSESSMENT_ID;
  public static final String FRAMEWORK_ARN =
      "arn:aws:bedrock:us-west-2:111111111111:assessmentFramework/" + FRAMEWORK_ID;
  protected static final String USER_ARN = "arn:aws:iam::111111111111:user/testUser";
  protected static final String USER_ARN_UPDATED = "arn:aws:iam::111111111111:user/testUserUpdated";
  protected static final String CONTROL_SET_ID = "ControlSetId-";

  //Assessment template
  protected static final String FRAMEWORK_NAME = "templateName";
  protected static final String FRAMEWORK_DESCRIPTION = "template description";
  protected static final String COMPLIANCE_STANDARD = "CIS";
  protected static final long CREATION_TIME = 1234567891L;
  protected static final long LAST_UPDATED = 2323223232L;

  protected static final String DELEGATION_ID_1 = "DelegationID1";
  protected static final String PROCESS_OWNER = "PROCESS_OWNER";
  protected static final String RESOURCE_OWNER = "RESOURCE_OWNER";
  protected static final int SYSTEM_EVIDENCE_COUNT = 1;
  protected static final int MANUAL_EVIDENCE_COUNT = 5;
  protected static final String DELEGATION_STATUS = "IN_PROGRESS";
  protected static final String ASSESSMENT_STATUS = "ACTIVE";
  protected static final String ASSESSMENT_REPORT_DESTINATION_TYPE = "S3";
  protected static final String ASSESSMENT_REPORT_DESTINATION = "s3://testBucket";
  protected static final String ASSESSMENT_REPORT_DESTINATION_UPDATED = "s3://testBucketUpdated";
  protected static final String SERVICE_NAME = "S3";
  protected static final String ACCOUNT_ID = "111111111111";
  protected static final String ACCOUNT_NAME = "testName";
  protected static final String ACCOUNT_EMAIL_ADDRESS = "test@gmail.com";
  protected static final Credentials MOCK_CREDENTIALS;
  protected static final LoggerProxy logger;

  static {
    MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    logger = new LoggerProxy();
  }

  public static ProxyClient<AuditManagerClient> MOCK_PROXY(
      final AmazonWebServicesClientProxy proxy,
      final AuditManagerClient sdkClient) {
    return new ProxyClient<AuditManagerClient>() {

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
      injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
        return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> CompletableFuture<ResponseT>
      injectCredentialsAndInvokeV2Async(
          RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
        throw new UnsupportedOperationException();
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>> IterableT
      injectCredentialsAndInvokeIterableV2(RequestT requestT, Function<RequestT, IterableT> function) {
        throw new UnsupportedOperationException();
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
      injectCredentialsAndInvokeV2InputStream(RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
        throw new UnsupportedOperationException();
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
      injectCredentialsAndInvokeV2Bytes(RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
        throw new UnsupportedOperationException();
      }

      @Override
      public AuditManagerClient client() {
        return sdkClient;
      }
    };
  }

  public static Map<String, String> makeTags() {
    return TAGS_TO_ADD;
  }

  public static AssessmentFramework makeAssessmentFramework() {
    return AssessmentFramework.builder()
        .id(FRAMEWORK_ID)
        .arn(FRAMEWORK_ARN)
        .build();
  }

  public static AssessmentMetadata makeAssessmentMetadata() {
    return AssessmentMetadata.builder()
        .complianceType(COMPLIANCE_STANDARD)
        .creationTime(new Date(CREATION_TIME).toInstant())
        .assessmentReportsDestination(makeAssessmentReportsDestination())
        .roles(makeRoles())
        .scope(makeScope())
        .name(ASSESSMENT_NAME)
        .description(ASSESSMENT_DESCRIPTION)
        .delegations(makeDelegations(0))
        .id(ASSESSMENT_ID)
        .status(ASSESSMENT_STATUS)
        .build();
  }

  public static AssessmentMetadataItem makeAssessmentMetadataItem() {
    return AssessmentMetadataItem.builder()
        .complianceType(COMPLIANCE_STANDARD)
        .roles(makeRoles())
        .name(ASSESSMENT_NAME)
        .delegations(makeDelegations(0))
        .id(ASSESSMENT_ID)
        .status(ASSESSMENT_STATUS)
        .build();
  }

  public Assessment makeAssessment(final List<Delegation> delegations, final Map<String, String> tags) {
    return Assessment.builder()
        .tags(tags)
        .arn(ASSESSMENT_ARN)
        .metadata(makeAssessmentMetadata())
        .framework(makeAssessmentFramework())
        .awsAccount(AWSAccount.builder()
            .id(ACCOUNT_ID)
            .emailAddress(ACCOUNT_EMAIL_ADDRESS)
            .name(ACCOUNT_NAME)
            .build())
        .build();
  }

  public Assessment makeAssessment(final List<Delegation> delegations,
                                   final Map<String, String> tags,
                                   final AssessmentMetadata assessmentMetadata) {
    return Assessment.builder()
        .tags(tags)
        .arn(ASSESSMENT_ARN)
        .metadata(assessmentMetadata)
        .framework(makeAssessmentFramework())
        .awsAccount(AWSAccount.builder()
            .id(ACCOUNT_ID)
            .emailAddress(ACCOUNT_EMAIL_ADDRESS)
            .name(ACCOUNT_NAME)
            .build())
        .build();
  }

  public static List<Role> makeRoles() {
    final List<Role> roles = Lists.newArrayList();
    final Role processOwner = Role.builder().roleType(PROCESS_OWNER)
        .roleArn(USER_ARN).build();
    final Role ResourceOwner = Role.builder().roleType(RESOURCE_OWNER)
        .roleArn(USER_ARN).build();
    roles.add(processOwner);
    roles.add(ResourceOwner);
    return roles;
  }

  public static List<software.amazon.auditmanager.assessment.Role> makeCfnRoles() {
    final List<software.amazon.auditmanager.assessment.Role> cfnRoles = Lists.newArrayList();
    final software.amazon.auditmanager.assessment.Role processOwner =
        software.amazon.auditmanager.assessment.Role.builder()
            .roleType(PROCESS_OWNER)
            .roleArn(USER_ARN)
            .build();
    final software.amazon.auditmanager.assessment.Role ResourceOwner =
        software.amazon.auditmanager.assessment.Role.builder()
            .roleType(RESOURCE_OWNER)
            .roleArn(USER_ARN)
            .build();
    cfnRoles.add(processOwner);
    cfnRoles.add(ResourceOwner);
    return cfnRoles;
  }

  public static List<Delegation> makeDelegations(final int controlSetId) {
    final List<Delegation> delegations = Lists.newArrayList();
    final Delegation delegation = Delegation.builder()
        .id(DELEGATION_ID_1)
        .assessmentName(ASSESSMENT_NAME)
        .assessmentId(ASSESSMENT_ID)
        .status(DelegationStatus.IN_PROGRESS)
        .roleType(PROCESS_OWNER)
        .controlSetId(CONTROL_SET_ID + controlSetId)
        .roleArn(USER_ARN)
        .creationTime(new Date(CREATION_TIME).toInstant())
        .build();
    delegations.add(delegation);
    return delegations;
  }

  public static List<software.amazon.auditmanager.assessment.Delegation> makeCfnDelegations(final int controlSetId) {
    final List<software.amazon.auditmanager.assessment.Delegation> delegations = Lists.newArrayList();
    final software.amazon.auditmanager.assessment.Delegation delegation =
        software.amazon.auditmanager.assessment.Delegation.builder()
            .id(DELEGATION_ID_1)
            .assessmentName(ASSESSMENT_NAME)
            .assessmentId(ASSESSMENT_ID)
            .status(DELEGATION_STATUS)
            .roleType(PROCESS_OWNER)
            .controlSetId(CONTROL_SET_ID + controlSetId)
            .roleArn(USER_ARN)
            .creationTime(Utils.translateInstantToDouble(new Date(CREATION_TIME).toInstant()))
        .build();
    delegations.add(delegation);
    return delegations;
  }

  public static AssessmentReportsDestination makeAssessmentReportsDestination() {
    return AssessmentReportsDestination.builder()
        .destinationType(ASSESSMENT_REPORT_DESTINATION_TYPE)
        .destination(ASSESSMENT_REPORT_DESTINATION)
        .build();
  }

  public static software.amazon.auditmanager.assessment.AssessmentReportsDestination
    makeCfnAssessmentReportsDestination() {
    return software.amazon.auditmanager.assessment.AssessmentReportsDestination.builder()
        .destinationType(ASSESSMENT_REPORT_DESTINATION_TYPE)
        .destination(ASSESSMENT_REPORT_DESTINATION)
        .build();
  }

  public static Scope makeScope() {
    final AWSService awsService = AWSService.builder().serviceName(SERVICE_NAME).build();
    final AWSAccount awsAccount = AWSAccount.builder()
        .id(ACCOUNT_ID)
        .emailAddress(ACCOUNT_EMAIL_ADDRESS)
        .name(ACCOUNT_NAME)
        .build();
    return Scope.builder()
        .awsServices(Lists.newArrayList(awsService))
        .awsAccounts(Lists.newArrayList(awsAccount))
        .build();
  }

  public static software.amazon.auditmanager.assessment.Scope makeCfnScope() {
    final software.amazon.auditmanager.assessment.AWSService awsService =
        software.amazon.auditmanager.assessment.AWSService.builder().serviceName(SERVICE_NAME).build();
    final software.amazon.auditmanager.assessment.AWSAccount awsAccount = makeCfnAccount();
    return software.amazon.auditmanager.assessment.Scope.builder()
        .awsServices(Lists.newArrayList(awsService))
        .awsAccounts(Lists.newArrayList(awsAccount))
        .build();
  }

  public static software.amazon.auditmanager.assessment.AWSAccount makeCfnAccount() {
    return software.amazon.auditmanager.assessment.AWSAccount.builder()
        .id(ACCOUNT_ID)
        .emailAddress(ACCOUNT_EMAIL_ADDRESS)
        .name(ACCOUNT_NAME)
        .build();
  }

  public static ResourceModel makeResourceModel() {
    return ResourceModel.builder()
        .frameworkId(FRAMEWORK_ID)
        .build();
  }
}
