#!/usr/bin/env bash
set -euo pipefail

MODULE_NAME="${1:-}"
BASE_PACKAGE="dev.ebaptistella.monolith"
ROOT="src/main/java/dev/ebaptistella/monolith/modules"

if [[ -z "${MODULE_NAME}" ]]; then
  echo "Usage: $0 <module-name>" >&2
  echo "Example: $0 billing" >&2
  exit 1
fi

if [[ ! "${MODULE_NAME}" =~ ^[a-z][a-z0-9]*$ ]]; then
  echo "Module name must be lowercase alphanumeric (e.g. billing, email)" >&2
  exit 1
fi

MODULE_PATH="${ROOT}/${MODULE_NAME}"

if [[ -d "${MODULE_PATH}" ]]; then
  echo "Module already exists: ${MODULE_PATH}" >&2
  exit 1
fi

mkdir -p "${MODULE_PATH}/models"
mkdir -p "${MODULE_PATH}/logic"
mkdir -p "${MODULE_PATH}/controllers"
mkdir -p "${MODULE_PATH}/adapters"
mkdir -p "${MODULE_PATH}/wire/in"
mkdir -p "${MODULE_PATH}/wire/out"
mkdir -p "${MODULE_PATH}/diplomat/jpa"
mkdir -p "${MODULE_PATH}/diplomat/http_server"
mkdir -p "${MODULE_PATH}/diplomat/inbound"
mkdir -p "${MODULE_PATH}/diplomat/outbound"
mkdir -p "${MODULE_PATH}/diplomat/producer"
mkdir -p "${MODULE_PATH}/diplomat/consumer"

PACKAGE_INFO="${MODULE_PATH}/package-info.java"
cat > "${PACKAGE_INFO}" <<EOF
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"shared"}
)
package ${BASE_PACKAGE}.modules.${MODULE_NAME};
EOF

README_SNIPPET="docs/modules/${MODULE_NAME}.md"
mkdir -p docs/modules
cat > "${README_SNIPPET}" <<EOF
# Module: ${MODULE_NAME}

Generated skeleton (Diplomat architecture). Next steps:

1. Add Liquibase changelog under \`src/main/resources/db/changelog/changes/\` and include it in \`db.changelog-master.yaml\`.
2. Implement domain models in \`models/\` and pure rules in \`logic/\`.
3. Use cases in \`controllers/\`; wire DTOs in \`wire/in\` and \`wire/out\`.
4. Adapters in \`adapters/\` (wire ↔ model).
5. Infrastructure in \`diplomat/\` (JPA, HTTP, inbound/outbound SPI, producer, consumer).
6. Cross-module events in \`shared.wire.in.events\` with \`@Externalized\`.
7. Follow [docs/GUIDELINES.md](../GUIDELINES.md) for full implementation checklist.
EOF

echo "Created module skeleton: ${MODULE_PATH}"
echo "Documentation stub: ${README_SNIPPET}"
