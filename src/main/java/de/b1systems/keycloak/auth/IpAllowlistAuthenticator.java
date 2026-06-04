package de.b1systems.keycloak.auth;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Arrays;
import java.util.List;

public class IpAllowlistAuthenticator implements Authenticator {
    private static final Logger LOG = Logger.getLogger(IpAllowlistAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String remoteAddress = context.getConnection().getRemoteAddr();

        String forwardedFor = context.getHttpRequest()
                .getHttpHeaders()
                .getHeaderString("X-Forwarded-For");

        String clientIp = forwardedFor != null
                ? forwardedFor.split(",")[0].trim()
                : remoteAddress;

        LOG.debugf("Checking IP allowlist for IP: %s", clientIp);

        AuthenticatorConfigModel config = context.getAuthenticatorConfig();

        if (config == null || config.getConfig() == null) {
            LOG.warn("No IP allowlist configured, allowing access");
            context.success();
            return;
        }

        String allowedIpsConfig = config.getConfig().get("allowedIps");
        if (allowedIpsConfig == null || allowedIpsConfig.isEmpty()) {
            context.success();
            return;
        }

        List<String> allowedIps = Arrays.asList(allowedIpsConfig.split(","));

        boolean allowed = allowedIps.stream()
                .map(String::trim)
                .anyMatch(pattern -> matchesIp(clientIp, pattern));

        if (allowed) {
            LOG.debugf("IP %s is in the allowlist", clientIp);
            context.success();
        } else {
            LOG.warnf("IP %s is NOT in the allowlist, blocking access", clientIp);
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
        } catch (Exception e) {
            LOG.warnf("Invalid CIDR pattern: %s", cidr);

            return false;
        }
    }

    private long ipToLong(String ip) {
        String[] octets = ip.split(".");
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
