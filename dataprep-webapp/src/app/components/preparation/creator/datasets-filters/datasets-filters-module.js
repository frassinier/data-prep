/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DatasetsFiltersComponent from './datasets-filters-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.inventory-copy-move
     * @description This module contains the entities to manage the inventory copy/move wiazerd
     * @requires data-prep.folder-selection
     */
    angular.module('data-prep.datasets-filters',
        [])
        .component('datasetsFilters', DatasetsFiltersComponent);
})();