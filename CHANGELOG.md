# Change Log
All notable changes to this project will be documented in this
file. This change log follows the conventions of
[keepachangelog.com](http://keepachangelog.com/).

## [UNRELEASED]

- Add `has-timestamp-record?` function.

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
