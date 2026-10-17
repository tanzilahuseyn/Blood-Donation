package ee.taltech.ite4120.blood.dto;

import org.helex.commons.model.QueryParams;

/**
 * Query parameters for the centre list endpoint. Extends the platform
 * {@link QueryParams}, which carries {@code limit}, {@code offset} and {@code sort}.
 */
public class CentreQueryParams extends QueryParams {

    /** Case-insensitive substring match on name or address. */
    private String textContains;
    /** Exact region filter, e.g. {@code HARJU}. */
    private String regionCode;

    public String getTextContains() { return textContains; }
    public void setTextContains(String textContains) { this.textContains = textContains; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
}
