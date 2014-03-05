# PGKB PathVisio Plugin

This contains everything needed to build the PGKB PathVisio plugin and create the PGKB PathVisio distribution.

We are relying on a custom build of PathVisio 2.0.11 which adds `PathwayElementEvent.getProperty()`.  This is critical for us to support what we're doing with the `PropertyDisplayManager` (i.e. the right info panel).



## Building

Run: `ant pathvisio`

This will generate `build/dist/pgkb-pathvisio.zip` which should be uploaded to [the wiki](http://wiki.pharmgkb.org/display/PUB/PharmGKB+Plugin+for+PathVisio).

Note that we're rewriting the classpath in `pathvisio.jar`'s manifest file.  We are:
* changing the location it expects the libraries to be in
* removing libraries PathVisio needs but we don't (i.e. unused functionality)
* adding libraries we need