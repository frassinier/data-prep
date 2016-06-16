// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.cache;

import org.talend.dataprep.cache.ContentCacheKey;

/**
 * Content cache key used to cache transformation.
 */
public class TransformationMetadataCacheKey implements ContentCacheKey {

    private final String preparationId;

    private final String stepId;

    public TransformationMetadataCacheKey(String preparationId, String stepId) {
        this.preparationId = preparationId;
        this.stepId = stepId;
    }

    @Override
    public String getKey() {
        return "transformation-metadata-" + preparationId + "-" + stepId;
    }
}
