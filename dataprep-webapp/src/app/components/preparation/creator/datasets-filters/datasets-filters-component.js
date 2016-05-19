/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DatasetsFiltersCtrl from './datasets-filters-controller';

export default {
    controller: DatasetsFiltersCtrl,
    bindings: {
        onFilterSelect: '&'
    },
    template: `
    <div class="inventory-item" ng-repeat="filter in $ctrl.datasetsFilters track by $index">
        <div class="inventory-icon"
             ng-switch="filter.value"
             ng-click="$ctrl.onFilterSelect({filter: filter.value})">
            <div ng-switch-when="RECENT_DATASETS" data-icon="{{filter.icon}}"></div>
            <div ng-switch-when="FAVORITE_DATASETS" data-icon="{{filter.icon}}"></div>
            <div ng-switch-when="CERTIFIED_DATASETS" ><img src="{{filter.imageUrl}}"/></div>
            <div ng-switch-when="ALL_DATASETS" data-icon="{{filter.icon}}"></div>
        </div>

        <div class="inventory-text">
            <span class="inventory-title" translate="{{filter.value}}"></span>
            <div class="inventory-description" translate="{{filter.description}}"></div>
        </div>
    </div>
`
}