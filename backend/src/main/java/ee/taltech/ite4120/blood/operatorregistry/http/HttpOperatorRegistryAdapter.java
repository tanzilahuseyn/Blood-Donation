package ee.taltech.ite4120.blood.operatorregistry.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.ite4120.blood.operatorregistry.OperatorInfo;
import ee.taltech.ite4120.blood.operatorregistry.OperatorRegistryAdapter;
import ee.taltech.ite4120.blood.operatorregistry.OperatorRegistryException;
import ee.taltech.ite4120.blood.operatorregistry.http.wire.ArCompanyResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Fetches the business register's answer over plain HTTPS+JSON and translates the
 * wire record into our domain {@link OperatorInfo}.
 *
 * <p>In the local configuration the URL points back at this very application's
 * {@code /mock-registry} endpoint — an imitated environment. Point it at a real
 * register by changing {@code blood.operator-registry.url}; nothing else changes.
 */
public class HttpOperatorRegistryAdapter implements OperatorRegistryAdapter {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper json = new ObjectMapper();
    private final String baseUrl;

    public HttpOperatorRegistryAdapter(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public OperatorInfo lookup(String registryCode) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/companies/" + registryCode))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                // An organisation the register does not know is a MISS, not a failure.
                return null;
            }
            if (response.statusCode() != 200) {
                throw new OperatorRegistryException(
                        "business register answered HTTP " + response.statusCode());
            }
            ArCompanyResponse wire = json.readValue(response.body(), ArCompanyResponse.class);
            // The mapper — the ONLY place where Estonian wire names become our names.
            return new OperatorInfo(wire.registrikood(), wire.nimi(), wire.aadress());
        } catch (IOException e) {
            throw new OperatorRegistryException("business register unreachable: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OperatorRegistryException("business register call interrupted", e);
        }
    }
}
