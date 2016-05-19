/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import PreparationCreatorCtrl from './prepration-creator-controller';

export default {
    controller: PreparationCreatorCtrl,
    bindings: {
        showAddPrepModal: '='
    },
    template: `
    <div>
        <span>Existing Datasets</span>
        <input type="text" ng-model="$ctrl.enteredFilterText"/>
    </div>

    <div>
        <datasets-filters on-filter-select="$ctrl.loadFilteredDatasets"></datasets-filters>

        <div class="inventory-list">
            <div class="inventory-item-row"
                 ng-repeat="dataset in $ctrl.filteredDatasets track by $index">
                <inventory-item
                        ng-click="$ctrl.createPreparation(dataset)"
                        type="dataset"
                        item="dataset"
                        details="DATASET_DETAILS"
                        toggle-favorite="angular.noop">
                </inventory-item>
            </div>
        </div>
    </div>

    <button>Browse and import New file</button>
    `
}