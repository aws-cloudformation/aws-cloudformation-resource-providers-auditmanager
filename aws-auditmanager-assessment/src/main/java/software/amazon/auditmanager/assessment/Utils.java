package software.amazon.auditmanager.assessment;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.time.format.DateTimeParseException;
import software.amazon.awssdk.services.auditmanager.model.AssessmentMetadata;
import software.amazon.awssdk.services.auditmanager.model.AssessmentFramework;
import software.amazon.awssdk.services.auditmanager.model.Assessment;
import software.amazon.awssdk.services.auditmanager.model.CreateAssessmentRequest;
import software.amazon.awssdk.services.auditmanager.model.ListAssessmentsResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

import java.util.List;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;
import java.time.Instant;

public class Utils {

  public static List<software.amazon.auditmanager.assessment.Tag> sdkTagsToCfnTags(final Map<String, String> tags) {
    if (tags == null || tags.isEmpty()) return null;
    final List<software.amazon.auditmanager.assessment.Tag> cfnTags =
        tags.entrySet().stream()
            .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
            .collect(Collectors.toList());
    return cfnTags;
  }

  public static Map<String, String> cfnTagsToSdkTags(final List<software.amazon.auditmanager.assessment.Tag> tags) {
    Map<String, String> tagsMap = new HashMap<>();
    if(tags == null || tags.isEmpty()) {
      return null;
    }
    for (Tag tag : tags) {
      tagsMap.put(tag.getKey(), tag.getValue());
    }
    return tagsMap;
  }

  public static Instant translateDoubleToInstant(final Double inputDate) {
    if (inputDate == null) return null;
    try {
      return new Date((long) (inputDate * 1000L)).toInstant();
    } catch (final DateTimeParseException e) {
      return new DateTime(inputDate, DateTimeZone.UTC).toDate().toInstant();
    }
  }

  public static Double translateInstantToDouble(final Instant inputDateInstant) {
    if (inputDateInstant == null) return null;
    final Date outputDate = Date.from(inputDateInstant);
    return Double.valueOf(outputDate.getTime()/1000);
  }

  public static ResourceModel transformToAssessmentResourceModel(final ResourceModel inputModel,
                                                                 final Assessment assessment) {
    if (assessment == null || inputModel == null) return null;
    return ResourceModel.builder()
        .tags(sdkTagsToCfnTags(assessment.tags()))
        .arn(assessment.arn())
        .assessmentReportsDestination(
            AssessmentMetadataUtils.transformToCfnAssessmentReportsDestination
            (assessment.metadata().assessmentReportsDestination()))
        .scope(AssessmentMetadataUtils.transformToCfnScope(assessment.metadata().scope()))
        .roles(AssessmentMetadataUtils.transformToCfnRoles(assessment.metadata().roles()))
        .name(assessment.metadata().name())
        .description(assessment.metadata().description())
        .delegations(
            AssessmentMetadataUtils.transformToCfnDelegations(
                assessment.metadata().delegations()))
        .creationTime(translateInstantToDouble(assessment.metadata().creationTime()))
        .status(assessment.metadata().statusAsString())
        .awsAccount(AssessmentMetadataUtils.transformToCfnAWSAccount(assessment.awsAccount()))
        .assessmentId(assessment.metadata().id())
        .frameworkId(assessment.framework().id())
        .build();
  }

  public static CreateAssessmentRequest transformToCreateAssessmentRequest(final ResourceModel resourceModel) {
    if (resourceModel == null) return null;
    final CreateAssessmentRequest createAssessmentRequest = CreateAssessmentRequest.builder()
        .tags(Utils.cfnTagsToSdkTags(resourceModel.getTags()))
        .frameworkId(resourceModel.getFrameworkId())
        .scope(AssessmentMetadataUtils.cfnScopeToSdkScope(resourceModel.getScope()))
        .roles(AssessmentMetadataUtils.cfnRoleToSdkRoles(resourceModel.getRoles()))
        .assessmentReportsDestination(AssessmentMetadataUtils.cfnAssessmentReportsDestinationToSdkAssessmentReportsDestination(
            resourceModel.getAssessmentReportsDestination()))
        .name(resourceModel.getName())
        .description(resourceModel.getDescription())
        .build();
    return createAssessmentRequest;
  }

  public static List<ResourceModel> transformToListAssessmentsResponse(
      final ListAssessmentsResponse listAssessmentsResponse) {
    if (listAssessmentsResponse == null || listAssessmentsResponse.assessmentMetadata() == null) return null;
    return listAssessmentsResponse.assessmentMetadata()
        .stream()
        .map(assessmentMetadata -> ResourceModel.builder()
            .assessmentId(assessmentMetadata.id())
            .roles(
                AssessmentMetadataUtils.transformToCfnRoles(assessmentMetadata.roles()))
            .name(assessmentMetadata.name())
            .delegations(
                AssessmentMetadataUtils.transformToCfnDelegations(assessmentMetadata.delegations()))
            .creationTime(translateInstantToDouble(assessmentMetadata.creationTime()))
            .status(assessmentMetadata.statusAsString())
            .build())
        .collect(Collectors.toList());
  }
}
