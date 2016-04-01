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
     * @name isMaxReached
     * @propertyOf talend.widget.controller:RangeSliderCtrl
     * @description checks max interval is equal or greater than the maxModel
     * @params {Object} filterToApply the filter to apply
     * @returns {Object}
     */
    vm.isMaxReached = function isMaxReached(filterToTrigger) {
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