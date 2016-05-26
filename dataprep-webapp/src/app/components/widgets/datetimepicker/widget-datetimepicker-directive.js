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
 * @name talend.widget.directive:TalendDatetimePicker
 * @description This directive create a datetimepicker element.
 * @restrict E
 * @usage
 <talend-datetime-picker
         ng-model="ctrl.value"
         datetimepicker-on-select="ctrl.onSelect"
         datetimepicker-min="min"
         datetimepicker-max="max"
         range-item-type="from|to"
         is-date-time
 ></talend-datetimepicker>
 * @param {string}   value Disable the ENTER key support
 * @param {number}   datetimepickerMin Min where datetimepicker is enabled
 * @param {number}   datetimepickerMax Max where datetimepicker is enabled
 * @param {function} datetimepickerOnSelect Event handler when date is picked
 * @param {string}   rangeItemType If datetimepicker composes a range, need to provide "from" or "to" value
 */
export default function TalendDatetimePicker($filter) {

    return {
        restrict: 'E',
        template: '<input class="datetimepicker" type="text" ng-model="ctrl.value" />',
        scope: {
            value: '=ngModel',
            datetimepickerMin: '=',
            datetimepickerMax: '=',
            datetimepickerOnSelect: '&',
            rangeItemType: '@'
        },
        bindToController: true,
        controller: () => {},
        controllerAs: 'ctrl',
        link: function (scope, iElement, iAttrs, ctrl) {
            Date.parseDate = function (input, format) {
                return moment(input, format).toDate();
            };
            Date.prototype.dateFormat = function (format) {
                return moment(this).format(format);
            };

            let
                format = iAttrs.format ? iAttrs.format : 'DD/MM/YYYY HH:mm:ss',
                formatTime = iAttrs.formatTime ? iAttrs.formatTime : 'HH:mm:ss',
                formatDate = iAttrs.formatDate ? iAttrs.formatDate : 'DD/MM/YYYY',

                input = iElement.find('.datetimepicker'),
                options = {};
            if (ctrl.datetimepickerMin) options.minDate = new Date(ctrl.datetimepickerMin).dateFormat(formatDate);
            if (ctrl.datetimepickerMax) options.maxDate = new Date(ctrl.datetimepickerMax).dateFormat(formatDate);
            if (ctrl.datetimepickerOnSelect) options.onSelectDate = (date) => {
                ctrl.value = date;
                ctrl.datetimepickerOnSelect()(date.getTime(), ctrl.rangeItemType);
            };

            if(!_.has(iAttrs, 'isDateTime')) {
                options.timepicker = false;
            }

            input.datetimepicker(
                _.extend(
                    options,
                    {
                        format: format,
                        formatTime: formatTime,
                        formatDate: formatDate,
                        allowBlank: true
                    }
                )
            );
            input.bind('keydown', (event) => {
                // hide calendar on 'ESC' keydown
                if (event.keyCode === 27) {
                    input.datetimepicker('hide');
                    event.stopPropagation();
                }
            });

            if(ctrl.rangeItemType) {
                // Only date time values need to be filtered
                scope.$watch(
                    () => ctrl.value,
                    (newValue) => {
                        if(newValue !== null || newValue !== '') {
                            ctrl.value = $filter('date')(newValue, 'dd/MM/yyyy');
                        }
                    }
                );
            }
        }
    };
}
