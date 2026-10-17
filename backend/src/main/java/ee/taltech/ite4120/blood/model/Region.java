package ee.taltech.ite4120.blood.model;

/** Reference row from {@code blood.region} — the county list of 01-region.sql. */
public class Region {

    private String code;
    private String name;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
