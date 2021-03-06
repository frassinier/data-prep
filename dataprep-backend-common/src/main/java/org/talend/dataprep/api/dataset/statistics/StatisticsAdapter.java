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

package org.talend.dataprep.api.dataset.statistics;

import static java.util.Locale.ENGLISH;
import static org.talend.dataprep.api.type.Type.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.Quality;
import org.talend.dataprep.api.dataset.location.SemanticDomain;
import org.talend.dataprep.api.dataset.statistics.date.DateHistogram;
import org.talend.dataprep.api.dataset.statistics.date.StreamDateHistogramStatistics;
import org.talend.dataprep.api.dataset.statistics.number.NumberHistogram;
import org.talend.dataprep.api.dataset.statistics.number.StreamNumberHistogramStatistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.api.type.TypeUtils;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.recognizer.CategoryFrequency;
import org.talend.dataquality.semantic.statistics.SemanticType;
import org.talend.dataquality.statistics.cardinality.CardinalityStatistics;
import org.talend.dataquality.statistics.frequency.DataTypeFrequencyStatistics;
import org.talend.dataquality.statistics.frequency.pattern.PatternFrequencyStatistics;
import org.talend.dataquality.statistics.numeric.quantile.QuantileStatistics;
import org.talend.dataquality.statistics.numeric.summary.SummaryStatistics;
import org.talend.dataquality.statistics.text.TextLengthStatistics;
import org.talend.dataquality.statistics.type.DataTypeEnum;
import org.talend.dataquality.statistics.type.DataTypeOccurences;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.common.inference.ValueQualityStatistics;

/**
 * Statistics adapter. This is used to inject every statistics part in the columns metadata.
 */
