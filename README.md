# Custom Authenticator for Keycloak implementing an IP-Adress Allowlist

## Overview

This Keyclok extension implements an authenticator that can restrict login
flows based on the connection client's IP-address using an "IP allowlist" that
also supports CIDR notation.

This Keycloak extension has been created for training purposes and is fine-tuned
for use with [the corresponding developer deployment](https://github.com/b1-systems/keycloak-developer-deployment).

## Author, Copyright and License Information

The code in this example is heavily based on Guilliano Molaire's blog article
"Keycloak Custom SPI Development: Build Your First Extension", accessed Jun
4th. 2026 at
<https://skycloak.io/blog/keycloak-custom-spi-development-guide/#section-7>.
