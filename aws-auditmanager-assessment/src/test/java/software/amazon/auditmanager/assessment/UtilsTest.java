package software.amazon.auditmanager.assessment;

import static software.amazon.auditmanager.assessment.AbstractTestBase.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static software.amazon.auditmanager.assessment.Utils.sdkTagsToCfnTags;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import software.amazon.awssdk.services.auditmanager.model.Assessment;
import software.amazon.awssdk.services.auditmanager.model.AssessmentMetadata;
import software.amazon.awssdk.services.auditmanager.model.AssessmentMetadataItem;
import software.amazon.awssdk.services.auditmanager.model.CreateAssessmentRequest;
import software.amazon.awssdk.services.auditmanager.model.ListAssessmentsResponse;

@ExtendWith(MockitoExtension.class)
public class UtilsTest extends AbstractTestBase {

  @Test
  public void testTransformToAssessmentResourceModel_withAssessmentNoTags_shouldTransform() {
    final ResourceModel actualResourceModel = Utils.transformToAssessmentResourceModel(makeResourceModel(),
        makeAssessment(null, null));
    validateResourceModel(makeAssessment(null, null), actualResourceModel);
  }

  @Test
  public void testTransformToAssessmentResourceModel_withAssessmentWithTags_shouldTransform() {
    final ResourceModel actualResourceModel = Utils.transformToAssessmentResourceModel(makeResourceModel(),
        makeAssessment(null, TAGS_TO_ADD));
    validateResourceModel(makeAssessment(null, TAGS_TO_ADD), actualResourceModel);
  }

  @Test
  public void testTransformToAssessmentResourceModel_withAssessmentWithDelegations_shouldTransform() {
    final ResourceModel actualResourceModel = Utils.transformToAssessmentResourceModel(makeResourceModel(),
        makeAssessment(makeDelegations(0), null));
    validateResourceModel(makeAssessment(makeDelegations(0), null), actualResourceModel);
  }

  @Test
  public void testTransformToAssessmentResourceModel_withNullAssessment_shouldTransform() {
    assertNull(Utils.transformToAssessmentResourceModel(null, null));
    assertNull(Utils.transformToAssessmentResourceModel(makeResourceModel(), null));
  }

  @Test
  public void testTransformToCreateAssessmentRequest_withNullResourceModel_shouldTransform() {
    assertNull(Utils.transformToCreateAssessmentRequest(null));
  }

  @Test
  public void testTransformToCreateAssessmentRequest_withResourceModel_shouldTransform() {
    final ResourceModel inputResourceModel =
        Utils.transformToAssessmentResourceModel(makeResourceModel(), makeAssessment(null, makeTags()));
    final ResourceModel createAssessmentRequestResourceModel = ResourceModel.builder()
        .tags(inputResourceModel.getTags())
        .scope(inputResourceModel.getScope())
        .roles(inputResourceModel.getRoles())
        .name(inputResourceModel.getName())
        .description(inputResourceModel.getDescription())
        .frameworkId(FRAMEWORK_ID)
        .build();
    final CreateAssessmentRequest createAssessmentRequest =
        Utils.transformToCreateAssessmentRequest(createAssessmentRequestResourceModel);
    assertEquals(makeTags(), createAssessmentRequest.tags());
    assertEquals(FRAMEWORK_ID, createAssessmentRequest.frameworkId());
    assertEquals(ASSESSMENT_NAME, createAssessmentRequest.name());
    assertEquals(ASSESSMENT_DESCRIPTION, createAssessmentRequest.description());
    assertEquals(makeScope(), createAssessmentRequest.scope());
    assertEquals(makeRoles(), createAssessmentRequest.roles());
  }

  @Test
  public void testTransformToListAssessmentsResponse_withListAssessmentsResponse_shouldTransform() {
    List<AssessmentMetadataItem> assessmentMetadataList = Lists.newArrayList(makeAssessmentMetadataItem());
    final ListAssessmentsResponse listAssessmentsResponse =
        ListAssessmentsResponse.builder().assessmentMetadata(assessmentMetadataList).build();
    List<ResourceModel> resourceModels =  Utils.transformToListAssessmentsResponse(listAssessmentsResponse);
    assertEquals(1, resourceModels.size());
    resourceModels.stream().forEach(resourceModel -> {
      assertEquals(ASSESSMENT_ID, resourceModel.getAssessmentId());
      assertEquals(makeCfnRoles(), resourceModel.getRoles());
      assertEquals(ASSESSMENT_STATUS, resourceModel.getStatus());
      assertEquals(ASSESSMENT_NAME, resourceModel.getName());
      AssessmentMetadataUtilsTest.validateCfnDelegation(resourceModel.getDelegations());
      //Other than above all properties of resource model are always null
      assertNull(resourceModel.getTags());
      assertNull(resourceModel.getArn());
      assertNull(resourceModel.getFrameworkId());
      assertNull(resourceModel.getAwsAccount());
      assertNull(resourceModel.getScope());
      assertNull(resourceModel.getDescription());
      assertNull(resourceModel.getAssessmentReportsDestination());
    });
  }

  @Test
  public void testTransformToListAssessmentsResponse_withNullListAssessmentsResponse_shouldTransform() {
    assertNull(Utils.transformToListAssessmentsResponse(null));
  }

  private static void validateResourceModel(final Assessment expectedAssessment,
                                            final ResourceModel actualResourceModel) {
    assertEquals(expectedAssessment.arn(), actualResourceModel.getArn());
    if (expectedAssessment.tags() == null || expectedAssessment.tags().isEmpty()) {
      assertNull(actualResourceModel.getTags());
    } else {
      assertEquals(sdkTagsToCfnTags(expectedAssessment.tags()), actualResourceModel.getTags());
    }
    assertEquals(makeCfnAccount(), actualResourceModel.getAwsAccount());
    assertEquals(ASSESSMENT_ID, actualResourceModel.getAssessmentId());
    assertEquals(FRAMEWORK_ID, actualResourceModel.getFrameworkId());
    assertEquals(makeCfnAssessmentReportsDestination(), actualResourceModel.getAssessmentReportsDestination());
    assertEquals(makeCfnScope(), actualResourceModel.getScope());
    assertEquals(makeCfnRoles(), actualResourceModel.getRoles());
    assertEquals(ASSESSMENT_STATUS, actualResourceModel.getStatus());
    assertEquals(ASSESSMENT_NAME, actualResourceModel.getName());
    assertEquals(ASSESSMENT_DESCRIPTION, actualResourceModel.getDescription());
    AssessmentMetadataUtilsTest.validateCfnDelegation(actualResourceModel.getDelegations());
  }
}
