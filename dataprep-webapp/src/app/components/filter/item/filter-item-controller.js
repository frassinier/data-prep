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
 * @name data-prep.filter-item.controller:FilterItemCtrl
 * @description FilterItem controller.
 */
export default class FilterItemCtrl {

    constructor(FilterService, $translate) {
        'ngInject';

        this.$translate = $translate;
        this.filterService = FilterService;

        this.remove = this.remove;
    }

    get EMPTY_RECORDS_LABEL() {
        return this.filterService.EMPTY_RECORDS_LABEL;
    }

    $onInit() {
        const filter = this.filter;
        if (filter) {
            switch (filter.type) {
                case 'contains':
                    this.sign = ' ≅ ';
                    break;
                case 'exact':
                    this.sign = ' = ';
                    break;
                case 'inside_range':
                    this.sign = ' in ';
                    break;
                default:
                    this.sign = this.$translate.instant('COLON');
            }
        }
    }

    /**
     * @ngdoc method
     * @name manageChange
     * @methodOf talend.widget.controller:BadgeCtrl
     * @description Trigger the change callback
     */
    onChange() {
        const filter = this.filter;
        this.editBadgeValueForm.$commitViewValue();
        this.onChange({filter});
    }

    /**
     * @ngdoc method
     * @name close
     * @methodOf talend.widget.controller:BadgeCtrl
     * @description Trigger the close callback
     */
    close() {
        const filter = this.filter;
        this.onClose({filter});
    }

    /**
     * Remove criterion from a multi-valued filter
     * @param index Position into the multi-valued list
     */
    remove(index) {
        this.filter.value.splice(index, 1);
        return this.onChange();
    }
}