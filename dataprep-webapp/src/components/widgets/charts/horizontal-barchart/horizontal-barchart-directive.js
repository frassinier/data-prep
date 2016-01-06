(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name talend.widget.directive:horizontalBarchart
     * @description This directive renders the horizontal bar chart.
     * @restrict E
     * @usage
     * <horizontal-barchart id="hBarChart"
     *    width="320"
     *    height="400"
     *    on-click="columnProfileCtrl.barchartClickFn(item)"
     *
     *    key-field="country"
     *    key-label="Country of the world"
     *
     *    primary-data="primaryData"
     *    primary-value-field="occurrences"
     *    primary-bar-class="blueBar"
     *
     *    secondary-data="secondaryData"
     *    secondary-value-field="filteredOccurrences"
     *    secondary-bar-class="greenBar">
     * </horizontal-barchart>
     * @param {number}      width The chart width
     * @param {number}      height The chart height
     * @param {function}    onClick The callback on chart bar click. The item in argument is the element in primaryData that is selected.
     * @param {string}      keyField The key property name in primaryData elements
     * @param {string}      keyLabel The label property name in primaryData elements used in tooltip
     * @param {array}       primaryData The primary value array to render
     * @param {string}      primaryValueField The primary value property name in primaryData
     * @param {string}      primaryBarClass The primary chart bar class name. Default: none
     * @param {array}       secondaryData The secondary value array to render
     * @param {string}      secondaryValueField The secondary value property name in secondaryData
     * @param {string}      secondaryBarClass The secondary chart bar class name. Default: 'blueBar'
     * */

    function HorizontalBarchart() {
        return {
            restrict: 'E',
            scope: {
                onClick: '&',
                keyField: '@',
                keyLabel: '@',
                primaryData: '=',
                primaryValueField: '@',
                primaryBarClass: '@',
                secondaryData: '=',
                secondaryValueField: '@',
                secondaryBarClass: '@'
            },
            link: function (scope, element, attrs) {
                var containerId = '#' + attrs.id;
                var renderPrimaryTimeout, renderSecondaryTimeout;

                // Define chart sizes and margin
                var margin = {top: 15, right: 20, bottom: 10, left: 10};
                var containerWidth = +attrs.width;

                //------------------------------------------------------------------------------------------------------
                //----------------------------------------------- Tooltip ----------------------------------------------
                //------------------------------------------------------------------------------------------------------
                var tooltip = d3.tip()
                    .attr('class', 'horizontal-barchart-cls d3-tip')
                    .offset([-10, 0])
                    .html(function (d) {
                        var key = getKey(d);
                        var primaryValue = getPrimaryValue(d);
                        var secondaryValue = getSecondaryValueFromKey(key);

                        return '<strong>' + scope.keyLabel + ':</strong> <span style="color:yellow">' + (secondaryValue ? secondaryValue + ' / ' : '') + primaryValue + '</span>' +
                            '<br/><br/>' +
                            '<strong>Record:</strong> <span style="color:yellow">' + key + '</span>';
                    });

                //------------------------------------------------------------------------------------------------------
                //---------------------------------------- Data manipulation -------------------------------------------
                //------------------------------------------------------------------------------------------------------
                function getKey(data) {
                    return data[scope.keyField];
                }

                function getPrimaryValue(data) {
                    return data[scope.primaryValueField];
                }

                function getSecondaryValue(data) {
                    return data[scope.secondaryValueField];
                }

                function getSecondaryValueFromKey(key) {
                    var secondaryData = scope.secondaryData;
                    if (!secondaryData) {
                        return 0;
                    }

                    var secondaryDataItem = _.find(secondaryData, function (dataItem) {
                        return getKey(dataItem) === key;
                    });
                    return secondaryDataItem ? getSecondaryValue(secondaryDataItem) : 0;
                }

                //------------------------------------------------------------------------------------------------------
                //--------------------------------------------- Bar class ----------------------------------------------
                //------------------------------------------------------------------------------------------------------
                function getPrimaryClassName() {
                    return scope.primaryBarClass ? scope.primaryBarClass : 'transparentBar';
                }

                function getSecondaryClassName() {
                    return scope.secondaryBarClass ? scope.secondaryBarClass : 'blueBar';
                }

                //------------------------------------------------------------------------------------------------------
                //------------------------------------------- Chart utils ----------------------------------------------
                //------------------------------------------------------------------------------------------------------
                var xScale, yScale;
                var xAxis, yAxis;
                var svg;

                function initScales(width, height) {
                    xScale = d3.scale.linear().range([0, width]);
                    yScale = d3.scale.ordinal().rangeBands([0, height], 0.18);
                }

                function configureScales(statData) {
                    xScale.domain([0, d3.max(statData, getPrimaryValue)]);
                    yScale.domain(statData.map(getKey));
                }

                function initAxes(width, height) {
                    var minSizeBetweenGrid = 40;
                    var ticksThreshold = Math.ceil(width / minSizeBetweenGrid);
                    var ticksNbre = xScale.domain()[1] > ticksThreshold ? ticksThreshold : xScale.domain()[1];

                    xAxis = d3.svg.axis()
                        .scale(xScale)
                        .tickFormat(d3.format(',d'))
                        .orient('top')
                        .tickSize(-height)
                        .ticks(ticksNbre);
                    yAxis = d3.svg.axis()
                        .scale(yScale)
                        .orient('left')
                        .tickSize(0);
                }

                function createContainer(width, height) {
                    svg = d3.select(containerId)
                        .append('svg')
                        .attr('class', 'horizontal-barchart-cls')
                        .attr('width', width)
                        .attr('height', height)
                        .append('g')
                        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

                    svg.call(tooltip);
                }

                function drawGrid() {
                    svg.append('g')
                        .attr('class', 'grid')
                        .call(xAxis)
                        .selectAll('.tick text')
                        .style('text-anchor', 'middle');
                }

                function createBarsContainers() {
                    svg.append('g')
                        .attr('class', 'primaryBar');

                    svg.append('g')
                        .attr('class', 'secondaryBar');
                }

                function drawBars(containerClassName, statData, getValue, barClassName) {
                    var bars = svg.select('.' + containerClassName)
                        .selectAll('.' + barClassName)
                        .data(statData, getKey);

                    //enter
                    bars.enter()
                        .append('rect')
                        .attr('class', barClassName)
                        .attr('transform', function (d) {
                            return 'translate(0,' + yScale(getKey(d)) + ')';
                        })
                        .attr('height', yScale.rangeBand())
                        .attr('width', xScale(0))
                        .transition().delay(function (d, i) {
                            return i * 30;
                        })
                        .attr('width', function (d) {
                            var realWidth = xScale(getValue(d));
                            return realWidth < 1 ? 3 : realWidth;
                        });

                    //update
                    bars.transition()
                        .ease('exp')
                        .delay(function (d, i) {
                            return i * 30;
                        })
                        .attr('width', function (d) {
                            var realWidth = xScale(getValue(d));
                            return realWidth < 1 ? 3 : realWidth;
                        });
                }

                function drawKeysLabels(statData, width) {
                    svg.append('g')
                        .attr('class', 'labels')
                        .selectAll('label')
                        .data(statData)
                        .enter()

                        //label container
                        .append('foreignObject')
                        .attr('width', width)
                        .attr('height', yScale.rangeBand())
                        .attr('transform', function (d) {
                            return 'translate(0,' + yScale(getKey(d)) + ')';
                        })

                        //label
                        .append('xhtml:div')
                        .attr('class', 'label ' + getSecondaryClassName())
                        .html(function (d) {
                            return getKey(d) || '(EMPTY)';
                        });
                }

                function drawHoverBars(statData, width) {
                    svg.selectAll('g.bg-rect')
                        .data(statData)
                        .enter()
                        .append('g')
                        .attr('class', 'hover')
                        .attr('transform', function (d) {
                            return 'translate(0, ' + (yScale(getKey(d)) - 2) + ')';
                        })
                        .append('rect')
                        .attr('width', width)
                        .attr('height', yScale.rangeBand() + 4)
                        .attr('class', 'bg-rect')
                        .style('opacity', 0)
                        .on('mouseenter', function (d) {
                            d3.select(this).style('opacity', 0.4);
                            tooltip.show(d);
                        })
                        .on('mouseleave', function (d) {
                            d3.select(this).style('opacity', 0);
                            tooltip.hide(d);
                        })
                        .on('click', function (d) {
                            scope.onClick({item: d});
                        });
                }

                //------------------------------------------------------------------------------------------------------
                //------------------------------------------- Chart render ---------------------------------------------
                //------------------------------------------------------------------------------------------------------
                function renderWholeHBarchart(firstVisuData, secondVisuData) {
                    // Chart sizes dynamically computed (it depends on the bars number)
                    var containerHeight = Math.ceil(((+attrs.height) / 15) * (firstVisuData.length + 1));
                    var width = containerWidth - margin.left - margin.right;
                    var height = containerHeight - margin.top - margin.bottom;

                    initScales(width, height);
                    configureScales(firstVisuData);
                    initAxes(width, height);
                    createContainer(containerWidth, containerHeight);
                    drawGrid();
                    createBarsContainers();

                    drawBars('primaryBar', firstVisuData, getPrimaryValue, getPrimaryClassName());
                    renderSecondaryBars(secondVisuData);

                    drawKeysLabels(firstVisuData, width);
                    drawHoverBars(firstVisuData, width);
                }

                function renderSecondaryBars(secondVisuData) {
                    if (secondVisuData) {
                        drawBars('secondaryBar', secondVisuData, getSecondaryValue, getSecondaryClassName());
                    }
                }

                //------------------------------------------------------------------------------------------------------
                //---------------------------------------------- Watchers ----------------------------------------------
                //------------------------------------------------------------------------------------------------------
                var oldVisuData;
                scope.$watchGroup(['primaryData', 'secondaryData'],
                    function (newValues) {
                        var firstVisuData = newValues[0];
                        var secondVisuData = newValues[1];
                        var firstDataHasChanged = firstVisuData !== oldVisuData;

                        if (firstDataHasChanged) {
                            oldVisuData = firstVisuData;
                            element.empty();
                            //because the tooltip is not a child of the horizontal barchart element
                            d3.selectAll('.horizontal-barchart-cls.d3-tip').remove();
                            if (firstVisuData) {
                                clearTimeout(renderPrimaryTimeout);
                                renderPrimaryTimeout = setTimeout(renderWholeHBarchart.bind(this, firstVisuData, secondVisuData), 100);
                            }
                        }
                        else {
                            clearTimeout(renderSecondaryTimeout);
                            renderSecondaryTimeout = setTimeout(renderSecondaryBars.bind(this, secondVisuData), 100);
                        }
                    }
                );

                scope.$on('$destroy', function () {
                    d3.selectAll('.horizontal-barchart-cls.d3-tip').remove();
                });
            }
        };
    }

    angular.module('talend.widget')
        .directive('horizontalBarchart', HorizontalBarchart);
})();