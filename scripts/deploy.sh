#!/bin/bash
# run by exec-maven-plugin at the end of the "package" phase,
# determines if ../keycloak-developer-deployment is present and, if yes,
# deploys JAR to ../keycloak-developer-deployment/keycloak-custom/providers.

##
# Configuration

artifact_id="custom-auth-spi"

name=$(basename "$(readlink -f "$0")")
dir=$(dirname "$(readlink -f "$0")")
jar=$(readlink -f "$dir"/../target/"$artifact_id".jar)
developer_deployment_dir=$(readlink -f "$dir"/../../keycloak-developer-deployment)
keycloak_custom_dir="$developer_deployment_dir"/keycloak-custom
providers_dir="$keycloak_custom_dir"/providers

##
# Main Program
#

echo "INFO: $name starting ..." >&2

if ! [[ -d "$developer_deployment_dir" ]] ; then
	echo "WARNING: Developer deployment not found at $developer_deployment_dir; skipped." >&2
	exit 0
elif ! mkdir -p "$providers_dir" ; then
	echo "ERROR: Creating providers_dir=$providers_dir failed" >&2
	exit 1
elif ! cp -v "$jar" "$providers_dir" ; then
	echo "ERROR: Copying $jar to $providers_dir failed" >&2
	exit 1
fi
