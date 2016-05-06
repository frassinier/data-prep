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
 * @name data-prep.filter-item.component:FilterItemComponent
 * @description Filter item represented by a bagde with editable content
 * @usage
 <talend-filter ng-model="filter"
                editable="true|false"
                removable="true|false"
                on-close="closeCallback()"
                on-change="changeCallback()"></talend-filter>
 * @param {Object}    filter     Object that contains the values
 * @param {boolean}   editable   If render editable filter values
 * @param {boolean}   removable  If provide remove button to that filter item
 * @param {function}  onChange   The callback that is triggered on content edit
 * @param {function}  onClose    The callback that is triggered on badge close
 */
const FilterItemComponent = {
    templateUrl: 'app/components/filter/item/filter-item.html',
    controller: 'FilterItemCtrl',
    bindings: {
        filter: '<ngModel',
        editable: '<',
        removable: '<',
        onChange: '&',
        onClose: '&'
    }
};

export default FilterItemComponent;