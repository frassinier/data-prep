//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.exception.error;

import static org.springframework.http.HttpStatus.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.error.ErrorCode;

/**
 * Transformation error codes.
 */
public enum TransformationErrorCodes implements ErrorCode {
    // 400
    BAD_LOOKUP_PARAMETER(BAD_REQUEST),
    UNABLE_TO_USE_EXPORT(BAD_REQUEST, "format"),
    EXPORT_IN_PROGRESS(BAD_REQUEST),
    // 403
    INSUFFICIENT_ROLE(FORBIDDEN),
    // 404
    UNKNOWN_DYNAMIC_ACTION(NOT_FOUND, "value"),
    // 415
    OUTPUT_TYPE_NOT_SUPPORTED(UNSUPPORTED_MEDIA_TYPE),
    // 500
    UNABLE_TO_COMPUTE_DATASET_ACTIONS(INTERNAL_SERVER_ERROR),
    UNABLE_TO_TRANSFORM_DATASET(INTERNAL_SERVER_ERROR),
    UNEXPECTED_EXCEPTION(INTERNAL_SERVER_ERROR),
    UNABLE_TO_READ_LOOKUP_DATASET(INTERNAL_SERVER_ERROR),
    UNABLE_TO_PERFORM_PREVIEW(INTERNAL_SERVER_ERROR);
    /** The http status to use. */
    private int httpStatus;

    /** Expected entries to be in the context. */
    private List<String> expectedContextEntries;

    /**
     * default constructor.
     * 
     * @param httpStatus the http status to use.
     */
    TransformationErrorCodes(final HttpStatus httpStatus) {
        this.httpStatus = httpStatus.value();
        this.expectedContextEntries = Collections.emptyList();
    }

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     * @param contextEntries expected context entries.
     */
    TransformationErrorCodes(final HttpStatus httpStatus, final String... contextEntries) {
        this.httpStatus = httpStatus.value();
        this.expectedContextEntries = Arrays.asList(contextEntries);
    }

    /**
     * @return the product.
     */
    @Override
    public String getGroup() {
        return "TS"; //$NON-NLS-1$
    }

    /**
     * @return the group.
     */
    @Override
    public String getProduct() {
        return "TDP"; //$NON-NLS-1$
    }

    /**
     * @return the http status.
     */
    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * @return the expected context entries.
     */
    @Override
    public Collection<String> getExpectedContextEntries() {
        return expectedContextEntries;
    }

    @Override
    public String getCode() {
        return this.toString();
    }
}
