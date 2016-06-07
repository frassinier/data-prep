/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Preparation Creator Controller', () => {

    let createController, scope, ctrl, stateMock;

    const urlQueries = {
        RECENT_DATASETS: '/api/datasets?sort=MODIF&limit=true&name=',
        FAVORITE_DATASETS: '/api/datasets?favorite=true&name=',
        CERTIFIED_DATASETS: '/api/datasets?certified=true&name=',
        ALL_DATASETS: '/api/datasets?name='
    };

    const FILTERS_TYPES = {
        'RECENT': 'RECENT_DATASETS',
        'FAVORITE': 'FAVORITE_DATASETS',
        'CERTIFIED': 'CERTIFIED_DATASETS',
        'ALL': 'ALL_DATASETS',
    };

    beforeEach(angular.mock.module('data-prep.preparation-creator', ($provide) => {
        stateMock = {
            inventory: {
                folder: {
                    content: {
                        preparations : [
                            {id: 'abc-def', name: 'my dataset Preparation'},
                            {id: 'a95-def', name: 'my dataset Preparation (1)'},
                        ]
                    }
                }
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = () => {
            return $componentController('preparationCreator',
                {$scope: scope},
                {showAddPrepModal: true});
        };
    }));

    beforeEach(inject((RestURLs) => {
        RestURLs.setServerUrl('');
    }));

    describe('init', () => {
        it('should load recent datasets by default', inject(($q, DatasetService) => {
            //given
            spyOn(DatasetService, 'loadFilteredDatasets').and.returnValue($q.when(true));
            ctrl = createController();

            //when
            ctrl.$onInit();

            //then
            expect(DatasetService.loadFilteredDatasets).toHaveBeenCalledWith(urlQueries.RECENT_DATASETS);
        }));
    });

    describe('load filtered datasets', () => {
        let filteredDs = [
            {id: 'def12535-212', name: 'datasetName1'},
            {id: 'abc15455-212', name: 'datasetName1'}
        ];

        beforeEach(inject(($q, DatasetService) => {
            spyOn(DatasetService, 'loadFilteredDatasets').and.returnValue($q.when(filteredDs));
        }));

        it('should query the recent datsets without Name filter', inject((DatasetService) => {
            //given
            ctrl = createController();

            //when
            ctrl.loadDatasets(FILTERS_TYPES.RECENT);

            //then
            expect(DatasetService.loadFilteredDatasets).toHaveBeenCalledWith(urlQueries.RECENT_DATASETS);
        }));

        it('should query the favorite datsets without Name filter', inject((DatasetService) => {
            //given
            ctrl = createController();

            //when
            ctrl.loadDatasets(FILTERS_TYPES.FAVORITE);

            //then
            expect(DatasetService.loadFilteredDatasets).toHaveBeenCalledWith(urlQueries.FAVORITE_DATASETS);
        }));

        it('should query the certified datsets without Name filter', inject((DatasetService) => {
            //given
            ctrl = createController();

            //when
            ctrl.loadDatasets(FILTERS_TYPES.CERTIFIED);

            //then
            expect(DatasetService.loadFilteredDatasets).toHaveBeenCalledWith(urlQueries.CERTIFIED_DATASETS);
        }));

        it('should query all the datsets without Name filter', inject((DatasetService) => {
            //given
            ctrl = createController();

            //when
            ctrl.loadDatasets(FILTERS_TYPES.ALL);

            //then
            expect(DatasetService.loadFilteredDatasets).toHaveBeenCalledWith(urlQueries.ALL_DATASETS);
        }));

        it('should fetch the filtered datasets', () => {
            //given
            ctrl = createController();
            expect(ctrl.filteredDatasets).toEqual([]);
            expect(ctrl.isFetchingDatasets).toBe(false);

            //when
            ctrl.loadDatasets(FILTERS_TYPES.ALL);
            expect(ctrl.isFetchingDatasets).toBe(true);
            scope.$digest();

            //then
            expect(ctrl.isFetchingDatasets).toBe(false);
            expect(ctrl.filteredDatasets).toBe(filteredDs);
        });
    });

    describe('loading datasets failure', () => {
        it('should fail while loading the filtered datasets', inject(($q, DatasetService) => {
            //given
            spyOn(DatasetService, 'loadFilteredDatasets').and.returnValue($q.reject());
            ctrl = createController();
            expect(ctrl.filteredDatasets).toEqual([]);
            expect(ctrl.isFetchingDatasets).toBe(false);

            //when
            ctrl.loadDatasets(FILTERS_TYPES.ALL);
            expect(ctrl.isFetchingDatasets).toBe(true);
            scope.$digest();

            //then
            expect(ctrl.isFetchingDatasets).toBe(false);
            expect(ctrl.filteredDatasets).toEqual([]);
        }));
    });

    describe('Import', () => {

        beforeEach(inject(($q, DatasetService) => {
            spyOn(DatasetService, 'createDatasetInfo').and.returnValue();
            spyOn(DatasetService, 'create').and.returnValue($q.when(true));
        }));

        describe('dataset name is NOT available', () => {
            beforeEach(inject(($q, DatasetService) => {
                spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.reject());
                spyOn(DatasetService, 'getUniqueName').and.returnValue($q.when('unique_dataset_name'));
            }));

            it('should call the unique name generator service', inject(($q, DatasetService) => {
                //given
                ctrl = createController();
                ctrl.datasetFile = [{name: 'my Dataset name (1).csv'}];

                //when
                ctrl.import();
                scope.$digest();

                //then
                expect(DatasetService.getUniqueName).toHaveBeenCalledWith('my Dataset name (1)');
                expect(ctrl.datasetName).toBe('unique_dataset_name');
            }));

            it('should call create dataset function', inject(($q, DatasetService) => {
                //given
                ctrl = createController();
                ctrl.datasetFile = [{name: 'my Dataset name (1).csv'}];

                //when
                ctrl.import();
                scope.$digest();

                //then
                expect(DatasetService.getUniqueName).toHaveBeenCalledWith('my Dataset name (1)');
                expect(ctrl.datasetName).toBe('unique_dataset_name');
                expect(DatasetService.createDatasetInfo).toHaveBeenCalled();
                expect(DatasetService.create).toHaveBeenCalled();
            }));
        });

        describe('dataset name is available', () => {
            beforeEach(inject(($q, DatasetService) => {
                spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when(true));
            }));

            it('should check the dataset name availability', inject(($q, DatasetService) => {
                //given
                ctrl = createController();
                ctrl.datasetFile = [{name: 'my Dataset name (1).csv'}];

                //when
                ctrl.import();

                //then
                expect(DatasetService.checkNameAvailability).toHaveBeenCalledWith('my Dataset name (1)');
            }));

            it('should call create dataset function', inject(($q, DatasetService) => {
                //given
                ctrl = createController();
                ctrl.datasetFile = [{name: 'my Dataset name (1).csv'}];

                //when
                ctrl.import();
                scope.$digest();

                //then
                expect(ctrl.datasetName).toBe('my Dataset name (1)');
                expect(DatasetService.createDatasetInfo).toHaveBeenCalled();
                expect(DatasetService.create).toHaveBeenCalled();
            }));
        });

    });

    describe('dataset creation', () => {
        let uploadDefer, dataset;
        beforeEach(inject(($q, DatasetService) => {
            spyOn(DatasetService, 'checkNameAvailability').and.returnValue($q.when(true));
            dataset = {
                name: 'name',
                progress: 0,
                file: {},
                error: false,
                id: 'abc-deff',
                type: 'file'
            };
            spyOn(DatasetService, 'createDatasetInfo').and.returnValue(dataset);

            ctrl = createController();
            ctrl.datasetFile = [{name: 'my Dataset name (1).csv'}];
            scope.$digest();
        }));

        beforeEach(inject(($q) => {
            uploadDefer = $q.defer();
            uploadDefer.promise.progress = (callback) => {
                uploadDefer.progressCb = callback;
                return uploadDefer.promise;
            };
        }));

        beforeEach(inject(($q, $state) => {
            spyOn($state, 'go').and.returnValue();
            ctrl.addPreparationForm = {$commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue()};
        }));

        describe('succeeds', () => {
            beforeEach(inject(($q, $state, DatasetService, PreparationService) => {
                spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);
                spyOn(PreparationService, 'create').and.returnValue($q.when(true));
                spyOn(ctrl, 'createPreparation').and.returnValue();
            }));

            it('should launch preparation creation process once the dataset creation finished', inject(($q, DatasetService) => {
                //given
                spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when(dataset));
                expect(ctrl.whileImport).toBe(false);

                //when
                ctrl.import();
                expect(ctrl.whileImport).toBe(true);
                expect(ctrl.uploadingDatasets.length).toBe(0);
                uploadDefer.resolve({ data: dataset.id });
                scope.$digest();

                //then
                expect(ctrl.whileImport).toBe(false);
                expect(ctrl.uploadingDatasets.length).toBe(0);
                expect(DatasetService.getDatasetById).toHaveBeenCalledWith(dataset.id);
                expect(ctrl.baseDataset).toBe(dataset);
                expect(ctrl.createPreparation).toHaveBeenCalled();
            }));

            it('should create a preparation with a generated unique name', inject(($q, DatasetService) => {
                //given
                ctrl.datasetFile = [{name: 'my dataset.csv'}];
                ctrl.userHasTypedName = false;
                ctrl.enteredName = '';
                spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when({id:'123', name:'my dataset'}));

                //when
                ctrl.import();
                uploadDefer.resolve({ data: dataset.id });
                scope.$digest();

                //then
                expect(ctrl.enteredName).toBe('my dataset Preparation (2)');
            }));

            it('should create a preparation with with the entered name by the user', inject(($q, DatasetService) => {
                //given
                ctrl.datasetFile = [{name: 'my dataset.csv'}];
                ctrl.userHasTypedName = true;
                ctrl.enteredName = 'prep name by user';
                spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when({id:'123', name:'my dataset'}));

                //when
                ctrl.import();
                uploadDefer.resolve({ data: dataset.id });
                scope.$digest();

                //then
                expect(ctrl.enteredName).toBe('prep name by user');
            }));
        });

        describe('progressing', () => {
            it('should update progress bar', inject((DatasetService) => {
                //given
                spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);
                expect(dataset.progress).toBeFalsy();
                const event = {
                    loaded: 100,
                    total: 200
                };

                //when
                ctrl.import();
                scope.$digest();
                uploadDefer.progressCb(event);
                scope.$digest();

                //then
                expect(DatasetService.create).toHaveBeenCalled();
                expect(dataset.progress).toBe(50);
                expect(dataset.error).toBeFalsy();
            }));
        });

        describe('fails', () => {
            it('should trigger erro', inject((DatasetService) => {
                //given
                spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);

                //when
                ctrl.import();
                uploadDefer.reject();
                scope.$digest();

                //then
                expect(dataset.error).toBe(true);
                expect(ctrl.baseDataset).toBe(null);
                expect(ctrl.whileImport).toBe(false);
            }));
        });
    });

    describe('Preparation Creation', () => {
        let newPreparation = {
            id: 'def-12558'
        };
        beforeEach(inject(($q, $state, PreparationService) => {
           spyOn(PreparationService, 'create').and.returnValue($q.when(newPreparation));
           spyOn($state, 'go').and.returnValue();
        }));

        it('should call create preparation service', inject(($state, PreparationService) => {
            //given
            ctrl = createController();
            ctrl.baseDataset = {id: 'abc-54'};
            ctrl.enteredName = 'prep name';
            stateMock.inventory.folder.metadata = {path : '/amaa'};
            ctrl.addPreparationForm = {$commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue()};

            //when
            ctrl.createPreparation();
            scope.$digest();

            //then
            expect(PreparationService.create).toHaveBeenCalledWith(ctrl.baseDataset.id, ctrl.enteredName, stateMock.inventory.folder.metadata.path);
            expect(ctrl.showAddPrepModal).toBe(false);
            expect($state.go).toHaveBeenCalledWith('playground.preparation', {prepid: newPreparation.id});
        }));
    });

    describe('Preparation Name availability', () => {
        it('should update userHasTypedName flag', () => {
            //given
            ctrl = createController();
            expect(ctrl.userHasTypedName).toBe(false);

            //when
            ctrl.checkExistingPrepName('user');

            //then
            expect(ctrl.userHasTypedName).toBe(true);
        });

        it('should not update userHasTypedName flag', () => {
            //given
            ctrl = createController();
            expect(ctrl.userHasTypedName).toBe(false);

            //when
            ctrl.checkExistingPrepName();

            //then
            expect(ctrl.userHasTypedName).toBe(false);
        });

        it('should set alreadyExistingName flag to true', () => {
            //given
            ctrl = createController();
            ctrl.enteredName = 'my dataset Preparation';

            //when
            ctrl.checkExistingPrepName();

            //then
            expect(ctrl.userHasTypedName).toBe(false);
        });

        it('should set alreadyExistingName flag to false', () => {
            //given
            ctrl = createController();
            ctrl.enteredName = 'unique name';

            //when
            ctrl.checkExistingPrepName();

            //then
            expect(ctrl.userHasTypedName).toBe(false);
        });
    });

    describe('Dataset Name Filter', () => {
        it('should call load datasets', () => {
            //given
            ctrl = createController();
            ctrl.lastFilterValue = 'my';
            spyOn(ctrl, 'loadDatasets').and.returnValue();

            //when
            ctrl.applyNameFilter();

            //then
            expect(ctrl.loadDatasets).toHaveBeenCalledWith(ctrl.lastFilterValue);
        });
    });

    describe('Base dataset selection', () => {
        let dataset = {
            id: 'abc-5424',
            name: 'my dataset'
        };

        let lastSelectedDataset = {
            id: 'abc-5424',
            name: 'my dataset',
            isSelected : true
        };

        it('should update selection flag for the 1st dataset select', () => {
            //given
            ctrl = createController();
            ctrl.lastSelectedDataset = null;

            //when
            ctrl.selectBaseDataset(dataset);

            //then
            expect(dataset.isSelected).toBe(true);
            expect(ctrl.baseDataset).toBe(dataset);
        });

        it('should update selection flag', () => {
            //given
            ctrl = createController();
            ctrl.lastSelectedDataset = lastSelectedDataset;

            //when
            ctrl.selectBaseDataset(dataset);

            //then
            expect(ctrl.lastSelectedDataset).toBe(dataset);
            expect(dataset.isSelected).toBe(true);
            expect(ctrl.baseDataset).toBe(dataset);
        });

        it('should generate a unique preparation Name given the selected dataset', () => {
            //given
            ctrl = createController();
            ctrl.userHasTypedName = false;
            ctrl.enteredName = '';

            //when
            ctrl.selectBaseDataset(dataset);

            //then
            expect(ctrl.enteredName).toBe('my dataset Preparation (2)');
        });

        it('should NOT change the entered preparation Name by the user', () => {
            //given
            ctrl = createController();
            ctrl.userHasTypedName = true;
            ctrl.enteredName = 'prep name';

            //when
            ctrl.selectBaseDataset(dataset);

            //then
            expect(ctrl.enteredName).toBe('prep name');
        });
    });

    describe('Form submission', () => {
        it('should disable form submission when entered name is empty', () => {
            //given
            ctrl = createController();
            ctrl.enteredName = '';
            ctrl.lastSelectedDataset = {};
            ctrl.alreadyExistingName = false;

            //when
            const enabledForm = ctrl.anyMissingEntries();

            //then
            expect(enabledForm).toBeFalsy();
        });

        it('should disable form submission when there is no selected dataset', () => {
            //given
            ctrl = createController();
            ctrl.enteredName = 'prep Name';
            ctrl.lastSelectedDataset = null;
            ctrl.alreadyExistingName = false;

            //when
            const enabledForm = ctrl.anyMissingEntries();

            //then
            expect(enabledForm).toBeFalsy();
        });

        it('should disable form submission when entered name already exists', () => {
            //given
            ctrl = createController();
            ctrl.enteredName = 'prep Name';
            ctrl.lastSelectedDataset = {};
            ctrl.alreadyExistingName = true;

            //when
            const enabledForm = ctrl.anyMissingEntries();

            //then
            expect(enabledForm).toBeFalsy();
        });
    });

    describe('Import button title generation', () => {
        it('should return while import title', () => {
            //given
            ctrl = createController();
            ctrl.whileImport = true;

            //when
            const title = ctrl.getImportTitle();

            //then
            expect(title).toBe('IMPORT_IN_PROGRESS');
        });

        it('should return already existing name title', () => {
            //given
            ctrl = createController();
            ctrl.alreadyExistingName = true;

            //when
            const title = ctrl.getImportTitle();

            //then
            expect(title).toBe('TRY_CHANGING_NAME');
        });

        it('should return import file description', () => {
            //given
            ctrl = createController();
            ctrl.alreadyExistingName = false;

            //when
            const title = ctrl.getImportTitle();

            //then
            expect(title).toBe('IMPORT_FILE_DESCRIPTION');
        });
    });
});