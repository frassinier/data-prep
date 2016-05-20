/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.transformation.service:TransformationApplicationService
 * @description Manage parameters and apply transformations
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.filters.service:FilterAdapterService
 */
export default function TransformationApplicationService(state, PlaygroundService, FilterAdapterService) {
    'ngInject';

    return {
        append: append,
        editCell: editCell,
        appendClosure: appendClosure
    };

    /**
     * @ngdoc method
     * @name appendClosure
     * @methodOf data-prep.services.transformation.service:TransformationApplicationService
     * @description Transformation application closure. It take the transformation to build the closure.
     * The closure then takes the parameters and append the new step in the current preparation
     */
    function append(action, scope, params) {
        return appendClosure(action, scope)(params);
    }

    /**
     * @ngdoc method
     * @name editCell
     * @methodOf data-prep.services.transformation.service:TransformationApplicationService
     * @param {Object} rowItem The row
     * @param {object} column The column where to execute the transformation
     * @param {string} newValue The new value to put on th target
     * @param {boolean} updateAllCellWithValue Indicates the scope (cell or column) of the transformaton
     * @description Perform a cell or a column edition
     */
    function editCell(rowItem, column, newValue, updateAllCellWithValue) {
        const action = {name: 'replace_on_value'};
        const scope = updateAllCellWithValue ? 'column' : 'cell';
        const params = {
            cell_value: {
                token: rowItem[column.id],
                operator: 'equals'
            },
            replace_value: newValue
        };

        return append(action, scope, params);
    }

    /**
     * @ngdoc method
     * @name appendClosure
     * @methodOf data-prep.services.transformation.service:TransformationApplicationService
     * @description Transformation application closure. It take the transformation to build the closure.
     * The closure then takes the parameters and append the new step in the current preparation
     */
    function appendClosure(action, scope) {
        return function (params) {
            var column = state.playground.grid.selectedColumns[0];
            var line = state.playground.grid.selectedLine;

            params = params || {};
            params.scope = scope;
            params.column_id = column && column.id;
            params.column_name = column && column.name;
            params.row_id = line && line.tdpId;

            if (state.playground.filter.applyTransformationOnFilters) {
                var stepFilters = FilterAdapterService.toTree(state.playground.filter.gridFilters);
                _.extend(params, stepFilters);
            }

            return PlaygroundService.appendStep(action.name, params);
        };
    }
}