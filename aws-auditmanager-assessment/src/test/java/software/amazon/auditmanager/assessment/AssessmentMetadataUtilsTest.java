package software.amazon.auditmanager.assessment;

import static software.amazon.auditmanager.assessment.AbstractTestBase.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.auditmanager.model.Delegation;
import software.amazon.awssdk.services.auditmanager.model.Role;
import software.amazon.awssdk.services.auditmanager.model.AssessmentReportsDestination;
import software.amazon.awssdk.services.auditmanager.model.Scope;
import software.amazon.awssdk.services.auditmanager.model.Delegation;
import software.amazon.auditmanager.assessment.AssessmentMetadataUtils;

@ExtendWith(MockitoExtension.class)
public class AssessmentMetadataUtilsTest extends AbstractTestBase {

  @Test
  public void testRoleTransformation_withValidRoles_shouldTransform() {
    List<Role> expectedSdkRoles = makeRoles();
    List<software.amazon.auditmanager.assessment.Role> expectedCfnRoles = makeCfnRoles();

    List<software.amazon.auditmanager.assessment.Role> cfnRoles = AssessmentMetadataUtils.transformToCfnRoles(expectedSdkRoles);
    assertEquals(expectedCfnRoles, cfnRoles);

    List<Role> roles = AssessmentMetadataUtils.cfnRoleToSdkRoles(expectedCfnRoles);
    assertEquals(expectedSdkRoles, roles);
  }

  @Test
  public void testAssessmentReportsDestinationTransformation_withValidAssessmentReportsDestination_shouldTransform() {
    AssessmentReportsDestination expectedAssessmentReportsDestination = makeAssessmentReportsDestination();
    software.amazon.auditmanager.assessment.AssessmentReportsDestination expectedCfnAssessmentReportsDestination =
        makeCfnAssessmentReportsDestination();

    assertEquals(expectedAssessmentReportsDestination,
        AssessmentMetadataUtils.cfnAssessmentReportsDestinationToSdkAssessmentReportsDestination
            (expectedCfnAssessmentReportsDestination));

    assertEquals(expectedCfnAssessmentReportsDestination,
        AssessmentMetadataUtils.transformToCfnAssessmentReportsDestination
            (expectedAssessmentReportsDestination));
  }

  @Test
  public void testScopeTransformation_withValidScope_shouldTransform() {
    Scope expectedScope = makeScope();
    software.amazon.auditmanager.assessment.Scope expectedCfnScope = makeCfnScope();

    assertEquals(expectedScope, AssessmentMetadataUtils.cfnScopeToSdkScope(expectedCfnScope));
    assertEquals(expectedCfnScope, AssessmentMetadataUtils.transformToCfnScope(expectedScope));
  }

  @Test
  public void testDelegationsTransformation_withValidDelegations_shouldTransform() {
    List<Delegation> expectedSdkDelegations = makeDelegations(0);
    List<software.amazon.auditmanager.assessment.Delegation> expectedCfnDelegations = makeCfnDelegations(0);

    List<software.amazon.auditmanager.assessment.Delegation> cfnDelegations =
        AssessmentMetadataUtils.transformToCfnDelegations(expectedSdkDelegations);
    validateCfnDelegation(cfnDelegations);

    List<Delegation> delegations = AssessmentMetadataUtils.cfnDelegationToSdkDelegation(expectedCfnDelegations);
    validateDelegation(delegations);
  }

  public static void validateCfnDelegation(final List<software.amazon.auditmanager.assessment.Delegation> delegations) {
    delegations.stream().forEach(delegation -> {
      assertEquals(DELEGATION_ID_1, delegation.getId());
      assertEquals(ASSESSMENT_NAME, delegation.getAssessmentName());
      assertEquals(ASSESSMENT_ID, delegation.getAssessmentId());
      assertEquals(DELEGATION_STATUS, delegation.getStatus());
      assertEquals(PROCESS_OWNER, delegation.getRoleType());
      assertEquals(CONTROL_SET_ID + 0, delegation.getControlSetId());
      assertEquals(USER_ARN, delegation.getRoleArn());
    });
  }

  private static void validateDelegation(final List<Delegation> delegations) {
    delegations.stream().forEach(delegation -> {
      assertEquals(DELEGATION_ID_1, delegation.id());
      assertEquals(ASSESSMENT_NAME, delegation.assessmentName());
      assertEquals(ASSESSMENT_ID, delegation.assessmentId());
      assertEquals(DELEGATION_STATUS, delegation.statusAsString());
      assertEquals(PROCESS_OWNER, delegation.roleTypeAsString());
      assertEquals(CONTROL_SET_ID + 0, delegation.controlSetId());
      assertEquals(USER_ARN, delegation.roleArn());
    });
  }
}
