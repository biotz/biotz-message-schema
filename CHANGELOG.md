# Change Log
All notable changes to this project will be documented in this
file. This change log follows the conventions of
[keepachangelog.com](http://keepachangelog.com/).

## [UNRELEASED]

## [0.1.2] - 2023-03-03

- Add `has-timestamp-record?` function.
- Update `curl` package version in Dockerfile
- Add `criterium` benchmarking library for dev purposes
- Redefine Malli simple-types as simple-schemas
- Add functions to derive data model metadata from schemas
- Ignore IntelliJ IDEA local files from .gitignore

## [0.1.1] - 2023-02-27

- Change artifact name to use reverse domain artifact name.
- Remove unnecessary piggieback dependency and repl-middleware.
- Add io.biotz.message-schema.edn namespace to read and write biotz
  message-schemas from/to edn/string.
- Add `unix-timestamp` and `rfc-3339-timestamp` schema types.
- Add `unix-timestamp` implementation variants, one for integers and
  another for strings.
- Add better predicate using regular expressions for `rfc-3339-timestamp`.

## [0.1.0] - 2023-02-22

- Initial stable version.
