package ee.taltech.ite4120.blood.operatorregistry;

import ee.taltech.ite4120.blood.operatorregistry.http.HttpOperatorRegistryAdapter;
import ee.taltech.ite4120.blood.operatorregistry.xroad.XRoadOperatorRegistryAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chooses the transport behind {@link OperatorRegistryAdapter} from configuration:
 * {@code blood.operator-registry.mode=http} (default) against
 * {@code blood.operator-registry.url} — locally this application's own imitated
 * register — or {@code xroad}, the forge-xroad skeleton.
 */
@Configuration
public class OperatorRegistryConfig {

    @Bean
    public OperatorRegistryAdapter operatorRegistryAdapter(
            @Value("${blood.operator-registry.mode:http}") String mode,
            @Value("${blood.operator-registry.url:http://localhost:18440/mock-registry}") String url,
            @Value("${blood.operator-registry.xroad.security-server-url:http://localhost:8080}") String ssUrl,
            @Value("${blood.operator-registry.xroad.instance:ee-test}") String instance,
            @Value("${blood.operator-registry.xroad.client-member-class:EDU}") String cMemberClass,
            @Value("${blood.operator-registry.xroad.client-member-code:taltech}") String cMemberCode,
            @Value("${blood.operator-registry.xroad.client-subsystem:ite4120}") String cSubsystem,
            @Value("${blood.operator-registry.xroad.service-member-class:GOV}") String sMemberClass,
            @Value("${blood.operator-registry.xroad.service-member-code:70000310}") String sMemberCode,
            @Value("${blood.operator-registry.xroad.service-subsystem:arireg}") String sSubsystem) {
        if ("xroad".equalsIgnoreCase(mode)) {
            return new XRoadOperatorRegistryAdapter(ssUrl, instance,
                    cMemberClass, cMemberCode, cSubsystem, sMemberClass, sMemberCode, sSubsystem);
        }
        return new HttpOperatorRegistryAdapter(url);
    }
}
