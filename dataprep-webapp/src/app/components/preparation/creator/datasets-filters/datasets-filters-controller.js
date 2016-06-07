/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class DatasetsFiltersCtrl {
    constructor() {
        'ngInject';

        this.datasetsFilters = [//The order is important
            {
                value: 'RECENT_DATASETS',
                imageUrl: '/assets/images/inventory/recent-datasets.png',
                description:'RECENT_DATASETS_DESCRIPTION',
                isSelected: true
            },
            {
                value: 'FAVORITE_DATASETS',
                icon: 'f',
                description:'FAVORITE_DATASETS_DESCRIPTION'
            },
            {
                value: 'CERTIFIED_DATASETS',
                imageUrl: '/assets/images/inventory/certified_no_shadow.png',
                description:'CERTIFIED_DATASETS_DESCRIPTION'
            },
            {
                value: 'ALL_DATASETS',
                imageUrl: '/assets/images/inventory/all-datasets.png',
                description:'ALL_DATASETS_DESCRIPTION'
            }
        ];

        this.selectedFilter = this.datasetsFilters[0];
    }

    selectFilter(filter) {
        if(this.importing){
            return;
        }
        this.selectedFilter.isSelected = false;
        this.selectedFilter = filter;
        this.selectedFilter.isSelected = true;
        this.onFilterSelect({filter: filter.value});
    }
}

export default DatasetsFiltersCtrl;