#!/bin/bash
# Normalize embedded OSGi bundle manifests for the Felix 4 runtime.
# java.* packages are supplied by the framework and Felix 4 rejects them in
# Import-Package.  Existing valid headers are left byte-for-byte unchanged
# unless they contain one of those imports or an invalid build-time header.

set -euo pipefail

bundle_dir=${1:?usage: strip_jvm_imports.sh DIRECTORY}

find "$bundle_dir" -maxdepth 1 -type f -name '*.jar' -print0 |
  while IFS= read -r -d '' jar_file; do
    work_dir=$(mktemp -d)
    cleanup() { rm -rf "$work_dir"; }
    trap cleanup EXIT

    unzip -qq "$jar_file" -d "$work_dir/content"
    manifest="$work_dir/content/META-INF/MANIFEST.MF"
    if [[ ! -f "$manifest" ]]; then
      cleanup
      trap - EXIT
      continue
    fi

    before_hash=$(sha256sum "$manifest")
    perl - "$manifest" <<'PERL'
use strict;
use warnings;

my ($manifest_file) = @ARGV;
local $/;
open my $input, '<', $manifest_file or die "$manifest_file: $!\n";
my $manifest = <$input>;
close $input;

# Unfold continuation lines before editing the header.  The continuation
# marker's leading space is syntax, not part of the header value.
$manifest =~ s/\r?\n[ \t]//g;
$manifest =~ s/\r\n/\n/g;

sub split_clauses {
    my ($value) = @_;
    my (@clauses, $clause, $quoted);
    $clause = '';
    for my $character (split //, $value) {
        if ($character eq '"') {
            $quoted = !$quoted;
        }
        if ($character eq ',' && !$quoted) {
            $clause =~ s/^\s+//;
            $clause =~ s/\s+$//;
            push @clauses, $clause if length $clause;
            $clause = '';
        } else {
            $clause .= $character;
        }
    }
    $clause =~ s/^\s+//;
    $clause =~ s/\s+$//;
    push @clauses, $clause if length $clause;
    return @clauses;
}

sub wrap_package_header {
    my ($name, $value) = @_;
    my @clauses = split_clauses($value);
    my $remaining = "$name: " . join(',', @clauses);
    my $result = '';
    while (length($remaining) > 70) {
        $result .= substr($remaining, 0, 70) . "\n ";
        $remaining = substr($remaining, 70);
    }
    return $result . $remaining . "\n";
}

my $changed = 0;
my @output;
my $is_jakarta = $manifest =~ /^Bundle-SymbolicName: jakarta\.xml\.bind-api$/m;
for my $line (split /\n/, $manifest, -1) {
    $line =~ s/\r$//;
    if ($line =~ /^(Import|Export)-Package:[ \t]*(.*)$/) {
        my $name = $1 . '-Package';
        my $value = $2;
        my $normalized;
        my $needs_change = 0;
        if ($name eq 'Import-Package') {
            my @original_imports = split_clauses($value);
            my @imports = grep { $_ !~ /^\s*(?:java|sun)\./ } @original_imports;
            $needs_change = @imports != @original_imports;
            if ($is_jakarta) {
                @imports = map {
                    /^jakarta\.activation;/ && !/resolution:=/ ? "$_;resolution:=optional" : $_
                } @imports;
                my $original_optional = scalar grep { /;resolution:=optional$/ } @original_imports;
                my $new_optional = scalar grep { /;resolution:=optional$/ } @imports;
                $needs_change = 1 if $new_optional != $original_optional;
            }
            $normalized = @imports ? wrap_package_header($name, join(',', @imports)) : '';
        } else {
            my @clauses = split_clauses($value);
            my @normalized_clauses = map {
                my $clause = $_;
                $clause =~ s{;uses:="([^"]*)"}{
                    my @uses = grep { $_ !~ /^(?:java|sun)\./ } split /,/, $1;
                    $needs_change = 1 if @uses != scalar split /,/, $1;
                    @uses ? ';uses:="' . join(',', @uses) . '"' : '';
                }e;
                $clause;
            } @clauses;
            $normalized = wrap_package_header($name, join(',', @normalized_clauses)) if $needs_change;
        }
        if ($needs_change) {
            $normalized =~ s/\n$//;
            $changed = 1;
            push @output, split /\n/, $normalized, -1 if length $normalized;
        } else {
            push @output, $line;
        }
    } elsif ($line =~ /^Include-Resource:/) {
        $changed = 1;
    } else {
        push @output, $line;
    }
}
$manifest = join("\n", @output);

if ($changed) {
    my @folded;
    for my $line (split /\n/, $manifest, -1) {
        while (length($line) > 70) {
            push @folded, substr($line, 0, 70);
            $line = ' ' . substr($line, 70);
        }
        push @folded, $line;
    }
    $manifest = join("\n", @folded);
    open my $output, '>', $manifest_file or die "$manifest_file: $!\n";
    print {$output} $manifest;
    close $output;
}
exit 0;
PERL
    after_hash=$(sha256sum "$manifest")
    if [[ "$before_hash" == "$after_hash" ]]; then
      cleanup
      trap - EXIT
      continue
    fi

    rebuilt="$work_dir/$(basename "$jar_file")"
    (cd "$work_dir/content" && zip -q -r "$rebuilt" .)
    mv "$rebuilt" "$jar_file"
    cleanup
    trap - EXIT
  done
