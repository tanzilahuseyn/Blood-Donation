package ee.taltech.ite4120.blood.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * One donation centre. Maps 1:1 onto {@code blood.donation_centre} — see
 * {@code resources/blood/db/changelog/blood/02-donation-centre.sql}.
 *
 * <p>The {@code sys*} fields are filled by the shared {@code core.sys_columns()}
 * trigger, never by application code.
 */
public class DonationCentre {

    private Long id;
    /** Unique among active rows; immutable once created. */
    private String centreCode;
    private String name;
    /** FK → blood.region(code). */
    private String regionCode;
    private String address;
    /** −90 … 90; given together with longitude or not at all. */
    private BigDecimal latitude;
    /** −180 … 180. */
    private BigDecimal longitude;
    /** Free text, e.g. "Mon–Fri 8–16". */
    private String openingHours;
    private String phone;
    /** Business registry code of the operating organisation — eight digits, a STRING. */
    private String operatorRegistryCode;

    private String sysStatus;
    private Long sysVersion;
    private OffsetDateTime sysCreatedAt;
    private String sysCreatedBy;
    private OffsetDateTime sysModifiedAt;
    private String sysModifiedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getSysStatus() { return sysStatus; }
    public void setSysStatus(String sysStatus) { this.sysStatus = sysStatus; }
    public Long getSysVersion() { return sysVersion; }
    public void setSysVersion(Long sysVersion) { this.sysVersion = sysVersion; }
    public OffsetDateTime getSysCreatedAt() { return sysCreatedAt; }
    public void setSysCreatedAt(OffsetDateTime sysCreatedAt) { this.sysCreatedAt = sysCreatedAt; }
    public String getSysCreatedBy() { return sysCreatedBy; }
    public void setSysCreatedBy(String sysCreatedBy) { this.sysCreatedBy = sysCreatedBy; }
    public OffsetDateTime getSysModifiedAt() { return sysModifiedAt; }
    public void setSysModifiedAt(OffsetDateTime sysModifiedAt) { this.sysModifiedAt = sysModifiedAt; }
    public String getSysModifiedBy() { return sysModifiedBy; }
    public void setSysModifiedBy(String sysModifiedBy) { this.sysModifiedBy = sysModifiedBy; }
}
