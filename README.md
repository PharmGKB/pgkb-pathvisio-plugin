# PGKB PathVisio Plugin

This contains everything needed to build the PGKB PathVisio plugin and create the PGKB PathVisio distribution.


## Building

Run: `ant pathvisio`

This will generate `build/dist/pgkb-pathvisio.*`.

Note that we're rewriting the classpath in `pathvisio.jar`'s manifest file.  We are:
* changing the location it expects the libraries to be in
* removing libraries PathVisio needs but we don't (i.e. unused functionality)
* adding libraries we need


### Mac

We're using a forked version of [appbundler](https://bitbucket.org/infinitekind/appbundler) to build the Mac app.  It has better support for OS X but requires that the JRE be included in the app.

Things to bear in mind:
* the app's executable is stored in `/Contents/MacOS/` and must have execute permissions
* PathVisio is registered as the editor for `.gpml` files and while double clicking on those files will open PathVisio, it will not load the file because it does not implement `com.apple.eawt.OpenFilesHandler`.
