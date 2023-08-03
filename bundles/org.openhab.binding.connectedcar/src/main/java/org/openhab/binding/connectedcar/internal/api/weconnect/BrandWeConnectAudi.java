/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.connectedcar.internal.api.weconnect;

import static org.openhab.binding.connectedcar.internal.BindingConstants.CONTENT_TYPE_FORM_URLENC;
import static org.openhab.binding.connectedcar.internal.BindingConstants.CONTENT_TYPE_JSON;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_BRAND_AUDI;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNAPI_HEADER_APP;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNAPI_HEADER_USER_AGENT;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNAPI_HEADER_VERS;
import static org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCAPI_BASE_URL;
import static org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList.WCVehicle;
import static org.openhab.binding.connectedcar.internal.util.Helpers.fromJson;
import static org.openhab.binding.connectedcar.internal.util.Helpers.generateQMAuth;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiBrandProperties;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiHttpMap;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity;
import org.openhab.binding.connectedcar.internal.api.ApiResult;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.api.IdentityOAuthFlow;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * {@link BrandWeConnectAudi} provides the Audi specific functions of the API
 *
 * @author Dr. Yves Kreis - Initial contribution
 */
@NonNullByDefault
public class BrandWeConnectAudi extends WeConnectApi implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(BrandWeConnectAudi.class);
    private Map<String, WCVehicle> vehicleData = new HashMap<>();

    static ApiBrandProperties properties = new ApiBrandProperties();
    static {
        properties.brand = API_BRAND_AUDI;
        properties.userAgent = "myAudi-Android/4.13.0 (Build 800236847.2111261819) Android/11";
        properties.xcountry = "DE";
        properties.apiDefaultUrl = WCAPI_BASE_URL;
        properties.tokenUrl = "https://emea.bff.cariad.digital/login/v1/idk/token";
        properties.tokenRefreshUrl = properties.tokenUrl;
        properties.clientId = "f4d0934f-32bf-4ce4-b3c4-699a7049ad26@apps_vw-dilab_com";
        properties.xClientId = "77869e21-e30a-4a92-b016-48ab7d3db1d8";
        properties.authScope = "address badge birthdate birthplace email gallery mbb name nationalIdentifier nationality nickname phone picture profession profile vin openid";
        properties.redirect_uri = "myaudi:///";
        properties.responseType = "code";
        properties.xappVersion = "3.22.0";
        properties.xappName = "myAudi";
    }

    private static String accessToken = "";

    public BrandWeConnectAudi(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
    }

    @Override
    public ApiBrandProperties getProperties() {
        return properties;
    }

    @Override
    public ApiIdentity grantAccess(IdentityOAuthFlow oauth) throws ApiException {
        String json = oauth.clearHeader().header(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON)
                .header(HttpHeader.CONTENT_TYPE.toString(), CONTENT_TYPE_FORM_URLENC)
                .header(HttpHeader.ACCEPT_CHARSET.toString(), StandardCharsets.UTF_8.toString())
                .header("X-QMAuth", "v1:01da27b0:" + generateQMAuth())
                .header(HttpHeader.ACCEPT_LANGUAGE.toString(), "de-de")
                .header(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT) //
                .clearData().data("client_id", config.api.clientId).data("grant_type", "authorization_code")
                .data("code", oauth.code).data("redirect_uri", config.api.redirect_uri)
                .data("response_type", "token id_token") //
                .post(config.api.tokenUrl, false).response;
        ApiIdentity identity = new ApiIdentity(fromJson(gson, json, ApiIdentity.OAuthToken.class));
        json = oauth.clearHeader().header(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON)
                .header(HttpHeader.CONTENT_TYPE.toString(), CONTENT_TYPE_JSON)
                .header(HttpHeader.ACCEPT_CHARSET.toString(), StandardCharsets.UTF_8.toString())
                .header(CNAPI_HEADER_VERS, "4.13.0").header(CNAPI_HEADER_APP, config.api.xappName)
                .header(HttpHeader.ACCEPT_LANGUAGE.toString(), "de-de")
                .header(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT) //
                .clearData().data("token", identity.getAccessToken()).data("grant_type", "id_token")
                .data("stage", "live").data("config", "myaudi")
                .post("https://emea.bff.cariad.digital/login/v1/audi/token", true).response;
        ApiIdentity.OAuthToken token = fromJson(gson, json, ApiIdentity.OAuthToken.class);
        accessToken = token.accessToken;
        return identity;
    }

    @Override
    public ApiIdentity.OAuthToken refreshToken(ApiIdentity token) throws ApiException {
        ApiHttpMap map = new ApiHttpMap().header(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON)
                .header(HttpHeader.CONTENT_TYPE.toString(), CONTENT_TYPE_FORM_URLENC)
                .header(HttpHeader.ACCEPT_CHARSET.toString(), StandardCharsets.UTF_8.toString())
                .header("X-QMAuth", "v1:01da27b0:" + generateQMAuth())
                .header(HttpHeader.ACCEPT_LANGUAGE.toString(), "de-de")
                .header(HttpHeader.USER_AGENT.toString(),
                        "myAudi-Android/4.13.0 (Build 800236847.2111261819) Android/11")
                .data("client_id", config.api.clientId).data("grant_type", "refresh_token")
                .data("refresh_token", token.getRefreshToken()).data("response_type", "token id_token");
        String json = http.post(config.api.tokenRefreshUrl, map.getHeaders(), map.getData(), false).response;
        return fromJson(gson, json, ApiIdentity.OAuthToken.class);
    }

    @Override
    public ArrayList<String> getVehicles() throws ApiException {
        createAccessToken();
        ApiHttpMap map = new ApiHttpMap().header(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT)
                .header(HttpHeader.AUTHORIZATION.toString(), "Bearer " + accessToken)
                .header(HttpHeader.ACCEPT_LANGUAGE.toString(), "de-DE").header("DMP-API-Version", "v2.0")
                .header("DMP-Client-Info", CNAPI_HEADER_USER_AGENT)
                .header(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON)
                .header(HttpHeader.ACCEPT_CHARSET.toString(), StandardCharsets.UTF_8.toString()).data("query",
                        "query vehicleList { userVehicles { vin userRole { role } vehicle { media { shortName } } } }");
        String json = callApi(map.getHeaders(), map.getData());
        WCVehicle wev;
        ArrayList<String> list = new ArrayList<String>();
        for (JsonElement jsonElement : JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("data")
                .getAsJsonArray("userVehicles")) {
            wev = new WCVehicle();
            wev.vin = jsonElement.getAsJsonObject().get("vin").getAsString();
            wev.role = jsonElement.getAsJsonObject().getAsJsonObject("userRole").get("role").getAsString();
            wev.model = jsonElement.getAsJsonObject().getAsJsonObject("vehicle").getAsJsonObject("media")
                    .get("shortName").getAsString();
            wev.nickname = "";
            list.add(wev.vin);
            vehicleData.put(wev.vin, wev);
        }
        return list;
    }

    protected String callApi(Map<String, String> headers, Map<String, String> data) throws ApiException {
        String json = "";
        try {
            ApiResult res = http.post("https://app-api.live-my.audi.com/vgql/v1/graphql", headers, data, true);
            json = res.response;
            if (res.isRedirect()) {
                // Handle redirect
                String newLocation = res.getLocation();
                logger.debug("{}: Handle HTTP Redirect -> {}", config.vehicle.vin, newLocation);
                json = http.get(newLocation, "", fillAppHeaders()).response;
            }

            // special case on target class == String (return raw info)
            return json;
        } catch (ApiException e) {
            ApiResult res = e.getApiResult();
            if (e.isSecurityException() || res.isHttpUnauthorized()) {
                json = loadJson("getVehicles");
            }

            if ((json == null) || json.isEmpty()) {
                logger.debug("{}: API call {} failed: {}", config.vehicle.vin, "getVehicles", e.toString());
                throw e;
            }

            // special case on target class == String (return raw info)
            return json;
        } catch (RuntimeException e) {
            logger.debug("{}: API call {} failed", config.vehicle.vin, "getVehicles", e);
            throw new ApiException("API call fails: RuntimeException", e);
        }
    }

    @Override
    public VehicleDetails getVehicleDetails(String vin) throws ApiException {
        if (vehicleData.containsKey(vin)) {
            WCVehicle vehicle = vehicleData.get(vin);
            VehicleDetails vehicleDetails = new VehicleDetails(config, vehicle);
            vehicleDetails.model = Objects.requireNonNull(vehicle).model;
            return vehicleDetails;
        } else {
            throw new ApiException("Unknown VIN: " + vin);
        }
    }
}
