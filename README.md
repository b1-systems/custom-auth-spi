# Custom Authenticator for Keycloak implementing an IP-Adress Allowlist

## Overview

This Keyclok extension implements an authenticator that can restrict login
flows based on the connection client's IP-address using an "IP allowlist" that
also supports CIDR notation.

This Keycloak extension has been created for training purposes and is fine-tuned
for use with [the corresponding developer deployment](https://github.com/b1-systems/keycloak-developer-deployment).

## Author, Copyright and License Information

*Note:* The code in this example is heavily based on Guilliano Molaire's blog
article "Keycloak Custom SPI Development: Build Your First Extension", accessed
Jun 4th. 2026 at
<https://skycloak.io/blog/keycloak-custom-spi-development-guide/#section-7>.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at <http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
