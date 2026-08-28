#!/bin/bash

set -euo pipefail

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
test_dir=$(mktemp -d)
trap 'rm -rf "$test_dir"' EXIT

source_manifest=$(unzip -p "$script_dir/../lib/commons-io.jar" META-INF/MANIFEST.MF)
if unzip -p "$script_dir/../lib/commons-io.jar" META-INF/MANIFEST.MF | perl -0ne 'exit 1 unless /version="1\.4\.\r?\n 9999"/'; then
  echo 'commons-io.jar still contains a folded version value' >&2
  exit 1
fi
source_manifest_unfolded=$(printf '%s' "$source_manifest" | perl -0pe 's/\r?\n[ \t]//g')
grep -Fq 'version="1.4.9999"' <<< "$source_manifest_unfolded"
if grep -Fq 'java.io' <<< "$source_manifest_unfolded"; then
  echo 'commons-io.jar still imports java.* packages' >&2
  exit 1
fi
for jar_name in commons-lang3.jar jspecify.jar; do
  direct_manifest=$(unzip -p "$script_dir/../lib/$jar_name" META-INF/MANIFEST.MF)
  if grep -Eq '^Import-Package: *($|Require-Capability:|Multi-Release:)' <<< "$direct_manifest"; then
    echo "$jar_name has a malformed Import-Package header" >&2
    exit 1
  fi
done

for jar_name in commons-io.jar commons-lang3.jar jakarta.xml.bind-api.jar jspecify.jar; do
  direct_manifest=$(unzip -p "$script_dir/../lib/$jar_name" META-INF/MANIFEST.MF)
  if grep -Eq '^Import-Package: .* (Require-Capability|Multi-Release):' <<< "$direct_manifest"; then
    echo "$jar_name has adjacent manifest headers merged into Import-Package" >&2
    exit 1
  fi
  if grep -Fq 'Include-Resource:' <<< "$direct_manifest"; then
    echo "$jar_name still contains Include-Resource metadata" >&2
    exit 1
  fi
done
if ! unzip -p "$script_dir/../lib/jakarta.xml.bind-api.jar" META-INF/MANIFEST.MF | perl -0pe 's/\r?\n[ \t]//g' | grep -Fq 'jakarta.activation;version="[2.0,3)";resolution:=optional'; then
  echo 'jakarta.xml.bind-api.jar must make jakarta.activation optional' >&2
  exit 1
fi

mkdir -p "$test_dir/bundles/META-INF"
printf '%s\r\n' \
  'Manifest-Version: 1.0' \
  'Bundle-ManifestVersion: 2' \
  'Export-Package: org.example;version="1.4.9999";uses:="sun.io,org.example.dep"' \
  'Include-Resource: META-INF/LICENSE.txt=LICENSE.txt' \
  'Import-Package: java.io,org.example.long.package,' \
  > "$test_dir/bundles/META-INF/MANIFEST.MF"
(cd "$test_dir/bundles" && zip -q -r "$test_dir/bundle.jar" META-INF)
mv "$test_dir/bundle.jar" "$test_dir/bundles/bundle.jar"

"$script_dir/strip_jvm_imports.sh" "$test_dir/bundles"

manifest=$(unzip -p "$test_dir/bundles/bundle.jar" META-INF/MANIFEST.MF)
grep -Fq 'version="1.4.9999"' <<< "$manifest"
if grep -Fq 'sun.io' <<< "$manifest"; then
  echo 'fixture still references sun.io from an export uses directive' >&2
  exit 1
fi
if grep -Fq 'java.io' <<< "$manifest"; then
  echo 'fixture still imports java.* packages' >&2
  exit 1
fi
if grep -Eq 'org\.example\.long\.package,\r?$' <<< "$manifest"; then
  echo 'fixture still has a trailing Import-Package comma' >&2
  exit 1
fi
if grep -Fq 'Include-Resource:' <<< "$manifest"; then
  echo 'fixture still contains Include-Resource metadata' >&2
  exit 1
fi

first_hash=$(sha256sum "$test_dir/bundles/bundle.jar")
"$script_dir/strip_jvm_imports.sh" "$test_dir/bundles"
second_hash=$(sha256sum "$test_dir/bundles/bundle.jar")
if [[ "$first_hash" != "$second_hash" ]]; then
  echo 'repair is not idempotent' >&2
  exit 1
fi

echo 'strip_jvm_imports_test: PASS'
