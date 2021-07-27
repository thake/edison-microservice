package de.otto.edison.jobs.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Meta information about a job.
 *
 * @since 1.0.0
 */
public final class JobMeta {

    private final String jobType;
    private final boolean running;
    private final boolean disabled;
    private final String disabledComment;
    private final Map<String,String> meta;


    public JobMeta() {
        jobType = null;
        disabled = false;
        meta = new HashMap<>();
        disabledComment = null;
        running = false;
    }

    public JobMeta(final String jobType,
                   final boolean running,
                   final boolean disabled,
                   final String disabledComment,
                   final Map<String,String> meta) {
        this.jobType = jobType;
        this.running = running;
        this.disabled = disabled;
        this.disabledComment = disabledComment != null ? disabledComment : "";
        this.meta = unmodifiableMap(meta);
    }

    private JobMeta(Builder builder) {
        jobType = builder.jobType;
        running = builder.running;
        disabled = builder.disabled;
        disabledComment = builder.disabledComment;
        meta = builder.meta;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(JobMeta copy) {
        Builder builder = new Builder();
        builder.jobType = copy.getJobType();
        builder.running = copy.isRunning();
        builder.disabled = copy.isDisabled();
        builder.disabledComment = copy.getDisabledComment();
        builder.meta = copy.getMeta();
        return builder;
    }

    public String getJobType() {
        return jobType;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public String getDisabledComment() {
        return disabledComment;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public String get(final String key) {
        return meta.get(key);
    }

    public Map<String,String> getAll() {
        return meta;
    }


    public static final class Builder {
        private String jobType;
        private boolean running;
        private boolean disabled;
        private String disabledComment;
        private Map<String, String> meta;

        private Builder() {
        }

        public Builder withJobType(String val) {
            jobType = val;
            return this;
        }

        public Builder withRunning(boolean val) {
            running = val;
            return this;
        }

        public Builder withDisabled(boolean val) {
            disabled = val;
            return this;
        }

        public Builder withDisabledComment(String val) {
            disabledComment = val;
            return this;
        }

        public Builder withMeta(Map<String, String> val) {
            meta = val;
            return this;
        }

        public JobMeta build() {
            return new JobMeta(this);
        }
    }
}