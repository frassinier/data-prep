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
 * @name data-prep.filter-item-value.component:FilterValueComponent
 * @description Filter item value represented by an input with editable content or a span
 * @usage
 <filter-value ng-model="filterValue"
                ng-focus="ngFocus"
                editable="true|false"
                removable="true|false"
                remove="removeCallback()"></filter-value>
 * @param {Object}     filterValue   TODO
 * @param {Expression} onFocus       TODO
 * @param {boolean}    editable      If render editable filter values
 * @param {boolean}    removable     If provide remove button to that filter item
 * @param {Function}   remove        TODO
 */
const FilterValueComponent = {
    templateUrl: 'app/components/filter/item/value/filter-value.html',
    bindings: {
        filterValue: '=ngModel',
        onFocus: '@ngFocus',
        editable: '<',
        removable: '=',
        remove: '&'
    }
};

export default FilterValueComponent;