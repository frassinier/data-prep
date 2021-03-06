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
 * @name data-prep.lookup.service:LookupDatagridStyleService
 * @description Datagrid private service that manage the grid style
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.utils.service:TextFormatService
 */
export default function LookupDatagridStyleService($timeout, ConverterService, TextFormatService) {
    'ngInject';

    var grid;
    var columnClassTimeout;

    return {
        init: init,
        updateColumnClass: updateColumnClass,
        columnFormatter: columnFormatter
    };

    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name resetCellStyles
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Reset the cells css
     */
    function resetCellStyles() {
        grid.resetActiveCell();
    }

    /**
     * @ngdoc method
     * @name addClass
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Add a css class to a column
     * @param {object} column The target column
     * @param {string} newClass The class to add
     */
    function addClass(column, newClass) {
        column.cssClass = (column.cssClass || '') + ' ' + newClass;
    }

    /**
     * @ngdoc method
     * @name updateSelectionClass
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Add 'selected' class if the column is the selected one
     * @param {object} column The target column
     * @param {object} selectedCol The selected column
     */
    function updateSelectionClass(column, selectedCol) {
        if (column === selectedCol) {
            addClass(column, 'selected');
        }
    }

    /**
     * @ngdoc method
     * @name updateNumbersClass
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Add the 'number' class to the column if its type is a number type
     * @param {object} column the target column
     */
    function updateNumbersClass(column) {
        var simplifiedType = ConverterService.simplifyType(column.tdpColMetadata.type);
        if (simplifiedType === 'integer' || simplifiedType === 'decimal') {
            addClass(column, 'numbers');
        }
    }

    /**
     * @ngdoc method
     * @name updateColumnClass
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Set style classes on columns depending on its state (type, selection, ...)
     * @param {object} columns The columns array
     * @param {object} selectedCol The grid selected column
     */
    function updateColumnClass(columns, selectedCol) {
        _.forEach(columns, function (column) {
            if (column.id === 'tdpId') {
                column.cssClass = 'index-column';
            }
            else {
                column.cssClass = null;
                updateSelectionClass(column, selectedCol);
                updateNumbersClass(column);
            }
        });
    }

    /**
     * @ngdoc method
     * @name columnFormatter
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Value formatter used in SlickGrid column definition. This is called to get a cell formatted value
     * @param {object} col The column to format
     */
    function columnFormatter(col) {

        var invalidValues = col.quality.invalidValues;
        var isInvalid = function isInvalid(value) {
            return invalidValues.indexOf(value) >= 0;
        };

        return function formatter(row, cell, value) {
            //hidden characters need to be shown
            var returnStr = TextFormatService.adaptToGridConstraints(value);
            return returnStr + (isInvalid(value) ? '<div title="Invalid Value" class="red-rect"></div>' : '<div class="invisible-rect"></div>');
        };
    }

    /**
     * @ngdoc method
     * @name attachColumnHeaderCallback
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description attachColumnHeaderListeners callback
     */
    function attachColumnHeaderCallback(event, args) {
        resetCellStyles();
        updateColumnClass(grid.getColumns(), args.column);
        grid.invalidate();
    }

    /**
     * @ngdoc method
     * @name attachColumnHeaderListeners
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Attach style listener on headers. On header selection (on right click or left click) we update the column cells style
     */
    function attachColumnHeaderListeners() {
        grid.onHeaderContextMenu.subscribe(attachColumnHeaderCallback);
        grid.onHeaderClick.subscribe(attachColumnHeaderCallback);
    }

    /**
     * @ngdoc method
     * @name scheduleUpdateColumnClass
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @param {number} colIndex The selected column index
     * @description Cancel the previous scheduled task and schedule a new one to update columns classes.
     */
    function scheduleUpdateColumnClass(colIndex) {
        var columns = grid.getColumns();
        var column = columns[colIndex];

        $timeout.cancel(columnClassTimeout);
        columnClassTimeout = $timeout(function () {
            updateColumnClass(columns, column);
            grid.invalidate();
        }, 100, false);
    }

    /**
     * @ngdoc method
     * @name attachCellListeners
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Attach cell action listeners : update columns classes
     */
    function attachCellListeners() {
        grid.onActiveCellChanged.subscribe(function (e, args) {
            if (angular.isDefined(args.cell)) {
                scheduleUpdateColumnClass(args.cell);
            }
        });
    }

    /**
     * @ngdoc method
     * @name init
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @param {object} newGrid The new grid
     * @description Initialize the grid and attach the style listeners
     */
    function init(newGrid) {
        grid = newGrid;
        attachColumnHeaderListeners();
        attachCellListeners();
    }
}