package be.ugent.rml;

import be.ugent.knows.idlabFunctions.IDLabFunctions;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;

public class MockedURLTests {

    @RegisterExtension
    static WireMockExtension wm1 = WireMockExtension.newInstance()
            .options(
                    wireMockConfig()
                            .port(8080)
                            .withRootDirectory("src/test/resources/mockedURLs")
            )
            .build();

    @Test
    public void dbpediaSpotlight_mocked() {
        WireMockRuntimeInfo wm1RuntimeInfo = wm1.getRuntimeInfo();
        int httpPort = wm1RuntimeInfo.getHttpPort();

        String endpoint = String.format("http://localhost:%s/dbpedia-spotlight/rest", httpPort);
        wm1.stubFor(get(urlMatching("/dbpedia-spotlight/rest/annotate\\?text=Barack\\+Obama")).willReturn(aResponse().withStatus(200)
                .withBodyFile("dbpedia-obama.json")));
        wm1.stubFor(get(urlMatching("/dbpedia-spotlight/rest/annotate\\?text=a")).willReturn(aResponse().withStatus(200)
                .withBodyFile("dbpedia-a.json")));

        List<String> entities = IDLabFunctions.dbpediaSpotlight("Barack Obama", endpoint);
        List<String> expected = new ArrayList<>();
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
