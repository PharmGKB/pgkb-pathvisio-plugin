# OSGi / Apache Felix 4 manifest repairs

The packaged PathVisio runtime uses Apache Felix 4.6.0. Felix 4 rejects
`java.*` and `sun.*` packages in `Import-Package`, although those packages
are supplied by the Java 8 framework. Newer bnd-generated manifests also
contain `Include-Resource`, which is a build instruction rather than a
runtime OSGi header and is not valid for this Felix runtime.

## Repaired artifacts

The repairs are committed to the artifacts so normal builds do not mutate
JAR manifests:

* `lib/commons-beanutils.jar`: removed `java.*` imports and `Include-Resource`.
* `lib/commons-codec.jar`: removed `java.*` imports and `Include-Resource`.
* `lib/commons-collections.jar`: removed `Include-Resource`.
* `lib/commons-email.jar`: removed `Include-Resource`.
* `lib/commons-io.jar`: removed `java.*`/`sun.*` imports and `Include-Resource`.
* `lib/commons-lang3.jar`: removed `java.*` imports and `Include-Resource`.
* `lib/commons-text.jar`: removed `java.*` imports and `Include-Resource`.
* `lib/gson.jar`: removed the optional `sun.misc` import.
* `lib/guava.jar`: removed the optional `sun.misc` import.
* `lib/jakarta.xml.bind-api.jar`: marked its `jakarta.activation` import
  optional because no activation bundle is shipped.
* `lib/javax.mail.jar`: removed the optional `sun.security.util` import.
* `lib/jspecify.jar`: removed `java.*` imports.
* `lib/logback-classic.jar`: removed the optional `sun.reflect` import.
* `lib/build/pathvisio.jar`: replaced its embedded
  `org.pathvisio.xerces.jar` with a copy whose `sun.io` imports were removed.

The plugin bundle manifest also excludes `java.*` during bnd generation in
`ant.bnd`. Its resource list uses bnd's `-includeresource` instruction so the
build instruction is not emitted as an invalid runtime header. These are
separate from the vendored-JAR repairs.

## Repeat the repair after upgrading a dependency

Use Java 8 for the project build:

```bash
export JAVA_HOME="$JAVA08_HOME"
export PATH="$JAVA_HOME/bin:$PATH"
```

For top-level library JARs, place the pristine upgraded JARs in a temporary
directory and run:

```bash
mkdir -p /tmp/osgi-repair/jars
cp lib/commons-io.jar /tmp/osgi-repair/jars/
bash bin/strip_jvm_imports.sh /tmp/osgi-repair/jars
cp /tmp/osgi-repair/jars/commons-io.jar lib/commons-io.jar
```

The helper scans only the immediate `*.jar` files in the supplied directory.
It is idempotent and changes only offending `Import-Package` clauses,
`jakarta.activation` optionality for the JAXB bundle, and
`Include-Resource`. Inspect the manifest before replacing the committed JAR
and add a corresponding entry to `lib/VERSIONS.txt`.

For a bundle embedded in `lib/build/pathvisio.jar`, unpack the outer JAR,
run the helper on the directory containing the embedded JARs, then rebuild
the outer JAR:

```bash
mkdir -p /tmp/osgi-repair/pathvisio
unzip lib/build/pathvisio.jar -d /tmp/osgi-repair/pathvisio
bash bin/strip_jvm_imports.sh /tmp/osgi-repair/pathvisio
(cd /tmp/osgi-repair/pathvisio && zip -q -r /tmp/osgi-repair/pathvisio.jar .)
cp /tmp/osgi-repair/pathvisio.jar lib/build/pathvisio.jar
```

Document both the embedded JAR and the containing artifact when this process
changes a nested bundle. Do not add the helper back to `ant dist`: the
committed artifacts are the build inputs and the normal build should remain
reproducible.

## Verification

Run the helper regression test and build the unpacked distribution:

```bash
bash bin/strip_jvm_imports_test.sh
ant unpack-dist
```

The application can then be launched with:

```bash
bash build/pgkb-pathvisio/pathvisio.sh
```

This final launch is a GUI test. A display server is required; in a headless
environment the launch stops at AWT initialization before Felix starts.
