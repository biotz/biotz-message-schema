# Change Log
All notable changes to this project will be documented in this
file. This change log follows the conventions of
[keepachangelog.com](http://keepachangelog.com/).

## [UNRELEASED]

- Allow negative values in "Unix timestamp, as string" data type, for symmetry with "Unix timestamp, as integer" data type.
- Unix timestamp RFC-3339 now allows lowercase "T" and "Z" characters, as suggested in https://www.rfc-editor.org/rfc/rfc3339#section-5.6
- Enforce limits for Integer data types (including the text-based representation). The previous validation rules allowed those data types to accept values that didn't fit in a 32 bits integer.

## [0.1.15] - 2024-04-12

- Rename 'offset+scale' to 'scale+offset'

## [0.1.14] - 2024-04-11

- Add 'boolean-as-string' and 'boolean-as-integer' schemas.
- Rename 'metadata-type' to 'canonical-type'
- Improve meta-schema to better handle type specific props
- Fix boolean-as-string schema not working in CLJS
- Implement first version of offset + scale transformation

## [0.1.13] - 2024-02-16

- Upgrade dependencies

## [0.1.12] - 2024-01-18

- Fix 'validate-message-data' and 'explain-message-data' not using closed Malli schemas.
- Remove 'build-message-data-validator' function. It's not really needed anywhere.
- Remove 'validate-message-schema' and 'explain-message-schema'. After removing the unneeded registry reference they were a 1-1 mapping to raw Malli validate/explain functions.

## [0.1.11] - 2024-01-15

- Fix 'record-name' uniqueness check.

## [0.1.10] - 2024-01-15

- Add additional validity checks to the meta-schema.

## [0.1.9] - 2023-12-11

- Add `base10-integer-as-string`, `base10-decimal-as-string`, and `base16-integer-as-string` schemas.

## [0.1.8] - 2023-06-29

- BREAKING CHANGE: Split `meta-schema` into two different versions: `msg-type-schema-meta-schema` and `action-msg-type-schema-meta-schema`. The difference is that the former can have a property map on the leaf nodes (with `:record-name` and `:record-timestamp`. The latter don't have it.
- BREAKING CHANGE: `validate-message-schema` and `explain-message-schema` now are functions of 2 arity. The first being the `meta-schema` (one of the explained above) and the second the `schema` to be validated.

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
