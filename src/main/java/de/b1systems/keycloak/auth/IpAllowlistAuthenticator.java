/* Copyright 2026  B1 Systems GmbH <info@b1-systems.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * */

package de.b1systems.keycloak.auth;

import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class IpAllowlistAuthenticator implements Authenticator {
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String remoteAddress = context.getConnection().getRemoteAddr();

        String forwardedFor = context.getHttpRequest()
                .getHttpHeaders()
                .getHeaderString("X-Forwarded-For");

        String clientIp = forwardedFor != null
                ? forwardedFor.split(",")[0].trim()
                : remoteAddress;

        AuthenticatorConfigModel config = context.getAuthenticatorConfig();

        if (config == null || config.getConfig() == null) {
            context.success();
            return;
        }

        String allowedIpsConfig = config.getConfig().get("allowedIps");

        if (allowedIpsConfig == null || allowedIpsConfig.isEmpty()) {
            context.success();
            return;
        }

        List<String> allowedIps = Arrays.asList(allowedIpsConfig.split(","));

        boolean allowed = allowedIps
            .stream()
            .map(String::trim)
            .anyMatch(pattern -> matchesIp(clientIp, pattern));

        if (allowed) {
            context.success();
        } else {
            context.failure(AuthenticationFlowError.ACCESS_DENIED);
        }
    }

    private boolean matchesIp(String clientIp, String pattern) {
        if (pattern.contains("/")) {
            return matchesCidr(clientIp, pattern);
        }

        return clientIp.equals(pattern);
    }

    private boolean matchesCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String networkAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);
            long ipLong = ipToLong(ip);
            long networkLong = ipToLong(networkAddress);
            long mask = -(1L << (32 - prefixLength));

            return (ipLong & mask) == (networkLong & mask);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        long result = 0;

        for (int i = 0; i < 4; i++) {
            result = (result << 8) | Integer.parseInt(octets[i]);
        }

        return result;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}
