# Regex Engine

A web service that fetches content from a source, selects the parts to scan, and returns every match of a set of regex patterns.

## Run

```bash
./mvnw spring-boot:run      # http://localhost:8087/anomaly-detector
./mvnw test
```

Files for `FILE` sources are read only from `scan.fs.base-dir` (default `~/scan-files`).

## API

`POST /anomaly-detector/api/scan`

```json
{
  "source": { "type": "HTTP", "url": "https://...", "headers": { "Authorization": "Bearer <TOKEN>" } },
  "source": { "type": "FILE", "path": "relative/to/base-dir.py", "encoding": "utf-8 | latin-1 | utf-16 | ..." },
  "contentType": "RAW | NOTEBOOK",
  "patterns": ["regex", "..."]
}
```

`source` is polymorphic (one of the two shapes above, selected by `type`). `headers` and `encoding` are optional; HTTP decodes using the response charset, files default to UTF-8.

Response:

```json
{ "results": [ { "pattern": "regex", "matches": ["matched text", "..."] } ] }
```

Errors always have the shape `{"type": "<ErrorType>", "message": "..."}` with a matching HTTP status: 400 for invalid input, regex or notebook; 404 when a file is missing; 413 when content is too large; 502 when the upstream fetch fails; 500 for unexpected errors, with no internal details in the response.

### Use cases

| Use case | `source.type` | `contentType` |
|---|---|---|
| #1 Website hyperlinks | `HTTP` | `RAW` |
| #2 Source code imports | `HTTP` / `FILE` | `RAW` |
| #3 Notebook imports (cell `source` only) | `HTTP` / `FILE` | `NOTEBOOK` |

```bash
# 1 - website
curl -s localhost:8087/anomaly-detector/api/scan -H 'Content-Type: application/json' -d '{
  "source": {"type": "HTTP", "url": "https://example.com"},
  "contentType": "RAW",
  "patterns": ["<a\\s+[^>]*href=[\"'\'']?[^\"'\''\\s>]+"]}'

# 2 - source code from the filesystem, latin-1
curl -s localhost:8087/anomaly-detector/api/scan -H 'Content-Type: application/json' -d '{
  "source": {"type": "FILE", "path": "latin1.py", "encoding": "latin-1"},
  "contentType": "RAW",
  "patterns": ["^[ \\t]*(?:import|from)[ \\t]+[\\w.]+"]}'

# 3 - notebook over HTTP with custom headers
curl -s localhost:8087/anomaly-detector/api/scan -H 'Content-Type: application/json' -d '{
  "source": {"type": "HTTP",
             "url": "https://raw.githubusercontent.com/jupyter/notebook/main/docs/source/examples/Notebook/Running%20Code.ipynb",
             "headers": {"User-Agent": "regex-engine/1.0"}},
  "contentType": "NOTEBOOK",
  "patterns": ["^[ \\t]*(?:import|from)[ \\t]+[\\w.]+"]}'
```

## Design

```
ScanRequest ─▶ ContentFetcher ─▶ ContentExtractor ─▶ RegexMatcher ─▶ ScanResponse
               (by source.type)   (by contentType)
```

The three use cases are combinations of two independent concerns:

- **Where the content comes from**: `ContentFetcher`, implemented by `HttpContentFetcher` (headers, timeouts, size cap) and `FileSystemContentFetcher` (encoding, base-dir guard).
- **Which part of it is scanned**: `ContentExtractor`, implemented by `RawTextExtractor` (HTML and code as-is) and `NotebookExtractor` (each cell's `source`, which is a string or an array of lines; `outputs` are ignored).

`ScanService` receives every implementation through Spring and picks the one whose `supports(...)` returns true. Adding a new source (S3, Git) means one new `SourceDTO` record and one fetcher `@Component`; a new format (PDF, YAML) means one enum value and one extractor. The pipeline and the controller stay unchanged.

Patterns are compiled before any content is fetched, so an invalid regex fails fast without a network call. Notebook cells are matched separately, so a match never spans two cells.

Cross-cutting concerns live under `common/`: `AppException` with an `ErrorType` enum that owns the message and the HTTP status, a single `@RestControllerAdvice`, and an AOP aspect that logs each request's entry, exit and duration.

## Security considerations

The service fetches arbitrary URLs and files and runs caller-supplied regexes, so the following is in place:

- **SSRF**: only absolute `http`/`https` URLs are accepted, with connect and read timeouts. *Future work:* resolve the host and block private, loopback and link-local ranges (including cloud metadata `169.254.169.254`) on every redirect hop.
- **Path traversal**: the file path is normalized and must stay inside `scan.fs.base-dir`. *Future work:* resolve symlinks with `toRealPath()`.
- **Resource limits**: `scan.max-content-bytes` applies to both sources, and pattern count and length are capped. *Future work:* skip empty matches and cap matches per pattern (a pattern like `x*` returns one empty match per character).
- **ReDoS**: limits reduce but do not eliminate catastrophic backtracking. *Future work:* a per-scan deadline (a `CharSequence` wrapper that throws after a timeout), or a linear-time engine such as RE2/J.

## Possible extensions

- Async or batch scans over many sources, with results persisted.
- Named-group extraction (for example, return only the package name).
- An `HtmlExtractor` that scans only `href` attribute values through a real parser, instead of regex over HTML.
