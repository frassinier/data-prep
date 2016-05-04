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
 * @name data-prep.services.dataset.service:DatasetListService
 * @description Dataset grid service. This service holds the dataset list like a cache and consume DatasetRestService to access to the REST api<br/>
 * <b style="color: red;">WARNING : do NOT use this service directly.
 * {@link data-prep.services.dataset.service:DatasetService DatasetService} must be the only entry point for datasets</b>
 * @requires data-prep.services.dataset.service:DatasetRestService
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:StorageService
 */
export default function DatasetListService($q, state, DatasetRestService, StateService) {
    'ngInject';

    var deferredCancel;
    var datasetsPromise;

    return {
        refreshDatasets: refreshDatasets,
        getDatasetsPromise: getDatasetsPromise,
        hasDatasetsPromise: hasDatasetsPromise,

        create: create,
        clone: clone,
        move: move,
        update: update,
        delete: deleteDataset,

        processCertification: processCertification,
        toggleFavorite: toggleFavorite
    };

    /**
     * @ngdoc method
     * @name cancelPendingGetRequest
     * @methodOf data-prep.services.dataset.service:DatasetListService
     * @description Cancel the pending datasets list GET request
     */
    function cancelPendingGetRequest() {
        if (datasetsPromise) {
            deferredCancel.resolve('user cancel');
            datasetsPromise = null;
        }
    }

    /**
     * @ngdoc method
     * @name refreshDatasets
     * @methodOf data-prep.services.dataset.service:DatasetListService
     * @description Refresh datasets list
     * @returns {promise} The pending GET promise
     */
    function refreshDatasets() {
        cancelPendingGetRequest();
        var sort = state.inventory.sort.id;
        var order = state.inventory.order.id;

        deferredCancel = $q.defer();
        datasetsPromise = DatasetRestService.getDatasets(sort, order, deferredCancel)
            .then((res) => {
                StateService.setDatasets(res.data);
                return res.data;
            })
            .catch(() => {
                StateService.setDatasets([]);
                return [];
            })
            .finally(() => datasetsPromise = null);
        return datasetsPromise;
    }

    /**
     * @ngdoc method
     * @name clone
     * @methodOf data-prep.services.dataset.service:DatasetListService
     * @param {object} dataset The dataset to clone
     * @param {object} folder to clone the dataset
     * @param {string} cloneName the name for the cloned dataset
     * @description Clone a dataset from backend and refresh its internal list
     * @returns {promise} The pending GET promise
     */
    function clone(dataset, folder, cloneName) {
        var promise = DatasetRestService.clone(dataset, folder, cloneName);
        promise.then(refreshDatasets);

        return promise;
    }

    /**
     * @ngdoc method
     * @name move
     * @methodOf data-prep.services.dataset.service:DatasetListService
     * @param {Object} dataset the dataset infos to move
     * @param {Object} newFolder the folder to move the dataset
     * @param {String} newName the name for the moved dataset (optional)
     * @description Move a dataset from backend and refresh its internal list
     * @returns {promise} The pending PUT promise
     */
    function move(dataset, newFolder, newName) {
        var currentFolder = state.inventory.currentFolder;
        var promise = DatasetRestService.move(dataset, currentFolder, newFolder, newName);
        promise.then(refreshDatasets);

        return promise;
    }

    /**
     * @ngdoc method
     * @name create
     * @methodOf data-prep.services.dataset.service:DatasetListService
     * @param {object} parameters The import parameters to import
     * @param {object} folder - the dataset folder
     * @param {object} file The file imported from local
     * @param {string} contentType The request Content-Type
     * @description Import a remote dataset from backend and refresh its internal list
     * @returns {promise} The pending POST promise
     */
    function create(folder, parameters, contentType, file) {
        var promise = DatasetRestService.create(folder, parameters, contentType, file);

        //The appended promise is not returned because DatasetRestService.import return a $upload object with progress function
        //which is used by the caller
        promise.then(refreshDatasets);

        return promise;
    }

    /**
     * @ngdoc method
     * @name update
     * @methodOf data-prep.services.dataset.service:DatasetListService
     * @param {object} dataset The dataset to delete
     * @description Update a dataset from backend and refresh its internal list
     * @returns {promise} The pending POST promise
     */
    function update(dataset) {
        var promise = DatasetRestService.update(dataset);

        //The appended promise is not returned because DatasetRestService.import return a $upload object with progress function
        //which is used by the caller
        promise.then(refreshDatasets);

        return promise;
    }

    /**
     * @ngdoc method
     * @name processCertification
     * @methodOf data-prep.services.dataset.service:DatasetService
     * @param {object} dataset The target dataset for certification
     * @description Ask certification for a dataset and refresh its internal list
     * @returns {promise} The pending PUT promise
     */
    function processCertification(dataset) {
        return DatasetRestService.processCertification(dataset.id)
            .then(refreshDatasets);
    }

    /**
     * @ngdoc method
     * @name delete
     * @methodOf data-prep.services.dataset.service:DatasetListService
     * @param {object} dataset The dataset to delete
     * @description Delete a dataset from backend and from its internal list
     * @returns {promise} The pending DELETE promise
     */
    function deleteDataset(dataset) {
        return DatasetRestService.delete(dataset)
            .then(function () {
                StateService.removeDataset(dataset);
            });
    }

    /**
     * @ngdoc method
     * @name getDatasetsPromise
     * @methodOf data-prep.services.dataset.service:DatasetListService
     * @description Return resolved or unresolved promise that returns the most updated datasetsList
     * @returns {promise} Promise that resolves datasetsList
     */
    function getDatasetsPromise() {
        return datasetsPromise ? datasetsPromise : refreshDatasets();
    }

    /**
     * @ngdoc method
     * @name hasDatasetsPromise
     * @methodOf data-prep.services.dataset.service:DatasetListService
     * @description Check if datasetsPromise is true or not
     * @returns {promise} datasetsPromise
     */
    function hasDatasetsPromise() {
        return datasetsPromise;
    }

    /**
     * @ngdoc method
     * @name toggleFavorite
     * @methodOf data-prep.services.dataset.service:DatasetListService
     * @param {object} dataset The target dataset to set or unset favorite
     * @description Set or Unset the dataset as favorite
     * @returns {promise} The pending POST promise
     */
    function toggleFavorite(dataset) {
        return DatasetRestService.toggleFavorite(dataset)
            .then(() => dataset.favorite = !dataset.favorite) //update currentFolderContent.datasets
            .then(refreshDatasets); //update inventory.datasets
    }
}