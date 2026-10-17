package ee.taltech.ite4120.blood;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ee.taltech.ite4120.blood.model.DonationCentre;
import ee.taltech.ite4120.blood.model.ReadinessQuestion;
import ee.taltech.ite4120.blood.service.DonationCentreService;
import ee.taltech.ite4120.blood.service.ReadinessQuestionService;
import java.math.BigDecimal;
import java.util.List;
import org.helex.commons.exception.ApiClientException;
import org.helex.commons.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * THE BUSINESS TESTS — the executable form of tests 1–5 in
 * docs/src/specifications/BLOOD.01 § Business tests. Written before the
 * implementation; watched fail; then made green. Test 6, the readiness outcome,
 * runs in the browser and is tested next to it: frontend/src/pages/readiness/evaluate.test.ts.
 *
 * <p>Runs against a real PostgreSQL via Testcontainers, so the blood changelog
 * is applied from empty as well.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class BloodBusinessRulesIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    DonationCentreService centres;

    @Autowired
    ReadinessQuestionService questions;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void aCentreCodeIsUniqueAmongActiveCentres() {
        // Given an active centre with code X
        DonationCentre first = centres.create(centre("IT-DUP-001"));
        assertThat(first.getId()).isNotNull();

        // When a second centre claims X, then the request conflicts
        assertThatThrownBy(() -> centres.create(centre("IT-DUP-001")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        // And when the first is retired, code X may be used again (soft delete)
        centres.retire(first.getId());
        DonationCentre reused = centres.create(centre("IT-DUP-001"));
        assertThat(reused.getId()).isNotEqualTo(first.getId());
    }

    @Test
    void aCentresLocationIsARealCoordinate() {
        // Given a request for a centre with latitude 91
        DonationCentre c = centre("IT-LAT-001");
        c.setLatitude(new BigDecimal("91"));
        c.setLongitude(new BigDecimal("24.7536"));

        // When it is added, then it is refused
        assertThatThrownBy(() -> centres.create(c))
                .isInstanceOf(ApiClientException.class)
                .hasMessageContaining("latitude must be between -90 and 90");
    }

    @Test
    void aCentresLocationIsGivenWhole() {
        // Given a latitude without a longitude
        DonationCentre c = centre("IT-LAT-002");
        c.setLatitude(new BigDecimal("59.4370"));
        c.setLongitude(null);

        // When it is added, then it is refused
        assertThatThrownBy(() -> centres.create(c))
                .isInstanceOf(ApiClientException.class)
                .hasMessageContaining("latitude and longitude must be given together");
    }

    @Test
    void theQuestionnaireServesOnlyActiveQuestionsInOrder() {
        // Given three active questions with display orders 2, 1 and 3, and one retired.
        // The demo context seeds questions too, so this test looks only at its own codes.
        question("IT-ORD-B", 2);
        question("IT-ORD-A", 1);
        question("IT-ORD-C", 3);
        question("IT-ORD-RETIRED", 0);
        jdbc.update("update blood.readiness_question set sys_status = 'C' where question_code = ?",
                "IT-ORD-RETIRED");

        // When the questions are requested
        List<String> served = questions.activeQuestions().stream()
                .map(ReadinessQuestion::getQuestionCode)
                .filter(code -> code.startsWith("IT-ORD-"))
                .toList();

        // Then exactly the three active ones are returned, ordered 1, 2, 3
        assertThat(served).containsExactly("IT-ORD-A", "IT-ORD-B", "IT-ORD-C");
    }

    @Test
    void aCentresRegionMustBeAKnownOne() {
        // Given a request for a centre with region code ATLANTIS
        DonationCentre c = centre("IT-REG-001");
        c.setRegionCode("ATLANTIS");

        // When it is added, then it is refused
        assertThatThrownBy(() -> centres.create(c))
                .isInstanceOf(ApiClientException.class)
                .hasMessageContaining("unknown region code ATLANTIS");
    }

    @Test
    void anOperatorCodeIsExactlyEightDigits() {
        // Given operator codes of seven digits, and of eight characters with a letter
        for (String malformed : List.of("1234567", "1234567A")) {
            DonationCentre c = centre("IT-OPR-" + malformed);
            c.setOperatorRegistryCode(malformed);

            // When the centre is added, then it is refused
            assertThatThrownBy(() -> centres.create(c))
                    .as("operator code %s", malformed)
                    .isInstanceOf(ApiClientException.class)
                    .hasMessageContaining("operatorRegistryCode must be exactly 8 digits");
        }
    }

    /** A valid centre: the region is one of the seeded counties, no location, no operator. */
    private static DonationCentre centre(String centreCode) {
        DonationCentre c = new DonationCentre();
        c.setCentreCode(centreCode);
        c.setName("Test Donation Centre");
        c.setRegionCode("HARJU");
        c.setAddress("Testi 1, Tallinn");
        return c;
    }

    /** Questions arrive by changeset, not through the API, so the test inserts them directly. */
    private void question(String code, int displayOrder) {
        jdbc.update("""
                insert into blood.readiness_question
                    (question_code, category, question_text, concern_answer, severity, advice, display_order)
                values (?, 'PREPARATION', 'Have you eaten today?', 'NO', 'ADVISORY', 'Eat a light meal first.', ?)""",
                code, displayOrder);
    }
}
