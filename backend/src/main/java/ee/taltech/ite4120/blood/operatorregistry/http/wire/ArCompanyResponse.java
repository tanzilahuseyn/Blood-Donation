package ee.taltech.ite4120.blood.operatorregistry.http.wire;

/**
 * WIRE RECORD — the business register's response exactly as the register defines it.
 *
 * <p>Field names are the provider's Estonian names, deliberately NOT translated:
 * a wire record must stay diffable against the provider's published schema. The
 * English renaming happens in exactly one place — the mapper in
 * {@link ee.taltech.ite4120.blood.operatorregistry.http.HttpOperatorRegistryAdapter}.
 */
public record ArCompanyResponse(
        String registrikood,
        String nimi,
        String aadress) {}
