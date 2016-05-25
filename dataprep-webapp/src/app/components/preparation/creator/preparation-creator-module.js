/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import PreparationCreatorComponent from './preparation-creator-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.preparation-creator
     * @description This module creates directly a preparation from a dataset
     * @requires data-prep.inventory-item
     * @requires data-prep.datasets-filters
     */
    angular.module('data-prep.preparation-creator',
        [
            'data-prep.inventory-item',
            'data-prep.datasets-filters',
            'data-prep.services.dataset',
            'data-prep.dataset-upload-list',
            'data-prep.import',
            'data-prep.services.preparation',
            'data-prep.services.utils',
        ])
        .component('preparationCreator', PreparationCreatorComponent);
})();