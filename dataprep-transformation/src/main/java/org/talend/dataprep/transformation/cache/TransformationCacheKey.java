package org.talend.dataprep.transformation.cache;

import java.io.IOException;
import java.util.Objects;

import org.springframework.util.DigestUtils;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.cache.ContentCacheKey;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Content cache key used to cache transformation.
 */
public class TransformationCacheKey implements ContentCacheKey {

    /** The dataset id. */
    private String datasetId;

    /** The dataset metadata hash. */
    private String datasetMetadataHash;

    /** The preparation id. */
    private String preparationId;

    /** The optional step id. */
    private String stepId;

    /** The sample size (default is 'full'). */
    private String sample = "full";

    /** The transformation format. */
    private String format;

    /**
     * Create a content cache for this transformation.
     *
     * @param preparationId the preparation id.
     * @param metadata the dataset metadata.
     * @param format the transformation format.
     * @param stepId the preparation version (step).
     * @throws IOException if an error occurs while computing the cache key.
     */
    public TransformationCacheKey(String preparationId, DataSetMetadata metadata, String format, String stepId)
            throws IOException {
        this.preparationId = preparationId;
        this.datasetId = metadata.getId();
        this.datasetMetadataHash = hash(metadata);
        this.format = format;
        this.stepId = stepId;
    }

    /**
     * Create a content cache key that only matches the given dataset id.
     *
     * @param preparationId the preparation id.
     * @param metadata the dataset metadata.
     * @param format the transformation format.
     * @param stepId the preparation version (step).
     * @throws IOException if an error occurs while computing the cache key.
     */
    public TransformationCacheKey(String preparationId, DataSetMetadata metadata, String format, String stepId, Long sample)
            throws IOException {
        this(preparationId, metadata, format, stepId);
        this.sample = String.valueOf(sample);
    }

    /**
     * @param metadata the dataset metadata.
     * @return the md5 hash of the given dataset metadata.
     * @throws IOException if an error occurs while computing the cache key.
     */
    private String hash(DataSetMetadata metadata) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writer().writeValueAsString(metadata);
        return DigestUtils.md5DigestAsHex(json.getBytes("UTF-8"));
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "TransformationCacheKey{" + "datasetId='" + datasetId + '\'' + ", datasetMetadataHash='" + datasetMetadataHash
                + '\'' + ", preparationId='" + preparationId + '\'' + ", stepId='" + stepId + '\'' + ", sample='" + sample + '\''
                + ", format='" + format + '\'' + '}';
    }

    /**
     * The key must be unique per content !
     *
     * @return the key for this cache content as a string.
     */
    @Override
    public String getKey() {
        return "transformation-" + Objects.hash(preparationId, datasetId, datasetMetadataHash, stepId, sample, format);
    }
}
