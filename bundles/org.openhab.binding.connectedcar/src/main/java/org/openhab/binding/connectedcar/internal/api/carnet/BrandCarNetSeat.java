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
package org.openhab.binding.connectedcar.internal.api.carnet;

import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiBrandProperties;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;

/**
 * {@link BrandCarNetSeat} provides the SEAT specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class BrandCarNetSeat extends CarNetApi implements BrandAuthenticator {
    private static final ApiBrandProperties properties = new ApiBrandProperties();
    static {
        properties.brand = "VW"; // CNAPI_BRAND_SEAT;
        properties.xcountry = "ES";
        properties.apiDefaultUrl = CNAPI_DEFAULT_API_URL;
        properties.clientId = "3c8e98bc-3ae9-4277-a563-d5ee65ddebba@apps_vw-dilab_com";
        properties.xClientId = "9dcc70f0-8e79-423a-a3fa-4065d99088b4";
        properties.authScope = "openid profile mbb cars birthdate nickname address phone email nationalIdentifier dealers badge nationality";
        properties.tokenUrl = CNAPI_VW_TOKEN_URL;
        properties.tokenRefreshUrl = properties.tokenUrl;
        properties.redirect_uri = "seatconnect://identity-kit/login";
        // properties.xrequest = "cz.skodaauto.connect";
        properties.responseType = "code id_token";
        properties.xappName = "SEAT Connect";
        properties.xappVersion = "1.11.2";
    }

    public BrandCarNetSeat(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
    }

    @Override
    public ApiBrandProperties getProperties() {
        return properties;
    }
}
