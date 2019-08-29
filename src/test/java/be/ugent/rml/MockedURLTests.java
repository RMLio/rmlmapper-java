package be.ugent.rml;

import be.ugent.rml.functions.lib.IDLabFunctions;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

public class MockedURLTests {

    private final int httpPort = 8080;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
            .withRootDirectory("src/test/resources/mockedURLs")
            .port(httpPort));


    @Test
    public void dbpediaSpotlight_mocked() {
        String endpoint = String.format("http://localhost:%s/dbpedia-spotlight/rest", httpPort);
        stubFor(get(urlMatching("/dbpedia-spotlight/rest/annotate\\?text=Barack\\+Obama")).willReturn(aResponse().withStatus(200)
                .withBodyFile("dbpedia-obama.json")));
        stubFor(get(urlMatching("/dbpedia-spotlight/rest/annotate\\?text=a")).willReturn(aResponse().withStatus(200)
                .withBodyFile("dbpedia-a.json")));

        List<String> entities = IDLabFunctions.dbpediaSpotlight("Barack Obama", endpoint);
        ArrayList<String> expected = new ArrayList<>();
        expected.add("http://dbpedia.org/resource/Barack_Obama");

        assertThat(entities, CoreMatchers.is(expected));

        entities = IDLabFunctions.dbpediaSpotlight("", endpoint);
        expected = new ArrayList<>();

        assertThat(entities, CoreMatchers.is(expected));

        entities = IDLabFunctions.dbpediaSpotlight("a", endpoint);
        expected = new ArrayList<>();

        assertThat(entities, CoreMatchers.is(expected));
    }
}
