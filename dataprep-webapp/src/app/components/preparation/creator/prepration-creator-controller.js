/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class InventoryCopyMoveCtrl {
    constructor(RestURLs, PreparationService, DatasetService) {
        'ngInject';

        this.preparationService = PreparationService;
        this.datasetService = DatasetService;
        this.url = RestURLs.datasetUrl;
        this.enteredFilterText = '';
    }

    loadDatasets(filterValue) {
        switch (filterValue) {
            case 'RECENT_DATASETS':
                this.url += '?sort=MODIF&limit=true&name=' + this.enteredFilterText;
                break;
            case 'FAVORITE_DATASETS':
                this.url += '?favorite=true&name=' + this.enteredFilterText;
                break;
            case 'CERTIFIED_DATASETS':
                this.url += '?certified=true&name=' + this.enteredFilterText;
                break;
            case 'ALL_DATASETS':
                this.url += '?name=' + this.enteredFilterText;
                break;
        }
        this.datasetService.loadFilteredDatasets(this.url)
            .then((filteredDatasets) => {
                this.filteredDatasets = filteredDatasets;
            })
    }

    createPreparation(dataset) {
        this.preparationService.create(dataset.id, dataset.name);
        this.showAddPrepModal = false;
    }
}

export default InventoryCopyMoveCtrl;