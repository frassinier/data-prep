/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Export controller', () => {
    'use strict';

    let scope, createController, form, stateMock, exportTypes;

    const currentParameters = { exportType: 'XLSX' };

    beforeEach(angular.mock.module('data-prep.export', ($provide) => {
        exportTypes = [
            {
                'mimeType': 'text/csv',
                'extension': '.csv',
                'id': 'CSV',
                'needParameters': 'true',
                'defaultExport': 'false',
                'enabled': true,
                'parameters': [
                    {
                        'name': 'csvSeparator',
                        'labelKey': 'CHOOSE_SEPARATOR',
                        'type': 'select',
                        'default': ';',
                        'values': [
                            { 'value': '&#09;', 'labelKey': 'SEPARATOR_TAB' },
                            { 'value': ' ', 'labelKey': 'SEPARATOR_SPACE' },
                            { 'value': ',', 'labelKey': 'SEPARATOR_COMMA' },
                        ],
                    },
                    {
                        'name': 'fileName',
                        'labelKey': 'EXPORT_FILENAME"',
                        'type': 'text',
                        'default': ';',
                    },
                ]
            },
            {
                'mimeType': 'application/tde',
                'extension': '.tde',
                'id': 'TABLEAU',
                'needParameters': 'false',
                'defaultExport': 'false',
                'enabled': true,
            },
            {
                'mimeType': 'application/vnd.ms-excel',
                'extension': '.xlsx',
                'id': 'XLSX',
                'needParameters': 'false',
                'defaultExport': 'true',
                'enabled': true,
            },
        ];

        stateMock = {
            playground: {
                preparation: { name: 'prepname' },
                exportParameters: {
                    exportType: 'CSV',
                    'exportParameters.csvSeparator': ';',
                    'exportParameters.fileName': 'prepname',
                },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject((RestURLs) => {
        RestURLs.setServerUrl('');
    }));

    beforeEach(inject(($rootScope, $controller, $q, ExportService, StorageService) => {
        form = {
            submit: () => {
            }
        };
        scope = $rootScope.$new();

        createController = () => {
            const ctrl = $controller('ExportCtrl', { $scope: scope });
            ctrl.form = form; //simulate init by directive
            return ctrl;
        };

        spyOn(StorageService, 'getExportParams').and.returnValue(currentParameters);
        spyOn(StorageService, 'saveExportParams').and.returnValue();
        spyOn(form, 'submit').and.returnValue();
    }));

    describe('property binding', () => {
        it('should bind stepId getter to RecipeService', inject((RecipeService) => {
            //given
            const ctrl = createController();
            expect(ctrl.stepId).toBeFalsy();

            //when
            RecipeService.getRecipe().push({
                transformation: {
                    stepId: '48da64513c43a548e678bc99'
                }
            });

            //then
            expect(ctrl.stepId).toBe('48da64513c43a548e678bc99');
        }));
    });

    describe('selectType', () => {
        it('should init parameters (type)', () => {
            //given
            const ctrl = createController();
            const csvType = exportTypes[0];

            expect(ctrl.nextSelectedType).not.toBe(csvType);

            //when
            ctrl.selectType(csvType);

            //then
            expect(ctrl.nextSelectedType).toBe(csvType);
            expect(ctrl.nextSelectedType.parameters[1].value).toBe('prepname');
        });

        it('should show modal', () => {
            //given
            const ctrl = createController();
            const csvType = exportTypes[0];

            expect(ctrl.showModal).not.toBe(true);

            //when
            ctrl.selectType(csvType);

            //then
            expect(ctrl.showModal).toBe(true);
        });
    });

    describe('saveAndExport', () => {
        it('should set action in form', inject((RestURLs, $timeout) => {
            //given
            const ctrl = createController();
            ctrl.nextSelectedType = exportTypes[0];

            expect(form.action).toBeFalsy();

            //when
            ctrl.saveAndExport();
            $timeout.flush();

            //then
            expect(form.action).toBe(RestURLs.exportUrl);
        }));

        it('should submit form', inject(($timeout) => {
            //given
            const ctrl = createController();
            ctrl.nextSelectedType = exportTypes[0];

            //when
            ctrl.saveAndExport();
            $timeout.flush();

            //then
            expect(form.submit).toHaveBeenCalled();
        }));

        it('should extract selected parameters', () => {
            //given
            const ctrl = createController();
            ctrl.nextSelectedType = exportTypes[0];
            ctrl.nextSelectedType.parameters[1].value = 'my prep';

            //when
            ctrl.saveAndExport();

            //then
            expect(ctrl.exportParams).toEqual({
                exportType: 'CSV',
                'exportParameters.csvSeparator': ';',
                'exportParameters.fileName': 'my prep'
            });
        });

        it('should save selected parameters', inject((StorageService) => {
            //given
            const ctrl = createController();
            ctrl.nextSelectedType = exportTypes[0];
            ctrl.nextSelectedType.parameters[1].value = 'my prep';

            //when
            ctrl.saveAndExport();

            //then
            expect(StorageService.saveExportParams).toHaveBeenCalledWith({
                exportType: 'CSV',
                'exportParameters.csvSeparator': ';',
                'exportParameters.fileName': 'my prep'
            });
        }));
    });

    describe('launchExport', () => {
        it('should extract selected parameters', () => {
            //given
            const ctrl = createController();
            ctrl.selectedType = exportTypes[0];
            ctrl.selectedType.parameters[1].value = 'my prep';

            //when
            ctrl.launchExport();

            //then
            expect(ctrl.exportParams).toEqual({
                exportType: 'CSV',
                'exportParameters.csvSeparator': ';',
                'exportParameters.fileName': 'my prep'
            });
        });

        it('should set action in form', inject((RestURLs, $timeout) => {
            //given
            const ctrl = createController();
            ctrl.selectedType = exportTypes[0];

            expect(form.action).toBeFalsy();

            //when
            ctrl.launchExport();
            $timeout.flush();

            //then
            expect(form.action).toBe(RestURLs.exportUrl);
        }));

        it('should submit form', inject(($timeout) => {
            //given
            const ctrl = createController();
            ctrl.selectedType = exportTypes[0];

            //when
            ctrl.launchExport();
            $timeout.flush();

            //then
            expect(form.submit).toHaveBeenCalled();
        }));
    });
});