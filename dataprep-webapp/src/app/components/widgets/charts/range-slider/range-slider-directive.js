/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc directive
 * @name talend.widget.directive:rangeSlider
 * @description This directive renders the rangeSlider.
 * @restrict E
 * @usage
 *     <range-slider
 *             id="domId"
 *             width="300"
 *             height="100"
 *             range-limits="rangeLimits"
 *             on-brush-end="onBrushEndFn(interval)"
 *         ></range-slider>
 * @param {string} id The element id
 * @param {number} width The width of the slider
 * @param {number} height The height of the slider
 * @param {object} rangeLimits {min: number, max: number} The limits of the slider
 * @param {function} onBrushEnd The callback on slider move
 * */

export default function RangeSlider($timeout) {
    'ngInject';

    return {
        restrict: 'E',
        scope: {
            rangeLimits: '=',
            onBrushEnd: '&'
        },
        controller: 'RangeSliderCtrl',
        controllerAs: 'rangeSliderCtrl',
        bindToController: true,
        templateUrl: 'app/components/widgets/charts/range-slider/range-slider.html',

        link: function (scope, element, attrs, ctrl) {
            var renderTimeout;
            //var filterToApply;

            ctrl.showRangeInputs = true;

            //the left and right margins MUST be the same as the vertical Barchart ones
            var margin = {top: 5, right: 20, bottom: 5, left: 15};
            var width = attrs.width - margin.left - margin.right;
            var height = attrs.height - margin.top - margin.bottom;

            var lastValues = {
                input: {},
                brush: {}
            };

            var brushg;

            /**
             * @ngdoc method
             * @name renderRangerSlider
             * @methodOf data-prep.rangeSlider.directive:RangeSlider
             * @description Render the slider and attach the actions listeners
             **/
            function renderRangerSlider() {
                var rangeLimits = ctrl.rangeLimits;
                ctrl.isDateType = rangeLimits.type === 'date';

                //convert all timestamps to midnight
                if (ctrl.isDateType) {
                    rangeLimits.min = ctrl.setDateTimeToMidnight(rangeLimits.min);
                    rangeLimits.max = ctrl.setDateTimeToMidnight(rangeLimits.max);
                    if (typeof rangeLimits.minBrush !== 'undefined') rangeLimits.minBrush = ctrl.setDateTimeToMidnight(rangeLimits.minBrush);
                    if (typeof rangeLimits.maxBrush !== 'undefined') rangeLimits.maxBrush = ctrl.setDateTimeToMidnight(rangeLimits.maxBrush);
                    if (typeof rangeLimits.minFilterVal !== 'undefined') rangeLimits.minFilterVal = ctrl.setDateTimeToMidnight(rangeLimits.minFilterVal);
                    if (typeof rangeLimits.maxFilterVal !== 'undefined') rangeLimits.maxFilterVal = ctrl.setDateTimeToMidnight(rangeLimits.maxFilterVal);
                }

                var minBrush = typeof rangeLimits.minBrush !== 'undefined' ? rangeLimits.minBrush : rangeLimits.min;
                var maxBrush = typeof rangeLimits.maxBrush !== 'undefined' ? rangeLimits.maxBrush : rangeLimits.max;
                var minFilter = typeof rangeLimits.minFilterVal !== 'undefined' ? rangeLimits.minFilterVal : rangeLimits.min;
                var maxFilter = typeof rangeLimits.maxFilterVal !== 'undefined' ? rangeLimits.maxFilterVal : rangeLimits.max;

                var nbDecimals = d3.max([ctrl.decimalPlaces(minBrush), ctrl.decimalPlaces(maxBrush)]);
                lastValues.input = {
                    min: minFilter,
                    max: maxFilter
                };

                ctrl.minMaxModel = {
                    minModel: ctrl.isDateType ? lastValues.input.min : '' + lastValues.input.min,
                    maxModel: ctrl.isDateType ? lastValues.input.max : '' + lastValues.input.max
                };

                lastValues.brush = {
                    min: minBrush,
                    max: maxBrush
                };

                var centerValue = (minBrush + maxBrush) / 2;

                //--------------------------------------------------------------------------------------------------
                //----------------------------------------------CONTAINER-------------------------------------------
                //--------------------------------------------------------------------------------------------------
                var scale = d3.scale.linear()
                    .domain([rangeLimits.min, rangeLimits.max])
                    .range([0, width]);

                var svg = d3.select('.range-slider-id').append('svg')
                    .attr('width', width + margin.left + margin.right)
                    .attr('height', height + margin.top + margin.bottom)
                    .attr('class', 'range-slider-cls')
                    .append('g')
                    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

                //when brush has a single value (min = max), the brush is empty
                //an empty brush is not rendered --> we set a delta
                function handleUniqueBrushValue() {
                    if (ctrl.brush.empty()) {
                        var min = ctrl.brush.extent()[0];
                        var max = ctrl.brush.extent()[1];
                        var exp = '1e-' + (nbDecimals + 2);
                        svg.select('.brush').call(ctrl.brush.clear().extent([min, max + Number(exp)]));
                    }
                }

                //--------------------------------------------------------------------------------------------------
                //--------------------PROCESS THE FILTER TO TRIGGER-------------------------------------------------
                //--------------------------------------------------------------------------------------------------

                function triggerFilter(filterToTrigger) {
                    ctrl.onBrushEnd({interval: ctrl.adaptFilterInterval(filterToTrigger)});
                }

                function prepareBrushFilter(initialBrushValues) {
                    var brushValues = ctrl.brush.extent();
                    lastValues.brush.min = ctrl.isDateType ? ctrl.setDateTimeToMidnight(brushValues[0]) : +brushValues[0].toFixed(nbDecimals);
                    lastValues.brush.max = ctrl.isDateType ? ctrl.setDateTimeToMidnight(brushValues[1]) : +brushValues[1].toFixed(nbDecimals);

                    //The case where the user clicks on an existing brush
                    // but DOES NOT drag n drop it
                    if (lastValues.brush.min === initialBrushValues[0] &&
                        lastValues.brush.max === initialBrushValues[1]) {
                        return;
                    }

                    //left brush moved
                    var filterToApply = {
                        min: initialBrushValues[0],
                        max: initialBrushValues[1]
                    };
                    if (initialBrushValues[0] !== lastValues.brush.min) {
                        filterToApply.min = lastValues.brush.min;
                        lastValues.input.min = lastValues.brush.min;
                    }
                    //right brush moved
                    if (initialBrushValues[1] !== lastValues.brush.max) {
                        filterToApply.max = lastValues.brush.max;
                        lastValues.input.max = lastValues.brush.max;
                    }

                    handleUniqueBrushValue();
                    triggerFilter(filterToApply);
                }

                ctrl.prepareInputFilter = function prepareInputFilter(dateAsTime, type) {
                    if ((!ctrl.areMinMaxNumbers() && !ctrl.isDateType) || (!ctrl.areMinMaxDates() && ctrl.isDateType)) {
                        initInputValues();
                    }
                    else {
                        if (ctrl.isDateType) {
                            if (type === 'from') {
                                ctrl.minMaxModel.minModel = ctrl.setDateTimeToMidnight(dateAsTime);
                                ctrl.minMaxModel.maxModel = ctrl.setDateTimeToMidnight(ctrl.minMaxModel.maxModel);
                            }
                            if (type === 'to') {
                                ctrl.minMaxModel.maxModel = ctrl.setDateTimeToMidnight(dateAsTime);
                                ctrl.minMaxModel.minModel = ctrl.setDateTimeToMidnight(ctrl.minMaxModel.minModel);
                            }
                            if (angular.isUndefined(type)) {
                                ctrl.minMaxModel.minModel = angular.isUndefined(dateAsTime) ? ctrl.setDateTimeToMidnight(ctrl.minMaxModel.minModel) : dateAsTime;//because new Date(0) ==> 01/01/1970
                                ctrl.minMaxModel.maxModel = angular.isUndefined(dateAsTime) ? ctrl.setDateTimeToMidnight(ctrl.minMaxModel.maxModel) : dateAsTime;
                            }
                        }

                        const enteredMin = +ctrl.minMaxModel.minModel;
                        const enteredMax = +ctrl.minMaxModel.maxModel;

                        //2 Cases:
                        //- ONBLUR: the user selects the input then he selects sth else
                        //- ONENTER without changing anything
                        if (lastValues.input.min === enteredMin &&
                            lastValues.input.max === enteredMax) {
                            return;
                        }
                        lastValues.input.min = enteredMin;
                        lastValues.input.max = enteredMax;

                        //resize the brush to the right extent
                        nbDecimals = d3.max([ctrl.decimalPlaces(enteredMin), ctrl.decimalPlaces(enteredMax)]);
                        lastValues.brush = ctrl.adaptRangeValues(enteredMin, enteredMax, rangeLimits.min, rangeLimits.max);
                        brushg.transition().call(ctrl.brush.extent([lastValues.brush.min, lastValues.brush.max]));
                        //resize the brush with the delta
                        handleUniqueBrushValue();

                        var filterToApply = {
                            min: lastValues.input.min,
                            max: lastValues.input.max
                        };

                        triggerFilter(filterToApply);
                    }
                };

                //--------------------------------------------------------------------------------------------------
                //--------------------BRUSH WIDGET------------------------------------------------------------------
                //--------------------------------------------------------------------------------------------------
                function initBrush() {
                    //create axis + brush
                    let axisTicksNumber;
                    if (ctrl.isDateType) {
                        axisTicksNumber = 3;
                    }
                    else {
                        axisTicksNumber = rangeLimits.max >= 1e10 || rangeLimits.min <= 1e-10 ? 1 : 3;
                    }
                    svg.append('g')
                        .attr('class', 'x axis')
                        .attr('transform', 'translate(0,' + (margin.top + 20) + ')')
                        .call(d3.svg.axis()
                            .scale(scale)
                            .orient('top')
                            .ticks(axisTicksNumber)
                            .tickFormat(function (d) {
                                return ctrl.isDateType ? d3.time.format('%b %Y')(new Date(d)) : d3.format(',')(d);
                            }))
                        .selectAll('text').attr('y', -13);

                    svg.append('g').append('text')
                        .attr('class', 'the-minimum-label')
                        .attr('x', -10)
                        .attr('y', height)
                        .attr('text-anchor', 'start')
                        .attr('fill', 'grey')
                        .text(function () {
                            return ctrl.isDateType ? ctrl.formatDate(new Date(rangeLimits.min)) : d3.format(',')(rangeLimits.min);
                        });

                    svg.append('g').append('text')
                        .attr('class', 'the-maximum-label')
                        .attr('x', width + 10)
                        .attr('y', height)
                        .attr('text-anchor', 'end')
                        .attr('fill', 'grey')
                        .text(function () {
                            return ctrl.isDateType ? ctrl.formatDate(new Date(rangeLimits.max)) : d3.format(',')(rangeLimits.max);
                        });

                    ctrl.brush = d3.svg.brush()
                        .x(scale)
                        .extent([centerValue, centerValue]);

                    brushg = svg.append('g')
                        .attr('transform', 'translate(0,' + (margin.top + 10) + ')')
                        .attr('class', 'brush')
                        .call(ctrl.brush);

                    brushg.call(ctrl.brush.event)
                        .transition().duration(400)
                        .call(ctrl.brush.extent([minBrush, maxBrush]));

                    brushg.selectAll('.resize')
                        .append('rect')
                        .attr('transform', function (d, i) {
                            return i ? 'translate(-10, 0)' : 'translate(0,0)';
                        })
                        .attr('width', 10);

                    brushg.selectAll('rect')
                        .attr('height', 20);

                    brushg.select('.extent')
                        .attr('height', 14)
                        .attr('transform', 'translate(0,3)');

                    //The case where the applied filter have the same value and the user click back on the column or
                    // on the CHART tab
                    handleUniqueBrushValue();

                    //The case where the user interacts with the brush
                    var initialBrushValues;
                    ctrl.brush

                        .on('brushstart', function brushstart() {
                            //Will memorize the ancient extent
                            const startExtent = ctrl.brush.extent();
                            if(ctrl.isDateType){
                                initialBrushValues = [
                                    ctrl.setDateTimeToMidnight(startExtent[0]),
                                    ctrl.setDateTimeToMidnight(startExtent[1])
                                ];
                            }
                            else {
                                initialBrushValues = startExtent;
                            }
                        })

                        //It will update the min and max inputs, and create a brush on a single value when the user clicks on the slider without making a drag( the created brush will be empty )
                        .on('brush', function brushmove() {
                            let brushValues;
                            if(ctrl.isDateType){
                                brushValues = [
                                    ctrl.setDateTimeToMidnight(ctrl.brush.extent()[0]),
                                    ctrl.setDateTimeToMidnight(ctrl.brush.extent()[1])
                                ];
                            }
                            else {
                                brushValues = ctrl.brush.extent();
                            }

                            if (initialBrushValues[0] !== brushValues[0]) {
                                $timeout(function () {
                                    ctrl.minMaxModel.minModel = ctrl.isDateType ? ctrl.setDateTimeToMidnight(brushValues[0]) : brushValues[0].toFixed(nbDecimals);
                                });
                            }
                            if (initialBrushValues[1] !== brushValues[1]) {
                                $timeout(function () {
                                    ctrl.minMaxModel.maxModel = ctrl.isDateType ? ctrl.setDateTimeToMidnight(brushValues[1]) : brushValues[1].toFixed(nbDecimals);
                                });
                            }
                        })

                        //It will propagate the new filter limits to the rest of the app, it's triggered when the user finishes a brush
                        .on('brushend', function brushend() {
                            prepareBrushFilter(initialBrushValues);
                        });
                }

                //--------------------------------------------------------------------------------------------------
                //--------------------INPUT WIDGET------------------------------------------------------------------
                //--------------------------------------------------------------------------------------------------
                //shows the message Error with details on comma existence
                function showMsgErr() {
                    ctrl.invalidNumber = true;
                    var minMaxStr = ctrl.minMaxModel.minModel + ctrl.minMaxModel.maxModel;
                    ctrl.invalidNumberWithComma = ctrl.checkCommaExistence(minMaxStr);
                }

                //hides the message Error
                function hideMsgErr() {
                    ctrl.invalidNumber = false;
                    ctrl.invalidNumberWithComma = false;
                }

                //Init min/max inputs values with existing filter values if defined, min/max otherwise
                function initInputValues() {
                    hideMsgErr();
                    $timeout(function () {
                        ctrl.minMaxModel.minModel = ctrl.isDateType ? lastValues.input.min : '' + lastValues.input.min;
                        ctrl.minMaxModel.maxModel = ctrl.isDateType ? lastValues.input.max : '' + lastValues.input.max;
                    });
                }

                function initRangeInputsListeners() {
                    var minCorrectness, maxCorrectness;
                    //create a key listener closure
                    ctrl.handleKey = function handleKey(rangeType) {
                        return function (e) {
                            switch (e.keyCode) {
                                case 9:
                                case 13:
                                    if (minCorrectness !== null && maxCorrectness !== null) {
                                        ctrl.prepareInputFilter();
                                    }
                                    else {
                                        initInputValues();
                                    }
                                    break;
                                case 27:
                                    initInputValues();
                                    break;
                                default:
                                    if (ctrl.isDateType) {
                                        minCorrectness = rangeType === 'min' ? ctrl.toDate(ctrl.minMaxModel.minModel) : minCorrectness;
                                        maxCorrectness = rangeType === 'max' ? ctrl.toDate(ctrl.minMaxModel.maxModel) : maxCorrectness;
                                    }
                                    else {
                                        minCorrectness = rangeType === 'min' ? ctrl.toNumber(ctrl.minMaxModel.minModel) : minCorrectness;
                                        maxCorrectness = rangeType === 'max' ? ctrl.toNumber(ctrl.minMaxModel.maxModel) : maxCorrectness;
                                    }

                                    if (minCorrectness === null || maxCorrectness === null) {
                                        showMsgErr();
                                    }
                                    else {
                                        hideMsgErr();
                                    }
                            }
                        };
                    };

                    //stop event propagation
                    ctrl.stopPropagation = function stopPropagation(e) {
                        e.stopPropagation();
                    };
                }

                //--------------------------------------------------------------------------------------------------
                //--------------------WIDGET INITIALIZATION 1 RENDER------------------------------------------------
                //--------------------------------------------------------------------------------------------------
                initInputValues();
                initRangeInputsListeners();
                initBrush();
            }


            function shouldRerender(newRangeLimits, oldRangeLimits) {
                if (newRangeLimits.type === 'date') {
                    newRangeLimits.min = ctrl.setDateTimeToMidnight(newRangeLimits.min);
                    newRangeLimits.max = ctrl.setDateTimeToMidnight(newRangeLimits.max);
                    if (newRangeLimits.minBrush) {
                        newRangeLimits.minBrush = ctrl.setDateTimeToMidnight(newRangeLimits.minBrush);
                    }
                    if (newRangeLimits.maxBrush) {
                        newRangeLimits.maxBrush = ctrl.setDateTimeToMidnight(newRangeLimits.maxBrush);
                    }
                }
                return !ctrl.brush ||
                    oldRangeLimits.min !== newRangeLimits.min ||
                    oldRangeLimits.max !== newRangeLimits.max ||
                    lastValues.brush.min !== newRangeLimits.minBrush ||
                    lastValues.brush.max !== newRangeLimits.maxBrush;
            }

            scope.$watch(function () {
                    return ctrl.rangeLimits;
                },
                function (newRangeLimits, oldRangeLimits) {
                    if (!newRangeLimits) {
                        element.find('svg').remove();
                        ctrl.showRangeInputs = false;
                        ctrl.brush = null;
                    }

                    else if (shouldRerender(newRangeLimits, oldRangeLimits)) {
                        element.find('svg').remove();
                        ctrl.showRangeInputs = false;
                        if (newRangeLimits.min !== newRangeLimits.max) {
                            $timeout.cancel(renderTimeout);
                            ctrl.showRangeInputs = true;
                            renderTimeout = $timeout(renderRangerSlider, 100, false);
                        }
                    }
                }
            );

            scope.$on('$destroy', function () {
                $timeout.cancel(renderTimeout);
            });
        }
    };
}