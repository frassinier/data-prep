/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:DateService
 * @description DateService service. This service help to manipulate dates
 */
export default function DateService() {

    const DEFAULT_MOMENT_DATE_FORMAT = 'DD/MM/YYYY';

    return {
        isInDateLimits: isInDateLimits,
        getFormattedDateFromTime: getFormattedDateFromTime,
        getTimeFromFormattedDate: getTimeFromFormattedDate
    };

    /**
     * @ngdoc method
     * @name isInDateLimits
     * @methodOf data-prep.services.utils.service:DateService
     * @description Predicate that test if a date is in the range
     * @param {number} minTimestamp The range min timestamp
     * @param {number} maxTimestamp The range max timestamp
     * @param {Array} patterns The date patterns to use for date parsing
     */
    function isInDateLimits(minTimestamp, maxTimestamp, patterns) {
        return function (value) {
            var parsedMoment = _.chain(patterns)
                .map(function (pattern) {
                    return moment(value, pattern, true);
                })
                .find(function (momentDate) {
                    return momentDate.isValid();
                })
                .value();

            if (!parsedMoment) {
                return false;
            }

            var time = parsedMoment.toDate().getTime();
            if (minTimestamp === null) return (time <= maxTimestamp);
            if (maxTimestamp === null) return (time >= minTimestamp);
            return time === minTimestamp || (time > minTimestamp && time < maxTimestamp);
        };
    }

    /**
     * @ngdoc method
     * @name getFormattedDateFromTime
     * @methodOf data-prep.services.utils.service:DateService
     * @description Get formatted date from time
     * @param {number} time Date time
     * @param {string} pattern date pattern to use
     * @returns {string|null} Formatted date using pattern
     */
    function getFormattedDateFromTime(time, format = DEFAULT_MOMENT_DATE_FORMAT) {
        if (!time) {
            return null;
        }
        const date = new Date(time);
        return moment(date).format(format);
    }

    /**
     * @ngdoc method
     * @name getTimeFromFormattedDate
     * @methodOf data-prep.services.utils.service:DateService
     * @description Get time from formatted Date
     * @param {string} formattedDate Formatted date
     * @param {string} format Date pattern to parse
     * @returns {number|null} Parsed date time
     */
    function getTimeFromFormattedDate(formattedDate, format = DEFAULT_MOMENT_DATE_FORMAT) {
        if (!formattedDate) {
            return null;
        }
        const date = moment(formattedDate, format).toDate();
        return date.getTime();
    }
}