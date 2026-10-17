package ee.taltech.ite4120.blood.operatorregistry;

/**
 * The port through which the application asks "who operates this centre?".
 *
 * <p>The rest of the application depends only on this interface. Which transport
 * answers — the built-in mock, a plain-HTTPS register, or a real X-Road security
 * server — is chosen by configuration ({@code blood.operator-registry.mode}).
 * Same idea as animals' OwnerRegistryAdapter.
 */
public interface OperatorRegistryAdapter {

    /**
     * Look up an organisation by Estonian business registry code.
     *
     * @return the organisation, or {@code null} when the register does not know it — a miss
     * @throws OperatorRegistryException when the register cannot be reached or answers
     *         with a fault — the caller decides whether that fails open or closed.
     */
    OperatorInfo lookup(String registryCode);
}
