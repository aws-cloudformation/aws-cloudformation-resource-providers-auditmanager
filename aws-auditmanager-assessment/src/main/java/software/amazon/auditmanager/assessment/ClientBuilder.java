package software.amazon.auditmanager.assessment;

import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import java.net.URI;
import software.amazon.cloudformation.LambdaWrapper;
import software.amazon.awssdk.regions.Region;

public class ClientBuilder {
    public static AuditManagerClient getClient() {
        return AuditManagerClient.builder().httpClient(LambdaWrapper.HTTP_CLIENT).build();
    }

    public static AuditManagerClient getClient(final String region) {
        return AuditManagerClient
            .builder()
            .region(Region.of(region))
            .build();
    }
}
