package ee.taltech.ite4120.blood.operatorregistry.mock;

import ee.taltech.ite4120.blood.operatorregistry.http.wire.ArCompanyResponse;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The imitated business register — canned data served by the application itself,
 * so the course works offline. Enabled only when
 * {@code blood.operator-registry.mock-server=true} (the local profile).
 *
 * <p>The three organisations are FICTIONAL. Real organisations' data is not copied
 * into the mock (BLOOD.01, The business register). 90000001 and 90000002 operate
 * demo centres; 90000003 is known but operates none; the demo centre with
 * 90000099 shows a miss.
 */
@RestController
@RequestMapping("/mock-registry")
@ConditionalOnProperty(name = "blood.operator-registry.mock-server", havingValue = "true")
public class MockOperatorRegistryController {

    private static final Map<String, ArCompanyResponse> COMPANIES = Map.of(
            "90000001", new ArCompanyResponse("90000001", "Näidisverekeskus SA", "Näidise 1, Tallinn"),
            "90000002", new ArCompanyResponse("90000002", "Lõuna Näidisvereteenistus SA", "Näidise 2, Tartu"),
            "90000003", new ArCompanyResponse("90000003", "Näidistervis OÜ", "Näidise 4, Narva"));

    @GetMapping("/companies/{registrikood}")
    public ResponseEntity<ArCompanyResponse> company(@PathVariable String registrikood) {
        ArCompanyResponse company = COMPANIES.get(registrikood);
        return company == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(company);
    }
}
