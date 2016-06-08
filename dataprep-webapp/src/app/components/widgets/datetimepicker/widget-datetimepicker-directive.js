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
         range-item-type="from|to"
         is-date-time
 ></talend-datetimepicker>
 * @param {string}   value Disable the ENTER key support
 * @param {function} datetimepickerOnSelect Event handler when date is picked
 * @param {function} onBlur Event handler when input is on blur
 * @param {string}   rangeItemType If datetimepicker composes a range, need to provide "from" or "to" value
 */
export default function TalendDatetimePicker($filter) {

    return {
        restrict: 'E',
        template: '<input class="datetimepicker" type="text" ng-blur="ctrl.onBlur()" ng-model="ctrl.value" />',
        scope: {
            value: '=ngModel',
            datetimepickerOnSelect: '&',
            onBlur: '&ngBlur',
            rangeItemType: '@'
        },
        bindToController: true,
        controller: () => {
        },
        controllerAs: 'ctrl',
        link: function (scope, iElement, iAttrs, ctrl) {
            Date.parseDate = function (input, format) {
                return moment(input, format).toDate();
            };
            Date.prototype.dateFormat = function (format) {
                return moment(this).format(format);
            };

            let format = iAttrs.format ? iAttrs.format : 'MM/DD/YYYY';
            let formatTime = iAttrs.formatTime ? iAttrs.formatTime : 'HH:mm:ss';
            let formatDate = iAttrs.formatDate ? iAttrs.formatDate : 'MM/DD/YYYY';
            let options = {};

            if (ctrl.datetimepickerOnSelect) {
                options.onSelectDate = (date) => {
                    ctrl.value = date;
                    ctrl.datetimepickerOnSelect({
                        timeStamp: date.getTime(),
                        rangeType: ctrl.rangeItemType
                    });
                };
            }

            if (!_.has(iAttrs, 'isDateTime')) {
                options.timepicker = false;
            }

            let input = iElement.find('.datetimepicker');
            input.datetimepicker({
                ...options,
                ...{
                    lang: 'en',
                    format: format,
                    formatDate: formatDate,
                    formatTime: formatTime,
                    allowBlank: false
                }
            });

            input.bind('keydown', (event) => {
                // hide calendar on 'ESC' keydown
                if (event.keyCode === 27) {
                    input.datetimepicker('hide');
                    event.stopPropagation();
                }
            });

            if (ctrl.rangeItemType) {
                // Only date time values need to be filtered
                scope.$watch(
                    () => ctrl.value,
                    (newValue) => {
                        ctrl.value = $filter('date')(newValue, 'MM/dd/yyyy')
                    }
                );
            }
        }
    };
}
