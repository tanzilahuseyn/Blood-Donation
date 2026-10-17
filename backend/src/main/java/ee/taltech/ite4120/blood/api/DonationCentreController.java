package ee.taltech.ite4120.blood.api;

import ee.taltech.ite4120.blood.dto.CentreQueryParams;
import ee.taltech.ite4120.blood.model.DonationCentre;
import ee.taltech.ite4120.blood.model.Region;
import ee.taltech.ite4120.blood.operatorregistry.OperatorInfo;
import ee.taltech.ite4120.blood.operatorregistry.OperatorRegistryAdapter;
import ee.taltech.ite4120.blood.service.DonationCentreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.helex.commons.model.QueryResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The donation centre directory REST API — BLOOD.01, API. */
@RestController
@RequestMapping("/api/centres")
@Tag(name = "Donation centres", description = "Where and when to give blood")
public class DonationCentreController {

    private final DonationCentreService service;
    private final OperatorRegistryAdapter operatorRegistry;

    public DonationCentreController(DonationCentreService service, OperatorRegistryAdapter operatorRegistry) {
        this.service = service;
        this.operatorRegistry = operatorRegistry;
    }

    @Operation(summary = "Search donation centres — paginated, filterable by region, sortable by name or centreCode")
    @ApiResponse(responseCode = "400", description = "Unknown sort key")
    @GetMapping
    public QueryResult<DonationCentre> search(@ModelAttribute CentreQueryParams params) {
        return service.search(params);
    }

    @Operation(summary = "One donation centre by id")
    @ApiResponse(responseCode = "200", description = "The centre")
    @ApiResponse(responseCode = "404", description = "No active centre with that id")
    @GetMapping("/{id}")
    public DonationCentre byId(@PathVariable Long id) {
        return service.require(id);
    }

    @Operation(summary = "Add a donation centre")
    @ApiResponse(responseCode = "201", description = "Created — Location header points at the new centre")
    @ApiResponse(responseCode = "400", description = "Validation failed (unknown region, bad coordinate, malformed operator code…)")
    @ApiResponse(responseCode = "409", description = "Centre code already in use by an active centre")
    @PostMapping
    public ResponseEntity<DonationCentre> create(@Valid @RequestBody CentreRequest body) {
        DonationCentre saved = service.create(body.toModel());
        return ResponseEntity.created(URI.create("/api/centres/" + saved.getId())).body(saved);
    }

    @Operation(summary = "Update a donation centre (centre code is immutable)")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "No active centre with that id")
    @PutMapping("/{id}")
    public DonationCentre update(@PathVariable Long id, @Valid @RequestBody CentreRequest body) {
        return service.update(id, body.toModel());
    }

    @Operation(summary = "Retire a donation centre (soft delete)")
    @ApiResponse(responseCode = "204", description = "Retired")
    @ApiResponse(responseCode = "404", description = "No active centre with that id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        service.retire(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "The centre's operator, looked up live from the business register",
            description = "Fails open: when the register is down only this endpoint answers 502. "
                    + "A centre without an operator code, or an organisation the register does not "
                    + "know, is a miss — 200 with an empty body.")
    @ApiResponse(responseCode = "200", description = "The operator, or an empty body for a miss")
    @ApiResponse(responseCode = "404", description = "No active centre with that id")
    @ApiResponse(responseCode = "502", description = "The business register is unavailable")
    @GetMapping("/{id}/operator")
    public OperatorInfo operator(@PathVariable Long id) {
        DonationCentre centre = service.require(id);
        if (centre.getOperatorRegistryCode() == null) {
            return null;
        }
        return operatorRegistry.lookup(centre.getOperatorRegistryCode());
    }

    @Operation(summary = "Region reference list")
    @GetMapping("/regions")
    public List<Region> regions() {
        return service.regions();
    }
}
