/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc component
 * @name data-prep.datasets-filters:datasetsFilters
 * @description This component renders datasets filters list in the left panel
 * @usage
 *      <datasets-filters
 *          on-filter-select="$ctrl.loadDatasets(filter)">
 *          importing="$ctrl.whileImport">
 *       </datasets-filters>
 * @param {function} onFilterSelect callback
 * @param {Boolean} importing while import set to true
 * */

import DatasetsFiltersCtrl from './datasets-filters-controller';

export default {
    controller: DatasetsFiltersCtrl,
    bindings: {
        onFilterSelect: '&',
        importing: '<'
    },
    template: `
    <div class="dataset-filter" ng-class="{'selected-filter': filter.isSelected}"
        ng-repeat="filter in $ctrl.datasetsFilters track by $index"
        ng-click="$ctrl.selectFilter(filter)">
        <div class="dataset-filter-icon"
             ng-switch="filter.value">
            <div ng-switch-when="RECENT_DATASETS"><img ng-src="{{filter.imageUrl}}"/></div>
            <div ng-switch-when="FAVORITE_DATASETS" data-icon="{{filter.icon}}" class="favorite"></div>
            <div ng-switch-when="CERTIFIED_DATASETS" ><img ng-src="{{filter.imageUrl}}"/></div>
            <div ng-switch-when="ALL_DATASETS"><img ng-src="{{filter.imageUrl}}"/></div>
        </div>

        <div class="dataset-filter-text">
            <span class="dataset-filter-title" translate-once="{{filter.value}}"></span>
            <div class="dataset-filter-description" translate-once="{{filter.description}}"></div>
        </div>
    </div>
`
}