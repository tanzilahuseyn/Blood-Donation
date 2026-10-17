package ee.taltech.ite4120.blood.operatorregistry.xroad.wire;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * WIRE RECORD — the X-Road request body for the (illustrative) business-register
 * company query, in the provider's namespace and vocabulary.
 *
 * <p>The service identifiers and this schema are TEACHING STAND-INS, like animals'
 * RrIsikRequest — before calling the real business register, replace them with
 * its published contract.
 */
@JacksonXmlRootElement(localName = "ettevoteParing", namespace = ArEttevoteRequest.NS)
public record ArEttevoteRequest(
        @JacksonXmlProperty(localName = "request", namespace = NS) Request request) {

    public static final String NS = "http://arireg.x-road.eu";

    public record Request(
            @JacksonXmlProperty(localName = "registrikood", namespace = NS) String registrikood) {}

    public static ArEttevoteRequest of(String registrikood) {
        return new ArEttevoteRequest(new Request(registrikood));
    }
}
