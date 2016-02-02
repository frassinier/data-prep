package org.talend.dataprep.schema;

import org.talend.dataprep.api.dataset.DataSetMetadata;

/**
 * Update the schema from an updated metadata.
 */
public interface SchemaUpdater {

    /**
     * Return true if this schema updated accepts this metadata.
     *
     * @param metadata the metadata to update.
     * @return true if this schema updater can update the given metadata.
     */
    boolean accept(DataSetMetadata metadata);

    /**
     * Update the schema of the given metadata.
     *
     * @param request the schema parser request.
     * @return the format guesser result.
     */
    FormatGuesser.Result updateSchema(SchemaParser.Request request);

    /**
     * @return the format guess.
     */
    FormatGuess getFormatGuess();

}