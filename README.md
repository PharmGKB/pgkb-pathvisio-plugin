# PGKB PathVisio Plugin

Everything needed to build the PGKB PathVisio plugin and the PGKB PathVisio distribution
(PathVisio bundled with PharmGKB's plugin).


## Building

- `ant test` — compiles and runs the test suite
- `ant dist` — builds `build/dist/pgkb-pathvisio.zip`, the Windows/generic distribution
- `ant pathvisio-mac` / `ant pathvisio-mac-bundle` — builds the Mac `.app` bundle; macOS only

`dist` rewrites `pathvisio.jar`'s manifest classpath: it drops libraries stock PathVisio ships but
this plugin doesn't need, and adds the libraries this plugin does need (see `lib/VERSIONS.txt` for
what's vendored and why).

Update-checks inside the running plugin query
`https://api.github.com/repos/PharmGKB/pgkb-pathvisio-plugin/releases/latest` (see
`DownloadUtils`) and compare it against the version embedded in the build.


### Mac

We're using a forked version of [appbundler](https://bitbucket.org/infinitekind/appbundler) to build the Mac app.
It has better support for OS X but requires that the JRE be included in the app.

Things to bear in mind:
* the app's executable is stored in `/Contents/MacOS/` and must have execute permissions
* PathVisio is registered as the editor for `.gpml` files and while double clicking on those files will open PathVisio,
  it will not load the file because it does not implement `com.apple.eawt.OpenFilesHandler`.
