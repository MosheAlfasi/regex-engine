package com.noma.assignment.scan.fetch;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import com.noma.assignment.config.ScanProperties;
import com.noma.assignment.scan.api.dto.HttpSource;
import com.noma.assignment.scan.api.dto.SourceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class HttpContentFetcher implements ContentFetcher {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    private final RestClient restClient;
    private final ScanProperties props;

    @Override
    public boolean supports(SourceDTO source) {
        return source instanceof HttpSource;
    }

    @Override
    public String fetch(SourceDTO source) {
        HttpSource http = (HttpSource) source;
        URI uri = toUri(http.url());
        try {
            return restClient.get()
                    .uri(uri)
                    .headers(h -> http.headers().forEach(h::set))
                    .exchange((request, response) -> {
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            throw new AppException(ErrorType.FETCH_FAILED, uri);
                        }
                        Charset charset = Optional.ofNullable(response.getHeaders().getContentType())
                                .map(MediaType::getCharset)
                                .orElse(StandardCharsets.UTF_8);
                        try (InputStream body = response.getBody()) {
                            byte[] bytes = body.readNBytes(Math.toIntExact(props.maxContentBytes() + 1));
                            if (bytes.length > props.maxContentBytes()) {
                                throw new AppException(ErrorType.CONTENT_TOO_LARGE, props.maxContentBytes());
                            }
                            return new String(bytes, charset);
                        }
                    });
        } catch (RestClientException e) {
            throw new AppException(ErrorType.FETCH_FAILED, e, uri);
        }
    }

    private static URI toUri(String location) {
        try {
            URI uri = URI.create(location);
            if (uri.getScheme() == null || !ALLOWED_SCHEMES.contains(uri.getScheme().toLowerCase()) || uri.getHost() == null) {
                throw new AppException(ErrorType.INVALID_ARGUMENT, "only absolute http/https URLs are allowed: " + location);
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorType.INVALID_ARGUMENT, e, "malformed URL: " + location);
        }
    }
}
