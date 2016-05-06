/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import FilterBar from './filter-bar-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.filter-bar
     * @description This module aggregate the filters components into a bar
     */
    angular.module('data-prep.filter-bar',
        [
            'data-prep.filter-list',
            'data-prep.filter-item',
            'data-prep.filter-monitor',
            'data-prep.filter-search',
        ])
        .directive('filterBar', FilterBar);
})();
