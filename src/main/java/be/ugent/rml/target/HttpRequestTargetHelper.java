package be.ugent.rml.target;


import be.ugent.rml.NAMESPACES;
import org.apache.http.HttpStatus;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.JoseException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class HttpRequestTargetHelper {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestTargetHelper.class);
    private final EllipticCurveJsonWebKey jwk;

    private final HttpClient httpClient;
    /**
     *
     * Constructs a new HttpRequestTargetHelper instance. A new private + public key pair
     * gets generated which is used for communication with the Solid server, more
     * specifically for Distributed Proof of Possession (DPoP).
     * @throws JoseException Generating the private/public key pair goes wrong.
     */
    public HttpRequestTargetHelper() throws JoseException {
        jwk = EcJwkGenerator.generateJwk(EllipticCurves.P256);
        // Initialize a HTTP client
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)   // The Community Solid Server only accepts HTTP 1.1 and complains about upgrade headers if you don't specify this.
                .build();
    }

    /**
     * Retrieves the Dpop Access Token for CSS authentication using client credentials
     * @param httpRequestInfo A map with all necessary data. The map should contain following keys:
     *                  oidcIssuer, webId, email, password
     * @return        The Dpop Access Token.
     * @throws Exception Something goes wrong.
     */
    private String getDpopAccessToken(Map<String, String> httpRequestInfo) throws Exception {

        // See https://blog.stackademic.com/how-dpop-works-a-guide-to-proof-of-possession-for-web-tokens-cbeac2d4e43c
        // for a simple and clear description of how DPoP works.

        try {
            // TODO: this implementation is tailored to the Community Solid Server and thus uses one URL for authorization server and solid server.
            String oidcIssuer = httpRequestInfo.get("oidcIssuer");
            String webId = httpRequestInfo.get("webId");
            String email = httpRequestInfo.get("email");
            String password = httpRequestInfo.get("password");

            /* Get account controls and retrieve login URL */
            HttpRequest accountInfoRequest = HttpRequest.newBuilder(URI.create(oidcIssuer + ".account/"))
                    .GET().build();
            HttpResponse<String> accountInfoResponse = httpClient.send(accountInfoRequest, HttpResponse.BodyHandlers.ofString());
            String accountInfoStr = accountInfoResponse.body();
            if (accountInfoResponse.statusCode() != HttpStatus.SC_OK) {
                log.error("Could not get account info: {}", accountInfoStr);
                throw new Exception("Could not get account info: " + accountInfoStr);
            }
            JSONObject accountInfo = new JSONObject(accountInfoStr);
            String passwordLoginURL = accountInfo.getJSONObject("controls").getJSONObject("password").getString("login");
            log.debug("Found login URL: {}", passwordLoginURL);


            /* Log in using e-mail and password and get authorization token */
            String loginMessage = "{\"email\": \"" + email + "\",\"password\":\"" + password + "\"}";
            HttpRequest loginRequest = HttpRequest.newBuilder(URI.create(passwordLoginURL))
                    .POST(HttpRequest.BodyPublishers.ofString(loginMessage, StandardCharsets.UTF_8))
                    .setHeader("Content-Type", "application/json")
                    .build();
            HttpResponse<String> loginResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
            String loginInfoStr = loginResponse.body();
            if (loginResponse.statusCode() != HttpStatus.SC_OK) {
                log.error("Could not log in: {}", loginInfoStr);
                throw new Exception("Could not get log in: " + loginInfoStr);
            }
            JSONObject loginInfo = new JSONObject(loginInfoStr);
            String authorizationToken = loginInfo.getString("authorization");
            log.debug("Found authorization token.");


            /* Use authorization token to get client credentials URL, added to account info */
            HttpRequest authorizedAccountInfoRequest = HttpRequest.newBuilder(URI.create(oidcIssuer + ".account/"))
                    .GET()
                    .setHeader("Authorization", "CSS-Account-Token " + authorizationToken)
                    .build();
            HttpResponse<String> authorizedAccountInfoResponse = httpClient.send(authorizedAccountInfoRequest, HttpResponse.BodyHandlers.ofString());
            String authorizedAccountInfoStr = authorizedAccountInfoResponse.body();
            if (authorizedAccountInfoResponse.statusCode() != HttpStatus.SC_OK) {
                log.error("Could not get account info: {}", authorizedAccountInfoStr);
                throw new Exception("Could not get account info: " + authorizedAccountInfoStr);
            }
            JSONObject authorizedAccountInfo = new JSONObject(authorizedAccountInfoStr);
            String clientCredentialsURL = authorizedAccountInfo.getJSONObject("controls").getJSONObject("account").getString("clientCredentials");
            log.debug("Found client credentials URL: {}", clientCredentialsURL);


            /* Post WebID and token prefix to client credentials URL to get client credentials, to be used at oidc endpoint later on */
            String webIdAndTokenMessage = "{\"name\": \"my-token\",\"webId\":\"" + webId + "\"}";
            HttpRequest getOIDCTokenRequest = HttpRequest.newBuilder(URI.create(clientCredentialsURL))
                    .POST(HttpRequest.BodyPublishers.ofString(webIdAndTokenMessage, StandardCharsets.UTF_8))
                    .setHeader("Authorization", "CSS-Account-Token " + authorizationToken)
                    .setHeader("Content-Type", "application/json")
                    .build();
            HttpResponse<String> getOIDCTokenResponse = httpClient.send(getOIDCTokenRequest, HttpResponse.BodyHandlers.ofString());
            String clientCredentialsStr = getOIDCTokenResponse.body();
            if (getOIDCTokenResponse.statusCode() != HttpStatus.SC_OK) {
                log.error("Could not get OpenID Connect token info: {}", clientCredentialsStr);
                throw new Exception("Could not get OpenID Connect token info: " + clientCredentialsStr);
            }
            JSONObject clientCredentials = new JSONObject(clientCredentialsStr);
            String clientCredentialsId = clientCredentials.getString("id");
            log.debug("Found Client credentials. id: {}", clientCredentialsId);
            String clientCredentialsSecret = clientCredentials.getString("secret");


            /* Get oidc info, used to obtain oidc token endpoints */
            // GET /.well-known/openid-configuration HTTP/1.1
            HttpRequest oidcInfoRequest = HttpRequest.newBuilder(URI.create(oidcIssuer + ".well-known/openid-configuration"))
                    .GET().build();
            HttpResponse<String> oidcInfoResponse = httpClient.send(oidcInfoRequest, HttpResponse.BodyHandlers.ofString());
            String oidcInfoStr = oidcInfoResponse.body();
            if (oidcInfoResponse.statusCode() != HttpStatus.SC_OK) {
                log.error("Could not get OpenID Connect info: {}", oidcInfoStr);
                throw new Exception("Could not get OpenID Connect info: " + oidcInfoStr);
            }
            JSONObject oidcInfo = new JSONObject(oidcInfoStr);
            String oidcTokenEndpoint = oidcInfo.getString("token_endpoint");
            log.debug("Found oidc token endpoint: {}", oidcTokenEndpoint);

            String dpopJWT = generateJWT(oidcTokenEndpoint, "POST");

            /* POST a request to oidc token endpoint with client credentials to obtain an oidc access token */

            // Generate base64 string of client credentials
            String clientCredentialsConcatenated = clientCredentialsId + ':' + clientCredentialsSecret;
            String base64clientCredentials = Base64.getEncoder().encodeToString(clientCredentialsConcatenated.getBytes(StandardCharsets.UTF_8));

            HttpRequest getOidcAccessTokenRequest = HttpRequest.newBuilder(URI.create(oidcTokenEndpoint))
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&scope=webid", StandardCharsets.UTF_8))
                    .setHeader("Authorization", "Basic " + base64clientCredentials)
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .setHeader("DPoP", dpopJWT)
                    .build();
            HttpResponse<String> oidcAccessTokenResponse = httpClient.send(getOidcAccessTokenRequest, HttpResponse.BodyHandlers.ofString());
            String oidcAccessTokenStr = oidcAccessTokenResponse.body();
            if (oidcAccessTokenResponse.statusCode() != HttpStatus.SC_OK) {
                log.error("Could not get OpenID Connect access token: {}", oidcAccessTokenStr);
                throw new Exception("Could not get OpenID Connect info: " + oidcAccessTokenStr);
            }
            JSONObject oidcAccessToken = new JSONObject(oidcAccessTokenStr);
            return oidcAccessToken.getString("access_token");
            // token_type should be 'DPoP'
            // We don't use 'expires' at the moment because we send the next request immediately
            // and don't know if the next request would go to the same server. This can be checked
            // for in future implementations.
        } catch (Throwable e) { // This is to catch runtime exceptions as well.
            throw new Exception(e);
        }

    }

    /**
     * Generates a JSON Web Token for a given URL and HTTP method.
     * @param url     The endpoint of the request using this token.
     * @param method  The HTTP method of the request using this token.
     * @return        The signed and serialized generated JWT.
     * @throws JoseException    Olé José: something goes wrong generating the token.
     */
    private String generateJWT(final String url, final String method) throws JoseException {
        // set claims
        JwtClaims claims = new JwtClaims();
        claims.setGeneratedJwtId(); // jti claim
        claims.setClaim("htm", method);
        claims.setClaim("htu", url);
        claims.setIssuedAtToNow(); // iat claim

        // create jws
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(jwk.getPrivateKey());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);  // alg header
        jws.setHeader("typ", "dpop+jwt");
        jws.setJwkHeader(jwk);
        jws.sign();
        return jws.getCompactSerialization();
    }

    /** Executes a HTTP request to a web resource, optionally using authentication with CSS client credentials.
     * @param httpRequestInfo A map with all necessary data. The map should contain following keys: absoluteURI, methodName.
     *                        The map may contain following keys: contentType, accept, data,
     *                        authenticationType, email, password, oidcIssuer, webId
     *                        absoluteURI, methodName, contentType, data
     * @return response body
     * @throws Exception Something goes wrong.
     */
    public String executeHttpRequest(Map<String, String> httpRequestInfo) throws Exception{
        try {
            String absoluteURI = httpRequestInfo.get("absoluteURI");
            String methodName = httpRequestInfo.get("methodName");

            HttpRequest.Builder RequestBuilder = HttpRequest.newBuilder(URI.create(absoluteURI));

            if (httpRequestInfo.containsKey("data")){
                RequestBuilder.method(methodName,
                        HttpRequest.BodyPublishers.ofString(httpRequestInfo.get("data"), StandardCharsets.UTF_8));
            } else {
                RequestBuilder.method(methodName, HttpRequest.BodyPublishers.noBody());
            }
            if (httpRequestInfo.containsKey("contentType")){
                RequestBuilder.setHeader("Content-Type", httpRequestInfo.get("contentType"));
            }
            if (httpRequestInfo.containsKey("accept")){
                RequestBuilder.setHeader("Accept", httpRequestInfo.get("accept"));
            }
            addAuthentication(RequestBuilder, httpRequestInfo);
            HttpRequest httpRequest = RequestBuilder.build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (isNotSuccessful(httpResponse.statusCode())) {
                log.error("Could not successfully execute HTTP request for URI {} and method {}: {}", absoluteURI, methodName, httpResponse.statusCode());
                throw new Exception("Could not successfully execute HTTP request for URI " + absoluteURI + " and method " + methodName+ ": " + httpResponse.statusCode());
            }
            return httpResponse.body();
        } catch (Throwable e) { // This is to catch runtime exceptions as well.
            throw new Exception(e);
        }
    }

    /** Executes a HTTP request to a linked web resource, optionally using authentication with CSS client credentials.
     * @param httpRequestInfo A map with all necessary data. The map should contain following keys:
     *                        linkingAbsoluteURI, linkRelation, methodName.
     *                        The map may contain following keys: contentType, accept, data,
     *                        authenticationType, email, password, oidcIssuer, webId
     * @return response body
     * @throws Exception Something goes wrong.
     */
    public String executeLinkedHttpRequest(Map<String, String> httpRequestInfo) throws Exception{
        try {
            String linkingAbsoluteURI = httpRequestInfo.get("linkingAbsoluteURI");
            String linkRelation = httpRequestInfo.get("linkRelation");
            /* HEAD request to retrieve the linked absolute URI via a link relation */
            HttpRequest.Builder RequestBuilder = HttpRequest.newBuilder(URI.create(linkingAbsoluteURI))
                    .method(HttpMethod.HEAD.name(), HttpRequest.BodyPublishers.noBody());
            addAuthentication(RequestBuilder, httpRequestInfo);
            HttpRequest httpRequest = RequestBuilder.build();

            HttpResponse<String> headResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            List<String> links = headResponse.headers().map().get("link");
            boolean foundLink = false;
            int index = 0;
            String absoluteURI = null;
            String responseBody = null;
            while (!foundLink && index < links.size() ){
                String link = links.get(index);
                // a better method to parse the link header in Java would be welcome ...
                if (link.contains("rel=\"" + linkRelation + "\"")) {
                    absoluteURI = link.substring(link.indexOf("<") + 1, link.indexOf(">"));
                    httpRequestInfo.put("absoluteURI", absoluteURI);
                    responseBody = executeHttpRequest(httpRequestInfo);
                    foundLink = true;
                }
                index += 1;
            }

            if (!foundLink) {
                String message = "Could not get linked absolute URI for link relation " + linkRelation
                        + " and linking absolute URI " + linkingAbsoluteURI;
                log.error(message);
                throw new Exception(message);
            } else {
                return responseBody;
            }

        } catch (Throwable e) { // This is to catch runtime exceptions as well.
            throw new Exception(e);
        }
    }

    private void addAuthentication(HttpRequest.Builder RequestBuilder, Map<String, String> httpRequestInfo) throws Exception {
        if (httpRequestInfo.containsKey("authenticationType") &&
                // only authentication with CSS Client Credentials implemented until now
                httpRequestInfo.get("authenticationType").equals(NAMESPACES.RMLE + "CssClientCredentialsAuthentication")) {
            // Get dpop access token
            String dpopAccessToken = getDpopAccessToken(httpRequestInfo);
            RequestBuilder.setHeader("Authorization", "DPoP " + dpopAccessToken);
            // Generate new JWT token for this request
            String dataJWT = generateJWT(httpRequestInfo.get("absoluteURI"), httpRequestInfo.get("methodName"));
            RequestBuilder.setHeader("DPoP", dataJWT);
        }
    }


    private boolean isNotSuccessful(int statusCode){
        return (statusCode < 200) || (statusCode > 299) ;
    }
}
