package com.noma.assignment.scan.fetch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import com.noma.assignment.config.ScanProperties;
import com.noma.assignment.scan.api.dto.FileSource;
import com.noma.assignment.scan.api.dto.SourceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Reads files only from inside the configured base directory. */
@Component
@RequiredArgsConstructor
public class FileSystemContentFetcher implements ContentFetcher {

    private final ScanProperties props;

    @Override
    public boolean supports(SourceDTO source) {
        return source instanceof FileSource;
    }

    @Override
    public String fetch(SourceDTO source) {
        FileSource file = (FileSource) source;
        Path path = resolveInsideBaseDir(file.path());
        try {
            if (Files.size(path) > props.maxContentBytes()) {
                throw new AppException(ErrorType.CONTENT_TOO_LARGE, props.maxContentBytes());
            }
            return new String(Files.readAllBytes(path), file.charset());
        } catch (NoSuchFileException e) {
            throw new AppException(ErrorType.NOT_FOUND, "File", file.path());
        } catch (IOException e) {
            throw new AppException(ErrorType.FETCH_FAILED, e, file.path());
        }
    }

    private Path resolveInsideBaseDir(String location) {
        Path baseDir = props.fs().baseDir();
        try {
            Path path = baseDir.resolve(location).normalize();
            if (!path.startsWith(baseDir)) {
                throw new AppException(ErrorType.INVALID_ARGUMENT, "path is outside the allowed directory: " + location);
            }
            return path;
        } catch (InvalidPathException e) {
            throw new AppException(ErrorType.INVALID_ARGUMENT, e, "invalid path: " + location);
        }
    }
}
