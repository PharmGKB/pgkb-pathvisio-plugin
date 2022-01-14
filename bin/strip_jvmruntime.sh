#!/bin/bash
#
# When built by GitHub Actions, the Info.plist file contains a JVMRuntime entry (x64) that breaks things.
# This strips it out.
#

mv "${1}/Contents/Info.plist" "${1}/Contents/Info.plist.orig"
cat "${1}/Contents/Info.plist.orig" | tr '\n' '\r' | sed -e 's/ *<key>JVMRuntime<\/key>\r *<string>x64<\/string>\r//' | tr '\r' '\n' > "${1}/Contents/Info.plist"
