package ee.taltech.ite4120.blood.operatorregistry;

/** The business register could not answer — transport failure or upstream fault. Maps to 502. */
public class OperatorRegistryException extends RuntimeException {

    public OperatorRegistryException(String message) {
        super(message);
    }

    public OperatorRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
