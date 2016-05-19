/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class InventoryCopyMoveCtrl {
    constructor() {
        'ngInject';

        this.datasetsFilters = [
            {
                value: 'RECENT_DATASETS',
                icon: 'c',
                description:'RECENT_DATASETS_DESCRIPTION'
            },
            {
                value: 'FAVORITE_DATASETS',
                icon: 'f',
                description:'FAVORITE_DATASETS_DESCRIPTION'
            },
            {
                value: 'CERTIFIED_DATASETS',
                imageUrl: '/assets/images/inventory/certification-certified',
                description:'CERTIFIED_DATASETS_DESCRIPTION'
            },
            {
                value: 'ALL_DATASETS',
                icon: 'c',
                description:'ALL_DATASETS_DESCRIPTION'
            }
        ];//The order is important
    }
}

export default InventoryCopyMoveCtrl;