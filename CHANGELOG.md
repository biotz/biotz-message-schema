# Change Log
All notable changes to this project will be documented in this
file. This change log follows the conventions of
[keepachangelog.com](http://keepachangelog.com/).

## [UNRELEASED]

## [0.1.7] - 2023-06-23

- Allow integer values if schema is defined as decimal.

## [0.1.6] - 2023-05-25

- Fix misplacement of optional property map.
- Make meta-schema's `::properties` schema closed.

## [0.1.5] - 2023-05-23

- Fix transformation failure when object names are duplicated
- Use closed object schemas for validation

## [0.1.4] - 2023-03-28

- Refactor record-name and object-key-name schemas.

## [0.1.3] - 2023-03-23

- Fix old metadata being lost when calculating new one.
- Use same metadata type identifier for all timestamps.

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
