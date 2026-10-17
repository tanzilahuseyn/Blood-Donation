package ee.taltech.ite4120.blood.api;

import ee.taltech.ite4120.blood.model.DonationCentre;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * The create/update request body. Bean Validation checks the *shape* here at the
 * edge; the rules that need the database or more than one field (known region,
 * both-or-neither coordinates, unique code) live in DonationCentreService.
 */
public class CentreRequest {

    @NotBlank
    @Size(max = 50)
    private String centreCode;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    private String regionCode;

    @NotBlank
    @Size(max = 255)
    private String address;

    @DecimalMin(value = "-90", message = "latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180", message = "longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "longitude must be between -180 and 180")
    private BigDecimal longitude;

    @Size(max = 255)
    private String openingHours;

    @Size(max = 30)
    private String phone;

    /** Eight digits — and a string, never a number. */
    @Pattern(regexp = "\\d{8}", message = "operatorRegistryCode must be exactly 8 digits")
    private String operatorRegistryCode;

    public DonationCentre toModel() {
        DonationCentre c = new DonationCentre();
        c.setCentreCode(centreCode);
        c.setName(name);
        c.setRegionCode(regionCode);
        c.setAddress(address);
        c.setLatitude(latitude);
        c.setLongitude(longitude);
        c.setOpeningHours(openingHours);
        c.setPhone(phone);
        c.setOperatorRegistryCode(operatorRegistryCode);
        return c;
    }

    public String getCentreCode() { return centreCode; }
    public void setCentreCode(String centreCode) { this.centreCode = centreCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getOperatorRegistryCode() { return operatorRegistryCode; }
    public void setOperatorRegistryCode(String operatorRegistryCode) { this.operatorRegistryCode = operatorRegistryCode; }
}
