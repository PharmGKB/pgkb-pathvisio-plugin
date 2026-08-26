# PGKB PathVisio Plugin

This contains everything needed to build the PGKB PathVisio plugin and create the PGKB PathVisio distribution.


## Building

Run from the repository root (not from this directory): `ant pathvisio`

This will generate `build/dist/pgkb-pathvisio.*`. (This module's own `build.xml` here defines
`dist`, not `pathvisio` — the root target delegates to it.)

Note that we're rewriting the classpath in `pathvisio.jar`'s manifest file.  We are:
* changing the location it expects the libraries to be in
* removing libraries PathVisio needs but we don't (i.e. unused functionality)
* adding libraries we need


### Mac

We're using a forked version of [appbundler](https://bitbucket.org/infinitekind/appbundler) to build the Mac app.  It has better support for OS X but requires that the JRE be included in the app.

Things to bear in mind:
* the app's executable is stored in `/Contents/MacOS/` and must have execute permissions
* PathVisio is registered as the editor for `.gpml` files and while double clicking on those files will open PathVisio, it will not load the file because it does not implement `com.apple.eawt.OpenFilesHandler`.


## Relationship to pgkb-core

This module's `src/main/java/org/pharmgkb/{pathvisio,exception,model,model/pathway}/` classes
(21 files — everything from the former `pgkb-core-pv` module except `LogbackConstants`,
`GpmlFileUtils`, and `DataSource`) are independent duplicates of the same-named classes in
`pgkb-core` — kept here because this module must stay Java 8-compatible while `pgkb-core` is
Java 17. If you change the shared logic in one of these classes in `pgkb-core`, check whether the
change needs to be manually backported here too.

`LinkOutResource`'s duplicate is a *trimmed* copy, not byte-identical to `pgkb-core`'s — see its
class comment for exactly which properties/methods were dropped as unused by this module (e.g. the
`DataSource` per-constant property, and the `DataSource` enum itself, since nothing else here used
it either). If you backport a `pgkb-core` change to it, re-check whether the change touches a
property this copy no longer carries.

The test suite under `src/test` has the same split: `GpmlValidatorTest`/`PathvisioUtilsTest` are
duplicates of `pgkb-core`'s copies (apply the same backport rule), while `BasicTestUtils` here is
a deliberately trimmed fork of `pgkb-core`'s version, not a duplicate — see the class comment.


## Relationship to pgkb-common

This module also carries its own copies of the handful of `pgkb-common` (`org.pharmgkb.common.util`)
classes it actually needs, instead of compiling `pgkb-common`'s source directly — same Java 8
reason as above. `src/main/java/org/pharmgkb/common/util/` has `ExtendedEnum`/`ExtendedEnumHelper`/
`ExtendedEnumConverter` (backing the enum-based model classes) and `UrlUtils` (used by
`GpmlValidator`); `src/test/java/org/pharmgkb/common/util/` has `AnsiConsole`/`PathUtils` (test
infrastructure only). `StreamUtils` isn't duplicated at all — its only method this module ever
called, `copyUrlToFile()`, was inlined directly into
`src/main/java/org/pharmgkb/pathvisio/plugin/DownloadUtils.java` (its only caller) as a private
method instead, since nothing else needed it as shared utility code; that also made
`ZippedFileInputStream` (only ever constructed by `StreamUtils`' now-removed gz/zip-aware openers)
fully unused, so it was dropped too. These remaining classes are unmodified copies of
`pgkb-common`'s classes — apply the same backport rule as the `pgkb-core` duplicates above if the
`pgkb-common` originals change.
