/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class PreparationCreatorCtrl {
    constructor(state, $document, $state, RestURLs,
                PreparationService, DatasetService, UploadWorkflowService) {
        'ngInject';

        this.$document = $document;
        this.$state = $state;
        this.preparationService = PreparationService;
        this.datasetService = DatasetService;
        this.UploadWorkflowService = UploadWorkflowService;
        this.restURLs = RestURLs;
        this.enteredFilterText = '';
        this.filteredDatasets = [];
        this.lastFilterValue = '';
        this.state = state;
        this.baseDataset = null;
        this.lastSelectedDataset = null;
        this.userHasTypedName = false;
        this.uploadingDatasets = [];
        this.whileImport = false;
        this.isFetchingDatasets = false;
    }

    $onInit() {
        this.loadDatasets('RECENT_DATASETS');
    }

    /**
     * @ngdoc method
     * @name loadDatasets
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description loads the filtered datasets
     * @params {String} filterValue the chosen filter value
     */
    loadDatasets(filterValue) {
        this.lastFilterValue = filterValue;
        this.url = this.restURLs.datasetUrl;
        switch (filterValue) {
            case 'RECENT_DATASETS':
                this.url += '?sort=MODIF&limit=true&name=';
                break;
            case 'FAVORITE_DATASETS':
                this.url += '?favorite=true&name=';
                break;
            case 'CERTIFIED_DATASETS':
                this.url += '?certified=true&name=';
                break;
            case 'ALL_DATASETS':
                this.url += '?name=';
                break;
        }
        this.url += this.enteredFilterText;
        this.isFetchingDatasets = true;
        this.datasetService.loadFilteredDatasets(this.url)
            .then((filteredDatasets) => {
                this.filteredDatasets = filteredDatasets;
            })
            .finally(() => {
                this.isFetchingDatasets = false;
            });
    }

    /**
     * @ngdoc method
     * @name import
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description imports the chosen dataset
     */
    import() {
        const file = this.datasetFile[0];
        const datasetName = file.name;

        // remove file extension and ask final name
        this.datasetName = datasetName.replace(/\.[^/.]+$/, '');
        this.whileImport = true;
        this.datasetService.checkNameAvailability(this.datasetName)
            .catch(() => {
                return this.datasetService.getUniqueName(this.datasetName)
                    .then((uniqueName) => {
                        this.datasetName = uniqueName;
                    });
            })
            .finally(() => {
                this._createDataset(file, this.datasetName);
            });
    }

    /**
     * @ngdoc method
     * @name _createDataset
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description [PRIVATE] creates a dataset and manages the progress bar
     * @params {Object} file to create
     * @params {String} name of the dataset
     */
    _createDataset(file, name) {
        const params = {
            datasetFile: "",
            type: "local",
            name: name
        };

        const dataset = this.datasetService.createDatasetInfo(file, name);
        this.uploadingDatasets.push(dataset);
        this.datasetService.create(params, 'text/plain', file)
            .progress((event) => {
                dataset.progress = parseInt(100.0 * event.loaded / event.total);
            })
            .then((event) => {
                this.whileImport = false;
                return this.datasetService.getDatasetById(event.data)
                    .then((dataset) => {
                        this.uploadingDatasets = [];
                        this.baseDataset = dataset;
                        if (!this.userHasTypedName) {
                            this._getUniquePrepName();
                        }
                        this.createPreparation();
                    });
            })
            .catch(() => {
                dataset.error = true;
                this.whileImport = false;
            });
    }

    /**
     * @ngdoc method
     * @name _getUniquePrepName
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description [PRIVATE] generates a unique preparation name
     * @params {Number} index the index to increment
     */
    _getUniquePrepName(index = 0) {
        const suffix = index === 0 ? ' Preparation' : ' Preparation (' + index + ')';

        this.enteredName = this.baseDataset.name + suffix;
        const existingName = _.some(this.state.inventory.folder.content.preparations, {name: this.enteredName});
        if (existingName) {
            this._getUniquePrepName(index + 1);
        }
    }

    /**
     * @ngdoc method
     * @name createPreparationFromMultiSheetDataset
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description created the preparation from a multi sheet dataset
     */
    createPreparationFromMultiSheetDataset() {
        this.showAddPrepModal = false;
        this.baseDataset.name = this.enteredName;
        this.UploadWorkflowService.openDraft(this.baseDataset, true);
    }

    /**
     * @ngdoc method
     * @name createPreparation
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description created the preparation
     */
    createPreparation() {
        if (this.baseDataset.draft) {
            this.createPreparationFromMultiSheetDataset();
        } else {
            this.addPreparationForm.$commitViewValue();
            this.preparationService.create(this.baseDataset.id, this.enteredName, this.state.inventory.folder.metadata.id)
                .then((newPreparation) => {
                    this.showAddPrepModal = false;
                    this.$state.go('playground.preparation', {prepid: newPreparation.id});
                });
        }
    }

    /**
     * @ngdoc method
     * @name checkExistingPrepName
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description creates a dataset and manages the progress bar
     * @params {String} from the caller
     */
    checkExistingPrepName(from) {
        if (from === 'user') {
            this.userHasTypedName = true;
        }
        this.alreadyExistingName = _.some(this.state.inventory.folder.content.preparations, {name: this.enteredName});
    }

    /**
     * @ngdoc method
     * @name applyNameFilter
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description generates a unique preparation name
     */
    applyNameFilter() {
        this.lastFilterValue && this.loadDatasets(this.lastFilterValue);
    }

    /**
     * @ngdoc method
     * @name selectBaseDataset
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description selects the base dataset to be used in the preparation
     * @params {Object} dataset the base dataset
     */
    selectBaseDataset(dataset) {
        if (this.lastSelectedDataset) {
            this.lastSelectedDataset.isSelected = false;
        }
        this.lastSelectedDataset = dataset;
        dataset.isSelected = true;
        this.baseDataset = dataset;
        if (!this.userHasTypedName) {
            this._getUniquePrepName();
        }
    }

    /**
     * @ngdoc method
     * @name anyMissingEntries
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description checks if there is a unique preparation name and there is a selected base dataset
     * @returns boolean
     */
    anyMissingEntries() {
        return this.enteredName && this.lastSelectedDataset && !this.alreadyExistingName;
    }

    /**
     * @ngdoc method
     * @name importFile
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description triggers the click on the upload input
     */
    importFile() {
        if (!this.whileImport && !this.alreadyExistingName) {
            this.$document.find('#localFileImport').eq(0).click();
        }
    }

    /**
     * @ngdoc method
     * @name getImportTitle
     * @methodOf data-prep.preparation-creator.controller:PreparationCreatorCtrl
     * @description creates the tooltip content
     * @returns {String} the tooltip content
     */
    getImportTitle() {
        if (this.whileImport) {
            return 'IMPORT_IN_PROGRESS';
        }

        if (this.alreadyExistingName) {
            return 'TRY_CHANGING_NAME';
        }
        return 'IMPORT_FILE_DESCRIPTION';
    }
}
export default PreparationCreatorCtrl;