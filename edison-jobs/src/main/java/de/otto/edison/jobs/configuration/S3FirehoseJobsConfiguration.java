package de.otto.edison.jobs.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.s3firehose.S3FirehoseJobMetaRepository;
import de.otto.edison.jobs.repository.s3firehose.S3FirehoseJobRepository;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnProperty(prefix = "edison.jobs", name = "s3Firehose.enabled", havingValue = "true", matchIfMissing = true)
public class S3FirehoseJobsConfiguration {

    @Bean
    public JobRepository jobRepository(final JobsProperties jobsProperties, final S3Client s3Client, final ObjectMapper objectMapper) {
        return new S3FirehoseJobRepository(jobsProperties.getS3Firehose(), s3Client, objectMapper);
    }

    @Bean
    public JobMetaRepository jobMetaRepository(final JobsProperties jobsProperties, final S3Client s3Client, final ObjectMapper objectMapper) {
        return new S3FirehoseJobMetaRepository(jobsProperties.getS3Firehose(), s3Client, objectMapper);
    }

}
