package de.otto.edison.jobs.repository.s3firehose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.IOUtils;
import org.slf4j.Logger;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class AbstractS3Client {

    private final S3Client s3Client;
    private final String bucketName;
    private final ObjectMapper objectMapper;

    private static final Logger LOG = getLogger(AbstractS3Client.class);

    AbstractS3Client(S3Client s3Client, String bucketName, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.objectMapper = objectMapper;
    }

    public <T> void updateS3Content(String key, T content) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.fromString(objectMapper.writeValueAsString(content));
        } catch (JsonProcessingException e) {
            LOG.error("error while trying to parse the object {} to string", content);
        }

        s3Client.putObject(putObjectRequest, requestBody);
    }

    public <T> Optional<T> getS3Content(String key, Class<T> clazzType) {
        String objJson = null;
        try {
            final var objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            final var inputStream = s3Client.getObject(objectRequest);
            objJson = IOUtils.toString(inputStream);
            return Optional.of(objectMapper.readValue(objJson, clazzType));
        } catch (NoSuchKeyException e) {
            LOG.warn("no key for {} found, will be created later on", key);
        } catch (JsonProcessingException e) {
            LOG.error("error while trying to parse the string {} to object", objJson);
        }
        return Optional.empty();
    }
}
