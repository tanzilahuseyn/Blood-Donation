package ee.taltech.ite4120.blood.operatorregistry;

/**
 * A centre's operating organisation, in OUR domain vocabulary. The business
 * register's own field names live in the wire records and never leak past the mapper.
 */
public record OperatorInfo(String registryCode, String legalName, String address) {}
