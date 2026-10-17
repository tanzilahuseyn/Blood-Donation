package ee.taltech.ite4120.blood.operatorregistry;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ee.taltech.ite4120.blood.operatorregistry.xroad.XRoadOperatorRegistryAdapter;
import org.junit.jupiter.api.Test;

/**
 * The forge-xroad skeleton is not expected to reach a security server in this
 * course — but it must fail FAST and CLEARLY when there is none. Same contract as
 * animals' XRoadAdapterSkeletonTest.
 */
class XRoadOperatorAdapterSkeletonTest {

    @Test
    void failsFastAndClearlyWithoutASecurityServer() {
        XRoadOperatorRegistryAdapter adapter = new XRoadOperatorRegistryAdapter(
                "http://127.0.0.1:9", // reserved discard port — nothing listens here
                "ee-test", "EDU", "taltech", "ite4120", "GOV", "70000310", "arireg");

        assertThatThrownBy(() -> adapter.lookup("90000001"))
                .isInstanceOf(OperatorRegistryException.class)
                .hasMessageContaining("X-Road call failed");
    }
}