@Component
public class StatisticsAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsAdapter.class);

    /**
     * Defines the minimum threshold for a semantic type suggestion. Defaults to 40% if not defined.
     */
    @Value("#{'${semantic.threshold:40}'}")
    private int semanticThreshold;

    /**
     * Extract analysis result and inject them in columns metadata
     * 
     * @param columns The columns metadata
     * @param results The analysis results
     * @see #adapt(List, List, Predicate) to filter out columns during extraction of results.
     */
    public void adapt(List<ColumnMetadata> columns, List<Analyzers.Result> results) {
        adapt(columns, results, c -> true);
    }

    /**
     * Extract analysis result and inject them in columns metadata
     *
     * @param columns The columns metadata
     * @param results The analysis results
     * @param filter A {@link Predicate predicate} to filter columns to adapt.
     */
    public void adapt(List<ColumnMetadata> columns, List<Analyzers.Result> results, Predicate<ColumnMetadata> filter) {
        genericAdapt(columns, results, filter);
    }

    /**
     * Extract analysis result and inject them in columns metadata. This method allows to use a subtype, when possible,
     * of the actual type that have been detected. This methods does not filter any column.
     *
     * @param columns The columns metadata
     * @param results The analysis results
     * @see #adapt(List, List, Predicate) to filter out columns during extraction of results.
     */
    public void adaptForSampling(List<ColumnMetadata> columns, List<Analyzers.Result> results) {
        adaptForSampling(columns, results, c -> true);
    }

    /**
     * Extract analysis result and inject them in columns metadata. This method allows to use a subtype, when possible,
     * of the actual type that have been detected.
     * 
     * @param columns The columns metadata
     * @param results The analysis results
     * @param filter A {@link Predicate predicate} to filter columns to adapt.
     */
    public void adaptForSampling(List<ColumnMetadata> columns, List<Analyzers.Result> results, Predicate<ColumnMetadata> filter) {
        genericAdapt(columns, results, filter);
    }

    /**
     * Extract analysis result and inject them in columns metadata
     *
     * @param columns The columns metadata
     * @param results The analysis results
     * @param filter A {@link Predicate predicate} to filter columns to adapt.
     */
    private void genericAdapt(List<ColumnMetadata> columns, List<Analyzers.Result> results, Predicate<ColumnMetadata> filter) {
        final Iterator<Analyzers.Result> resultIterator = results.iterator();
        columns.stream().filter(filter).forEach(c -> {
            if (resultIterator.hasNext()) {
                final Analyzers.Result result = resultIterator.next();
                injectDataTypeAnalysis(c, result);
                adaptCommonAnalysis(c, result);
            }
        });
    }

    /**
     * Extracts remaining analysis result (other than data type) and inject them in the specified column metadata.
     * 
     * @param currentColumn the specified column metadata
     * @param result the specified Analysis result
     */
    private void adaptCommonAnalysis(final ColumnMetadata currentColumn, final Analyzers.Result result) {
        injectSemanticTypes(currentColumn, result);
        injectCardinality(currentColumn, result); // distinct + duplicates
        injectDataFrequency(currentColumn, result);
        injectPatternFrequency(currentColumn, result);
        injectQuantile(currentColumn, result);
        injectNumberSummary(currentColumn, result); // min, max, mean, variance
        injectTextLength(currentColumn, result);
        injectNumberHistogram(currentColumn, result);
        injectDateHistogram(currentColumn, result);
    }

    private void injectDataTypeAnalysis(final ColumnMetadata column, final Analyzers.Result result) {
        if (result.exist(DataTypeOccurences.class) && !column.isTypeForced()) {
            final DataTypeOccurences dataType = result.get(DataTypeOccurences.class);
            final DataTypeEnum suggestedEnumType = dataType.getSuggestedType();
            final Type suggestedColumnType = Type.get(suggestedEnumType.name());

            // the suggested type can be modified by #injectValueQuality
            column.setType(suggestedColumnType.getName());
        }
        injectValueQuality(column, result);
    }

    private void injectValueQuality(final ColumnMetadata column, final Analyzers.Result result) {
        if (result.exist(ValueQualityStatistics.class)) {
            final Statistics statistics = column.getStatistics();
            final Quality quality = column.getQuality();
            final ValueQualityStatistics valueQualityStatistics = result.get(ValueQualityStatistics.class);

            final long allCount = valueQualityStatistics.getCount();
            final long emptyCount = valueQualityStatistics.getEmptyCount();
            final long validCount = valueQualityStatistics.getValidCount();
            final long invalidCount = allCount - emptyCount - validCount;

            // Set in column quality...
            quality.setEmpty((int) emptyCount);
            quality.setValid((int) validCount);
            quality.setInvalid((int) invalidCount);
            quality.setInvalidValues(valueQualityStatistics.getInvalidValues());
            // ... and statistics
            statistics.setCount(allCount);
            statistics.setEmpty((int) emptyCount);
            statistics.setInvalid((int) invalidCount);
            statistics.setValid(validCount);
        }
    }

    private void injectSemanticTypes(final ColumnMetadata column, final Analyzers.Result result) {
        if (result.exist(SemanticType.class) && !column.isDomainForced()) {
            final SemanticType semanticType = result.get(SemanticType.class);
            final Map<CategoryFrequency, Long> foundSemanticTypes = semanticType.getCategoryToCount();
            // TDP-471: Don't pick semantic type if lower than a threshold.
            final Optional<Map.Entry<CategoryFrequency, Long>> entry = foundSemanticTypes.entrySet().stream()
                    .filter(e -> !e.getKey().getCategoryName().isEmpty())
                    .max((o1, o2) -> (o1.getKey().compareTo(o2.getKey())));
            if (entry.isPresent()) {
                // TODO (TDP-734) Take into account limit of the semantic analyzer.
                final float percentage = entry.get().getKey().getFrequency();
                if (percentage > semanticThreshold) {
                    final CategoryFrequency key = entry.get().getKey();
                    final String categoryId = key.getCategoryId();
                    try {
                        final SemanticCategoryEnum category = SemanticCategoryEnum.valueOf(categoryId);
                        column.setDomain(category.getId());
                        column.setDomainLabel(category.getDisplayName());
                        column.setDomainFrequency(percentage);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Could not find {} in known categories.", categoryId, e);
                    }
                } else {
                    // Ensure the domain is cleared if percentage is lower than threshold (earlier analysis - e.g.
                    // on the first 20 lines - may be over threshold, but full scan may decide otherwise.
                    column.setDomain(StringUtils.EMPTY);
                    column.setDomainLabel(StringUtils.EMPTY);
                    column.setDomainFrequency(0);
                }
            } else if (!StringUtils.isEmpty(column.getDomain())) {
                // Column *had* a domain but seems like new analysis removed it.
                column.setDomain(StringUtils.EMPTY);
                column.setDomainLabel(StringUtils.EMPTY);
                column.setDomainFrequency(0);
            }
            // Remembers all suggested semantic categories
            List<SemanticDomain> semanticDomains = semanticType.getCategoryToCount().entrySet().stream()
                    .map(current -> { //
                        // Find category display name
                        final String id = current.getKey().getCategoryId();
                        if (!StringUtils.isEmpty(id)) {
                            // Takes only actual semantic domains (unknown = "").
                            final String categoryDisplayName = TypeUtils.getDomainLabel(id);
                            return new SemanticDomain(id, categoryDisplayName, current.getKey().getFrequency());
                        } else {
                            return null;
                        }
                    }) //
                    .filter(semanticDomain -> semanticDomain != null && semanticDomain.getFrequency() >= 1) //
                    .limit(10) //
                    .collect(Collectors.toList());
            column.setSemanticDomains(semanticDomains);
        }
    }

    private void injectCardinality(final ColumnMetadata column, final Analyzers.Result result) {
        if (result.exist(CardinalityStatistics.class)) {
            final Statistics statistics = column.getStatistics();
            final CardinalityStatistics cardinalityStatistics = result.get(CardinalityStatistics.class);
            statistics.setDistinctCount(cardinalityStatistics.getDistinctCount());
            statistics.setDuplicateCount(cardinalityStatistics.getDuplicateCount());
        }
    }

    private void injectDataFrequency(final ColumnMetadata column, final Analyzers.Result result) {
        if (result.exist(DataTypeFrequencyStatistics.class)) {
            final Statistics statistics = column.getStatistics();
            final DataTypeFrequencyStatistics dataFrequencyStatistics = result.get(DataTypeFrequencyStatistics.class);
            final Map<String, Long> topTerms = dataFrequencyStatistics.getTopK(15);
            if (topTerms != null) {
                statistics.getDataFrequencies().clear();
                topTerms.forEach((s, o) -> statistics.getDataFrequencies().add(new DataFrequency(s, o)));
            }
        }
    }

    private void injectPatternFrequency(final ColumnMetadata column, final Analyzers.Result result) {
        if (result.exist(PatternFrequencyStatistics.class)) {
            final Statistics statistics = column.getStatistics();
            final PatternFrequencyStatistics patternFrequencyStatistics = result.get(PatternFrequencyStatistics.class);
            final Map<String, Long> topTerms = patternFrequencyStatistics.getTopK(15);
            if (topTerms != null) {
                statistics.getPatternFrequencies().clear();
                topTerms.forEach((s, o) -> statistics.getPatternFrequencies().add(new PatternFrequency(s, o)));
            }
        }
    }

    private void injectQuantile(final ColumnMetadata column, final Analyzers.Result result) {
        if (result.exist(QuantileStatistics.class)) {
            try {
                final QuantileStatistics quantileStatistics = result.get(QuantileStatistics.class);
                final Quantiles quantiles = column.getStatistics().getQuantiles();
                quantiles.setLowerQuantile(quantileStatistics.getLowerQuartile());
                quantiles.setMedian(quantileStatistics.getMedian());
                quantiles.setUpperQuantile(quantileStatistics.getUpperQuartile());
            } catch (Exception e) {
                LOGGER.warn("Unable to inject quantile information in column {}.", column.getName());
                LOGGER.debug("Unable to inject quantile information in column {}.", e);
            }
        }
    }

    /**
     * Injects numerical statistics like max, min to statistics of the specified column metadata.
     *
     * For columns of type date, min and max values are retrieved from the date histogram
     * @param column the specified column metadata
     * @param result the analyzer result
     */
    private void injectNumberSummary(final ColumnMetadata column, final Analyzers.Result result) {
        if (result.exist(SummaryStatistics.class)) {
            final Statistics statistics = column.getStatistics();
            final SummaryStatistics summaryStatistics = result.get(SummaryStatistics.class);
            statistics.setMean(summaryStatistics.getMean());
            statistics.setVariance(summaryStatistics.getVariance());
            // if the column is of type Date
            if (DATE.isAssignableFrom(column.getType()) && result.exist(StreamDateHistogramStatistics.class)){
                final DateHistogram histogram = (DateHistogram) result.get(StreamDateHistogramStatistics.class).getHistogram();
                statistics.setMax(histogram.getMaxUTCEpochMilliseconds());
                statistics.setMin(histogram.getMinUTCEpochMilliseconds());
            }
            else {
                statistics.setMax(summaryStatistics.getMax());
                statistics.setMin(summaryStatistics.getMin());
            }
        }
    }

    private void injectNumberHistogram(final ColumnMetadata column, final Analyzers.Result result) {
        if (NUMERIC.isAssignableFrom(column.getType()) && result.exist(StreamNumberHistogramStatistics.class)) {
            final Statistics statistics = column.getStatistics();
            final Map<org.talend.dataquality.statistics.numeric.histogram.Range, Long> histogramStatistics = result
                    .get(StreamNumberHistogramStatistics.class).getHistogram();
            final NumberFormat format = DecimalFormat.getInstance(ENGLISH);

            // Set histogram ranges
            final Histogram histogram = new NumberHistogram();
            histogramStatistics.forEach((rangeValues, occurrence) -> {
                final HistogramRange range = new HistogramRange();
                try {
                    range.getRange().setMax(new Double(format.format(rangeValues.getUpper())));
                    range.getRange().setMin(new Double(format.format(rangeValues.getLower())));
                } catch (NumberFormatException e) {
                    // Fallback to unformatted numbers (unable to parse numbers).
                    range.getRange().setMax(rangeValues.getUpper());
                    range.getRange().setMin(rangeValues.getLower());
                }
                range.setOccurrences(occurrence);
                histogram.getItems().add(range);
            });
            statistics.setHistogram(histogram);
        }
    }

    private void injectDateHistogram(final ColumnMetadata column, final Analyzers.Result result) {
        if (DATE.isAssignableFrom(column.getType()) && result.exist(StreamDateHistogramStatistics.class)) {
            final Histogram histogram = result.get(StreamDateHistogramStatistics.class).getHistogram();
            column.getStatistics().setHistogram(histogram);
        }
    }

    private void injectTextLength(final ColumnMetadata column, final Analyzers.Result result) {
        if (STRING.equals(Type.get(column.getType())) && result.exist(TextLengthStatistics.class)) {
            final TextLengthStatistics textLengthStatistics = result.get(TextLengthStatistics.class);
            final TextLengthSummary textLengthSummary = column.getStatistics().getTextLengthSummary();
            textLengthSummary.setAverageLength(textLengthStatistics.getAvgTextLength());
            textLengthSummary.setMinimalLength(textLengthStatistics.getMinTextLength());
            textLengthSummary.setMaximalLength(textLengthStatistics.getMaxTextLength());
        }
    }

}
