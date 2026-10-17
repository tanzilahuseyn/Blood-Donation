package ee.taltech.ite4120.blood.operatorregistry.xroad.wire;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * WIRE RECORD — the provider's response, schema-faithful. Estonian element names
 * stay Estonian; the mapper in XRoadOperatorRegistryAdapter does the translating.
 */
@JacksonXmlRootElement(localName = "ettevoteParingResponse", namespace = ArEttevoteRequest.NS)
public record ArEttevoteResponse(
        @JacksonXmlProperty(localName = "registrikood", namespace = ArEttevoteRequest.NS) String registrikood,
        @JacksonXmlProperty(localName = "nimi", namespace = ArEttevoteRequest.NS) String nimi,
        @JacksonXmlProperty(localName = "aadress", namespace = ArEttevoteRequest.NS) String aadress) {}
