/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name talend.widget.controller:RangeSliderCtrl
 * @description The rangeSlider controller
 */
export default function RangeSliderCtrl() {
    var vm = this;

    /**
     * @ngdoc method
     * @name formatDate
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description format date with D3
     */
    vm.formatDate = d3.time.format('%m/%d/%Y');

    /**
     * @ngdoc method
     * @name adaptFilterInterval
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description checks max interval >= the max of data values and  if the min interval < max interval
     * @params {Object} filterToApply the filter to apply
     * @returns {Object}
     */
    vm.adaptFilterInterval = function adaptFilterInterval(filterToTrigger) {
        filterToTrigger = filterToTrigger.min > filterToTrigger.max ?
        {min: filterToTrigger.max, max: filterToTrigger.min} :
            filterToTrigger;

        filterToTrigger.isMaxReached = filterToTrigger.max >= vm.rangeLimits.max;
        return filterToTrigger;
    };

    /**
     * @ngdoc method
     * @name areMinMaxNumbers
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description checks if both of the entered values are numbers
     * @returns {boolean}
     */
    vm.areMinMaxNumbers = function areMinMaxNumbers() {
        var isMinNumber = vm.toNumber(vm.minMaxModel.minModel);
        var isMaxNumber = vm.toNumber(vm.minMaxModel.maxModel);
        return !(isMinNumber === null || isMaxNumber === null);
    };

    /**
     * @ngdoc method
     * @name areMinMaxDates
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description checks if both of the entered values are dates
     * @returns {boolean}
     */
    vm.areMinMaxDates = function areMinMaxDates() {
        const isMinDate = vm.toDate(vm.minMaxModel.minModel);
        const isMaxDate = vm.toDate(vm.minMaxModel.maxModel);
        return !(isMinDate === null || isMaxDate === null);
    };

    /**
     * @ngdoc method
     * @name toNumber
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description converts the entered string to a number returns null if not a valid number
     * @param {string} value The value to transform
     */
    vm.toNumber = function toNumber(value) {
        value = value.trim();
        if (/^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/.test(value)) {
            return Number(value);
        }
        return null;
    };

    /**
     * @ngdoc method
     * @name toDate
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description converts a timeStamp as String to a date object
     * @param {String} dateString to transform
     * @returns {*} Valid date or null
     */
    vm.toDate = function toDate(dateString) {
        return isNaN(new Date(dateString).getTime()) ? null : new Date(dateString);
    };

    /**
     * @ngdoc method
     * @name setDateTimeToMidnight
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description given a timestamp, it sets it to midnight, the same day
     * @param {Number} timeStamp to set
     * @returns {number} date timestamp
     */
    vm.setDateTimeToMidnight = function setDateTimeToMidnight(timeStamp) {
        return new Date(vm.formatDate(new Date(timeStamp))).getTime();
    };

    /**
     * @ngdoc method
     * @name checkCommaExistence
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description check if the entered values contain a comma
     */
    vm.checkCommaExistence = function checkCommaExistence(minMaxStr) {
        return minMaxStr.indexOf(',') > -1;
    };

    /**
     * @ngdoc method
     * @name decimalPlaces
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description Return the decimal index
     */
    vm.decimalPlaces = function decimalPlaces(num) {
        var match = ('' + num).match(/(?:\.(\d+))?(?:[eE]([+-]?\d+))?$/);
        return Math.max(
            0,
            // Number of digits right of decimal point.
            (match[1] ? match[1].length : 0) -
                // Adjust for scientific notation.
            (match[2] ? +match[2] : 0));
    };

    /**
     * @ngdoc method
     * @name adaptRangeValues
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description Adapt the entered values to respect the rules :
     * <ul>
     *     <li>The entered min value < the entered max value</li>
     *     <li>The entered min value is within the range defined by 'minimum' and 'maximum'</li>
     * </ul>
     * @param enteredMin The min input value
     * @param enteredMax The max input value
     * @param minimum The minimum value in the range
     * @param maximum The maximum value in the range
     */
    vm.adaptRangeValues = function adaptRangeValues(enteredMin, enteredMax, minimum, maximum) {
        //switch entered values if necessary
        if (enteredMin > enteredMax) {
            var _aux = enteredMin;
            enteredMin = enteredMax;
            enteredMax = _aux;
        }

        //maximum limits
        if (enteredMax > maximum) {
            enteredMax = maximum;
        }
        else if (enteredMax < minimum) {
            enteredMax = minimum;
        }

        //minimum limits
        if (enteredMin > maximum) {
            enteredMin = maximum;
        }
        else if (enteredMin < minimum) {
            enteredMin = minimum;
        }

        //final extent without delta
        return {
            min: enteredMin,
            max: enteredMax
        };
    };
}