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
 * @name data-prep.services.datasetWorkflowService:UpdateWorkflowService
 * @description UpdateWorkflowService service. This service exposes functions to update a given dataset
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:MessageService
 * @requires data-prep.services.dataset.service:DatasetService
 * @requires data-prep.services.datasetWorkflowService.service:UploadWorkflowService
 */
export default function UpdateWorkflowService(StateService, MessageService, DatasetService, UploadWorkflowService) {
    'ngInject';

    /**
     * @ngdoc method
     * @name updateDataset
     * @methodOf data-prep.services.datasetWorkflowService.service:UpdateWorkflowService
     * @param {object} file - the file to upload
     * @param {object} existingDataset - the existing dataset
     * @description [PRIVATE] Update existing dataset
     */
    this.updateDataset = function updateDataset(file, existingDataset) {
        var dataset = DatasetService.createDatasetInfo(file, existingDataset.name, existingDataset.id);
        StateService.startUploadingDataset(dataset);

        return DatasetService.update(dataset)
            .progress(function (event) {
                dataset.progress = parseInt(100.0 * event.loaded / event.total);
            })
            .then(function () {
                MessageService.success('DATASET_UPDATE_SUCCESS_TITLE', 'DATASET_UPDATE_SUCCESS', {dataset: dataset.name});

                //Force the update currentMetadata of the dataset
                StateService.resetPlayground();
                DatasetService.getDatasetById(dataset.id).then(UploadWorkflowService.openDataset);

            })
            .catch(function () {
                dataset.error = true;
            })
            .finally(function () {
                StateService.finishUploadingDataset(dataset);
            });
    };

}