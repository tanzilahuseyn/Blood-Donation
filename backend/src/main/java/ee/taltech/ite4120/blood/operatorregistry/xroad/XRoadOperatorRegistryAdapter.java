package ee.taltech.ite4120.blood.operatorregistry.xroad;

import ee.taltech.ite4120.blood.operatorregistry.OperatorInfo;
import ee.taltech.ite4120.blood.operatorregistry.OperatorRegistryAdapter;
import ee.taltech.ite4120.blood.operatorregistry.OperatorRegistryException;
import ee.taltech.ite4120.blood.operatorregistry.xroad.wire.ArEttevoteRequest;
import ee.taltech.ite4120.blood.operatorregistry.xroad.wire.ArEttevoteResponse;
import java.net.URI;
import java.time.Duration;
import org.helex.forge.xroad.XRoadClient;
import org.helex.forge.xroad.XRoadClientConfig;
import org.helex.forge.xroad.XRoadClientId;
import org.helex.forge.xroad.XRoadException;
import org.helex.forge.xroad.XRoadResponse;
import org.helex.forge.xroad.XRoadServiceId;

/**
 * The REAL X-Road transport, on the published {@code org.helex.forge:forge-xroad}
 * library — a SKELETON by design, like animals' XRoadOwnerRegistryAdapter: it
 * compiles, builds a correct envelope, and fails fast with a clear error when no
 * security server is reachable, which in this course there is not.
 *
 * <p>Activated by {@code blood.operator-registry.mode=xroad}; the identifiers come
 * from {@code blood.operator-registry.xroad.*}.
 */
public class XRoadOperatorRegistryAdapter implements OperatorRegistryAdapter {

    private final XRoadClient client;
    private final XRoadServiceId service;

    public XRoadOperatorRegistryAdapter(String securityServerUrl, String instance,
            String clientMemberClass, String clientMemberCode, String clientSubsystem,
            String serviceMemberClass, String serviceMemberCode, String serviceSubsystem) {
        XRoadClientId clientId =
                new XRoadClientId(instance, clientMemberClass, clientMemberCode, clientSubsystem);
        XRoadClientConfig config = new XRoadClientConfig(
                URI.create(securityServerUrl), clientId, "ITE4120", Duration.ofSeconds(30));
        this.client = new XRoadClient(config);
        // A teaching stand-in shaped like the real business register service.
        this.service = new XRoadServiceId(instance, serviceMemberClass, serviceMemberCode,
                serviceSubsystem, "ettevoteParing", "v1");
    }

    @Override
    public OperatorInfo lookup(String registryCode) {
        try {
            XRoadResponse<ArEttevoteResponse> response =
                    client.send(service, ArEttevoteRequest.of(registryCode), ArEttevoteResponse.class);
            ArEttevoteResponse wire = response.body();
            if (wire == null || wire.registrikood() == null) {
                return null; // the register does not know this organisation — a miss, not a failure
            }
            // The mapper: wire vocabulary → domain vocabulary, in one place.
            return new OperatorInfo(wire.registrikood(), wire.nimi(), wire.aadress());
        } catch (XRoadException e) {
            throw new OperatorRegistryException("X-Road call failed: " + e.getMessage(), e);
        }
    }
}
