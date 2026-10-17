package ee.taltech.ite4120.blood.service;

import ee.taltech.ite4120.blood.dto.CentreQueryParams;
import ee.taltech.ite4120.blood.model.DonationCentre;
import ee.taltech.ite4120.blood.model.Region;
import ee.taltech.ite4120.blood.repository.DonationCentreRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import org.helex.commons.exception.ApiClientException;
import org.helex.commons.exception.ConflictException;
import org.helex.commons.exception.NotFoundException;
import org.helex.commons.model.QueryResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business rules for the donation centre directory — exactly the ones written as
 * business tests 1, 2, 4 and 5 in docs/src/specifications/BLOOD.01.
 */
@Service
public class DonationCentreService {

    private static final BigDecimal LAT_LIMIT = BigDecimal.valueOf(90);
    private static final BigDecimal LON_LIMIT = BigDecimal.valueOf(180);
    private static final Pattern OPERATOR_CODE = Pattern.compile("\\d{8}");

    private final DonationCentreRepository repository;

    public DonationCentreService(DonationCentreRepository repository) {
        this.repository = repository;
    }

    public QueryResult<DonationCentre> search(CentreQueryParams params) {
        return repository.search(params);
    }

    public DonationCentre require(Long id) {
        DonationCentre c = repository.load(id);
        if (c == null) {
            throw new NotFoundException("donation centre " + id + " not found");
        }
        return c;
    }

    @Transactional
    public DonationCentre create(DonationCentre centre) {
        validate(centre);
        // Business rule: a centre code is unique among active centres.
        if (repository.loadByCentreCode(centre.getCentreCode()) != null) {
            throw new ConflictException("donation-centre",
                    "an active centre with code " + centre.getCentreCode() + " already exists");
        }
        Long id = repository.insert(centre);
        return require(id);
    }

    @Transactional
    public DonationCentre update(Long id, DonationCentre centre) {
        DonationCentre existing = require(id);
        // The centre code is immutable after creation — identity does not change.
        centre.setCentreCode(existing.getCentreCode());
        validate(centre);
        centre.setId(id);
        if (!repository.update(centre)) {
            throw new NotFoundException("donation centre " + id + " not found");
        }
        return require(id);
    }

    @Transactional
    public void retire(Long id) {
        if (!repository.retire(id)) {
            throw new NotFoundException("donation centre " + id + " not found");
        }
    }

    public List<Region> regions() {
        return repository.regionList();
    }

    private void validate(DonationCentre c) {
        // Business rule: a centre's region must be a known one.
        if (!repository.regionExists(c.getRegionCode())) {
            throw new ApiClientException("unknown region code " + c.getRegionCode());
        }
        // Business rule: a location is a real coordinate, given whole.
        if ((c.getLatitude() == null) != (c.getLongitude() == null)) {
            throw new ApiClientException("latitude and longitude must be given together");
        }
        if (c.getLatitude() != null && c.getLatitude().abs().compareTo(LAT_LIMIT) > 0) {
            throw new ApiClientException("latitude must be between -90 and 90");
        }
        if (c.getLongitude() != null && c.getLongitude().abs().compareTo(LON_LIMIT) > 0) {
            throw new ApiClientException("longitude must be between -180 and 180");
        }
        // Business rule: an operator code is exactly eight digits — a string, never a number.
        if (c.getOperatorRegistryCode() != null && !OPERATOR_CODE.matcher(c.getOperatorRegistryCode()).matches()) {
            throw new ApiClientException("operatorRegistryCode must be exactly 8 digits");
        }
    }
}
