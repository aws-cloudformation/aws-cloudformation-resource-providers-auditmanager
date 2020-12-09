package software.amazon.auditmanager.assessment;

import static software.amazon.auditmanager.assessment.Utils.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.auditmanager.model.AssessmentMetadata;
import software.amazon.awssdk.services.auditmanager.model.AssessmentFramework;
import software.amazon.awssdk.services.auditmanager.model.Scope;
import software.amazon.awssdk.services.auditmanager.model.AWSService;
import software.amazon.awssdk.services.auditmanager.model.AWSAccount;
import software.amazon.awssdk.services.auditmanager.model.Role;
import software.amazon.awssdk.services.auditmanager.model.AssessmentReportsDestination;
import software.amazon.awssdk.services.auditmanager.model.Assessment;
import software.amazon.awssdk.services.auditmanager.model.Delegation;

public class AssessmentMetadataUtils {
  /************************** CFN AssessmentMetadata to AssessmentMetadata **************************/

  public static List<Role> cfnRoleToSdkRoles(final List<software.amazon.auditmanager.assessment.Role> cfnRoles) {
    if (cfnRoles == null) return null;
    return cfnRoles.stream()
        .map(role -> Role.builder().roleArn(role.getRoleArn()).roleType(role.getRoleType()).build())
        .collect(Collectors.toList());
  }

  public static Scope cfnScopeToSdkScope(software.amazon.auditmanager.assessment.Scope cfnScope) {
    if (cfnScope == null) return null;
    return Scope.builder()
        .awsServices(cfnServicesToSdkServices(cfnScope.getAwsServices()))
        .awsAccounts(cfnServicesToSdkAccount(cfnScope.getAwsAccounts()))
        .build();
  }

  public static AssessmentReportsDestination cfnAssessmentReportsDestinationToSdkAssessmentReportsDestination(
      final software.amazon.auditmanager.assessment.AssessmentReportsDestination cfnAssessmentReportsDestination) {
    if (cfnAssessmentReportsDestination == null) return null;
    return AssessmentReportsDestination.builder()
        .destinationType(cfnAssessmentReportsDestination.getDestinationType())
        .destination(cfnAssessmentReportsDestination.getDestination())
        .build();
  }

  private static List<AWSAccount> cfnServicesToSdkAccount(
      final List<software.amazon.auditmanager.assessment.AWSAccount> cfnAWSAccounts) {
    if (cfnAWSAccounts == null) return null;
    return cfnAWSAccounts.stream()
        .map(account -> AWSAccount.builder()
            .name(account.getName())
            .emailAddress(account.getEmailAddress())
            .id(account.getId()).build())
        .collect(Collectors.toList());
  }

  public static List<Delegation> cfnDelegationToSdkDelegation(
      final List<software.amazon.auditmanager.assessment.Delegation> cfnDelegations) {
    if (cfnDelegations == null || cfnDelegations.isEmpty()) return null;
    return cfnDelegations.stream().map(cfnDelegation -> Delegation.builder()
        .id(cfnDelegation.getId())
        .assessmentName(cfnDelegation.getAssessmentName())
        .createdBy(cfnDelegation.getCreatedBy())
        .assessmentId(cfnDelegation.getAssessmentId())
        .status(cfnDelegation.getStatus())
        .roleType(cfnDelegation.getRoleType())
        .roleArn(cfnDelegation.getRoleArn())
        .creationTime(translateDoubleToInstant(cfnDelegation.getCreationTime()))
        .lastUpdated(translateDoubleToInstant(cfnDelegation.getLastUpdated()))
        .controlSetId(cfnDelegation.getControlSetId())
        .comment(cfnDelegation.getComment())
        .build())
        .collect(Collectors.toList());
  }

  private static List<AWSService> cfnServicesToSdkServices(
      final List<software.amazon.auditmanager.assessment.AWSService> cfnAWSServices) {
    if (cfnAWSServices == null) return null;
    return cfnAWSServices.stream()
        .map(service -> AWSService.builder().serviceName(service.getServiceName()).build())
        .collect(Collectors.toList());
  }

  /************************** AssessmentMetadata to CFN AssessmentMetadata **************************/
  public static List<software.amazon.auditmanager.assessment.Delegation> transformToCfnDelegations(
      final List<Delegation> delegations) {
    if (delegations == null || delegations.isEmpty()) return null;
    return delegations.stream().map(delegation -> software.amazon.auditmanager.assessment.Delegation.builder()
        .id(delegation.id())
        .assessmentName(delegation.assessmentName())
        .createdBy(delegation.createdBy())
        .assessmentId(delegation.assessmentId())
        .status(delegation.statusAsString())
        .roleType(delegation.roleTypeAsString())
        .roleArn(delegation.roleArn())
        .creationTime(translateInstantToDouble(delegation.creationTime()))
        .lastUpdated(translateInstantToDouble(delegation.lastUpdated()))
        .controlSetId(delegation.controlSetId())
        .comment(delegation.comment())
        .build())
        .collect(Collectors.toList());
  }

  public static software.amazon.auditmanager.assessment.AWSAccount transformToCfnAWSAccount(
      final AWSAccount awsAccount) {
    if (awsAccount == null) return null;
    return software.amazon.auditmanager.assessment.AWSAccount.builder()
        .name(awsAccount.name())
        .emailAddress(awsAccount.emailAddress())
        .id(awsAccount.id())
        .build();
  }

  public static List<software.amazon.auditmanager.assessment.Role> transformToCfnRoles(
      final List<Role> roles) {
    if (roles == null) return null;
    return roles.stream().map(role -> software.amazon.auditmanager.assessment.Role.builder()
        .roleType(role.roleTypeAsString())
        .roleArn(role.roleArn())
        .build()).collect(Collectors.toList());
  }

  public static software.amazon.auditmanager.assessment.AssessmentReportsDestination transformToCfnAssessmentReportsDestination(
      final AssessmentReportsDestination assessmentReportsDestination) {
    if (assessmentReportsDestination == null) return null;
    return software.amazon.auditmanager.assessment.AssessmentReportsDestination.builder()
        .destinationType(assessmentReportsDestination.destinationTypeAsString())
        .destination(assessmentReportsDestination.destination())
        .build();
  }

  public static software.amazon.auditmanager.assessment.Scope transformToCfnScope(final Scope scope) {
    if (scope == null) return null;
    return software.amazon.auditmanager.assessment.Scope.builder()
        .awsServices(transformToCfnServices(scope.awsServices()))
        .awsAccounts(transformToCfnAccounts(scope.awsAccounts()))
        .build();
  }

  private static List<software.amazon.auditmanager.assessment.AWSService> transformToCfnServices(
      final List<AWSService> awsServices) {
    if (awsServices == null) return null;
    return awsServices.stream()
        .map(service -> software.amazon.auditmanager.assessment.AWSService.builder()
            .serviceName(service.serviceName()).build())
        .collect(Collectors.toList());
  }

  private static List<software.amazon.auditmanager.assessment.AWSAccount> transformToCfnAccounts(
      final List<AWSAccount> awsAccounts) {
    if (awsAccounts == null) return null;
    return awsAccounts.stream()
        .map(account -> {
          return (software.amazon.auditmanager.assessment.AWSAccount)transformToCfnAWSAccount(account);
        })
        .collect(Collectors.toList());
  }
}
