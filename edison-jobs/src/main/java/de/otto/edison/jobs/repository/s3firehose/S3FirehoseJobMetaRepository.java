package de.otto.edison.jobs.repository.s3firehose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.jobs.configuration.JobsProperties;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

public class S3FirehoseJobMetaRepository extends AbstractS3Client implements JobMetaRepository {

    public static final String JOB_META_PREFIX = "job-meta/";
    private final Map<String, Map<String, String>> map = new ConcurrentHashMap<>();
    private static final String KEY_DISABLED = "_e_disabled";
    private static final String KEY_RUNNING = "_e_running";

    public S3FirehoseJobMetaRepository(JobsProperties.S3Firehose s3Firehose, S3Client s3Client, ObjectMapper objectMapper) {
        super(s3Client, s3Firehose.getBucketName(), objectMapper);
    }

    @Override
    public String getRunningJob(final String jobType) {
        return getValue(jobType, KEY_RUNNING);
    }

    @Override
    public boolean setRunningJob(final String jobType, final String jobId) {
        return createValue(jobType, KEY_RUNNING, jobId);
    }

    /**
     * Clears the job running mark of the jobType. Does nothing if not mark exists.
     *
     * @param jobType the job type
     */
    @Override
    public void clearRunningJob(final String jobType) {
        setValue(jobType, KEY_RUNNING, null);
    }

    /**
     * Reenables a job type that was disabled
     *
     * @param jobType the enabled job type
     */
    @Override
    public void enable(final String jobType) {
        var jobMeta = getJobMeta(jobType);
        JobMeta updatedMeta = JobMeta.newBuilder(jobMeta)
                .withDisabled(false)
                .withDisabledComment(null)
                .build();
        updateS3Content(JOB_META_PREFIX + jobType, updatedMeta);
    }

    /**
     * Disables a job type, i.e. prevents it from being started
     *
     * @param jobType the disabled job type
     * @param comment an optional comment
     */
    @Override
    public void disable(final String jobType, final String comment) {
        var jobMeta = getJobMeta(jobType);
        JobMeta updatedMeta = JobMeta.newBuilder(jobMeta)
                .withDisabled(true)
                .withDisabledComment(comment)
                .build();
        updateS3Content(JOB_META_PREFIX + jobType, updatedMeta);
    }

    @Override
    public String setValue(String jobType, String key, String value) {
        var jobMeta = getJobMeta(jobType);

        Map<String, String> meta = new HashMap<>(jobMeta.getMeta());
        meta.put(key, value);
        JobMeta updatedMeta = JobMeta.newBuilder(jobMeta)
                .withMeta(meta)
                .build();
        updateS3Content(JOB_META_PREFIX + jobType, updatedMeta);
        return value;
    }

    @Override
    public String getValue(String jobType, String key) {
        var meta = getJobMeta(jobType);
        return meta.get(key);
    }

    /**
     * Returns all job types matching the specified predicate.
     *
     * @return set containing matching job types.
     */
    @Override
    public Set<String> findAllJobTypes() {
        return map.keySet();
    }

    /**
     * Deletes all information from the repository.
     */
    @Override
    public void deleteAll() {
        map.clear();
    }

    /**
     * Returns the current state of the specified job type.
     *
     * @param jobType the job type
     * @return current state of the job type
     */
    @Override
    public JobMeta getJobMeta(String jobType) {
        var jobMeta = getS3Content(JOB_META_PREFIX + jobType, JobMeta.class);
        return jobMeta.orElse(new JobMeta(jobType, false, false, "", emptyMap()));
    }

    @Override
    public boolean createValue(String jobType, String key, String value) {
        if (isNull(getValue(jobType, key))) {
            setValue(jobType, key, value);
            return true;
        }
       return false;
    }

    @Override
    public String toString() {
        return "S3FirehoseRepository";
    }

}
