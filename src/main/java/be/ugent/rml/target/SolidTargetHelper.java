package be.ugent.rml.target;


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

/**
 * SolidTargetHelper helps writing data to a Solid server, taking care of the Solid authorization flow.
 */
public class SolidTargetHelper {
    private static final Logger log = LoggerFactory.getLogger(SolidTargetHelper.class);
    private final EllipticCurveJsonWebKey jwk;

    private final HttpClient httpClient;
    /**
     *
     * Constructs a new SolidTargetHelper instance. A new private + public key pair
     * gets generated which is used for communication with the Solid server, more
     * specifically for Distributed Proof of Possession (DPoP).
     * @throws JoseException Generating the private/public key pair goes wrong.
     */
    public SolidTargetHelper() throws JoseException {
        jwk = EcJwkGenerator.generateJwk(EllipticCurves.P256);
        // Initialize a HTTP client
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)   // The Community Solid Server only accepts HTTP 1.1 and complains about upgrade headers if you don't specify this.
                .build();
    }

    /**
     * Sends data to a Solid pod using Solid OIDC.
     * @param solidInfo A map with all necessary data. The map should contain following keys:
     *                  serverUrl, webId, email, password, resourceUrl, contentType, data
     * @throws Exception Something goes wrong.
     */
    private String getDpopAccessToken(Map<String, String> solidInfo) throws Exception {

        // See https://blog.stackademic.com/how-dpop-works-a-guide-to-proof-of-possession-for-web-tokens-cbeac2d4e43c
        // for a simple and clear description of how DPoP works.

        try {
            // TODO: this implementation is tailored to the Community Solid Server and thus uses one URL for authorization server and solid server.
            String serverUrl = solidInfo.get("serverUrl");
            String webId = solidInfo.get("webId");
            String email = solidInfo.get("email");
            String password = solidInfo.get("password");

            /* Get account controls and retrieve login URL */
            HttpRequest accountInfoRequest = HttpRequest.newBuilder(URI.create(serverUrl + ".account/"))
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
            HttpRequest authorizedAccountInfoRequest = HttpRequest.newBuilder(URI.create(serverUrl + ".account/"))
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
            HttpRequest oidcInfoRequest = HttpRequest.newBuilder(URI.create(serverUrl + ".well-known/openid-configuration"))
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

    /** Adds data to a Solid pod as a resource using Solid OIDC.
     * @param solidInfo A map with all necessary data. The map should contain following keys:
     *                  serverUrl, webId, email, password, resourceUrl, contentType, data
     * @throws Exception Something goes wrong.
     */
    void addResource(Map<String, String> solidInfo) throws Exception{
        try {
            String resourceUrl = solidInfo.get("resourceUrl");
            String contentType = solidInfo.get("contentType");
            String data = solidInfo.get("data");

            String dpopAccessToken = getDpopAccessToken(solidInfo);

            /* PUT the data */
            // Generate new JWT token for this request
            String putDataJWT = generateJWT(resourceUrl, "PUT");

            HttpRequest putDataRequest = HttpRequest.newBuilder(URI.create(resourceUrl))
                    .PUT(HttpRequest.BodyPublishers.ofString(data, StandardCharsets.UTF_8))
                    .setHeader("Authorization", "DPoP " + dpopAccessToken)
                    .setHeader("DPoP", putDataJWT)
                    .setHeader("Content-Type", contentType)
                    .build();

            HttpResponse<String> putDataResponse = httpClient.send(putDataRequest, HttpResponse.BodyHandlers.ofString());
            if (isNotSuccessful(putDataResponse.statusCode())) {
                log.error("Could not create resource for URL {}: {}", resourceUrl, putDataResponse.statusCode());
                throw new Exception("Could not create resource for URL " + resourceUrl + ": " + putDataResponse.statusCode());
            }
        } catch (Throwable e) { // This is to catch runtime exceptions as well.
            throw new Exception(e);
        }
    }

    /** Adds data to a Solid pod as an ACL for a resource using Solid OIDC.
     * @param solidInfo A map with all necessary data. The map should contain following keys:
     *                  serverUrl, webId, email, password, resourceUrl, contentType, data
     * @throws Exception Something goes wrong.
     */
    void addAcl(Map<String, String> solidInfo) throws Exception{
        try {
            String resourceUrl = solidInfo.get("resourceUrl");
            String contentType = solidInfo.get("contentType");
            String data = solidInfo.get("data");

            String dpopAccessToken = getDpopAccessToken(solidInfo);

            /* HEAD the resource to a get link to where the ACL should be put */
            // Generate new JWT token for this request
            String headDataJWT = generateJWT(resourceUrl, "HEAD");

            HttpRequest headRequest = HttpRequest.newBuilder(URI.create(resourceUrl))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .setHeader("Authorization", "DPoP " + dpopAccessToken)
                    .setHeader("DPoP", headDataJWT)
                    .build();

            HttpResponse<String> headResponse = httpClient.send(headRequest, HttpResponse.BodyHandlers.ofString());

            List<String> links = headResponse.headers().map().get("link");
            boolean foundLink = false;
            int index = 0;
            while (!foundLink && index < links.size() ){
                String link = links.get(index);
                // a better method to parse the link header in Java would be welcome ...
                if (link.contains("rel=\"acl\"")) {

                    /* PUT the ACL to the found link */
                    String linkUrl = link.substring(link.indexOf("<") + 1, link.indexOf(">"));
                    String putAclJWT = generateJWT(linkUrl, "PUT");
                    HttpRequest putAclRequest = HttpRequest.newBuilder(URI.create(linkUrl))
                            .PUT(HttpRequest.BodyPublishers.ofString(data, StandardCharsets.UTF_8))
                            .setHeader("Content-Type", contentType)
                            .setHeader("Authorization", "DPoP " + dpopAccessToken)
                            .setHeader("DPoP", putAclJWT)
                            .build();
                    HttpResponse<String> putAclResponse = httpClient.send(putAclRequest, HttpResponse.BodyHandlers.ofString());

                    if (isNotSuccessful(putAclResponse.statusCode())) {
                        log.error("Could not create ACL for resource with URL {}: {}", resourceUrl, putAclResponse.statusCode());
                        throw new Exception("Could not create ACL for resource with URL " + resourceUrl + ": " + putAclResponse.statusCode());
                    }
                    foundLink = true;
                }
                index += 1;
            }

            if (!foundLink) {
                String message = "Could not get ACL link for resource with URL " + resourceUrl;
                log.error(message);
                throw new Exception(message);
            }

        } catch (Throwable e) { // This is to catch runtime exceptions as well.
            throw new Exception(e);
        }
    }

    /** Retrieve data to a Solid pod using Solid OIDC. This method is only used for testing.
     * @param solidInfo A map with all necessary data. The map should contain following keys:
     *                  serverUrl, webId, email, password, resourceUrl
     * @throws Exception Something goes wrong.
     */
    public String getResource(Map<String, String> solidInfo) throws Exception{
        try {
            String resourceUrl = solidInfo.get("resourceUrl");
            String dpopAccessToken = getDpopAccessToken(solidInfo);

            /* GET the data */
            // Generate new JWT token for this request
            String getDataJWT = generateJWT(resourceUrl, "GET");

            HttpRequest getDataRequest = HttpRequest.newBuilder(URI.create(resourceUrl))
                    .GET()
                    .setHeader("Authorization", "DPoP " + dpopAccessToken)
                    .setHeader("DPoP", getDataJWT)
                    .setHeader("Accept","application/n-quads")
                    .build();

            HttpResponse<String> getDataResponse = httpClient.send(getDataRequest, HttpResponse.BodyHandlers.ofString());
            if (isNotSuccessful(getDataResponse.statusCode())) {
                log.error("Could not get data from resource with URL {}: {}", resourceUrl, getDataResponse.statusCode());
                throw new Exception("Could not get data from resource with URL " + resourceUrl + ": " + getDataResponse.statusCode());
            }
            return getDataResponse.body();
        } catch (Throwable e) { // This is to catch runtime exceptions as well.
            throw new Exception(e);
        }
    }

    /** Delet a resource from a Solid pod using Solid OIDC. This method is only used for testing.
     * @param solidInfo A map with all necessary data. The map should contain following keys:
     *                  serverUrl, webId, email, password, resourceUrl
     * @throws Exception Something goes wrong.
     */
    public void deleteResource(Map<String, String> solidInfo) throws Exception{
        try {
            String resourceUrl = solidInfo.get("resourceUrl");
            String dpopAccessToken = getDpopAccessToken(solidInfo);

            /* DELETE the resource */
            // Generate new JWT token for this request
            String deleteDataJWT = generateJWT(resourceUrl, "DELETE");

            HttpRequest deleteDataRequest = HttpRequest.newBuilder(URI.create(resourceUrl))
                    .DELETE()
                    .setHeader("Authorization", "DPoP " + dpopAccessToken)
                    .setHeader("DPoP", deleteDataJWT)
                    .build();

            HttpResponse<String> deleteDataResponse = httpClient.send(deleteDataRequest, HttpResponse.BodyHandlers.ofString());
            if (isNotSuccessful(deleteDataResponse.statusCode())) {
                log.error("Could not delete resource with URL {}: {}", resourceUrl, deleteDataResponse.statusCode());
                throw new Exception("Could not delete resource with URL " + resourceUrl + ": " + deleteDataResponse.statusCode());
            }
        } catch (Throwable e) { // This is to catch runtime exceptions as well.
            throw new Exception(e);
        }
    }


    private boolean isNotSuccessful(int statusCode){
        return (statusCode < 200) || (statusCode > 299) ;
    }
}
