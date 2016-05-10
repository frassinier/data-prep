/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import Import from './import-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.import
     * @description import component.
     * @requires talend.widget
     * @requires data-prep.services.dataset
     * @requires data-prep.services.datasetWorkflowService
     * @requires data-prep.services.state
     */
    angular.module('data-prep.import',
        [
            'talend.widget',
            'data-prep.services.dataset',
            'data-prep.services.datasetWorkflowService',
            'data-prep.services.state',
        ])
        .component('import', Import);
})();
