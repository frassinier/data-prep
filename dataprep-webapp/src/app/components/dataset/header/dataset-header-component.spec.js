/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataset header component', () => {
    let scope, createElement, element, stateMock;

    const sortList = [
        {id: 'name', name: 'NAME_SORT', property: 'name'},
        {id: 'date', name: 'DATE_SORT', property: 'created'}
    ];

    const orderList = [
        {id: 'asc', name: 'ASC_ORDER'},
        {id: 'desc', name: 'DESC_ORDER'}
    ];

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            'SORTED_BY': 'Sorted by',
            'SORT_IN': 'in',
            'SORT_ORDER': 'order',
            'NAME_SORT': 'name',
            'ASC_ORDER': 'asc'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(angular.mock.module('data-prep.dataset-header', ($provide) => {
        stateMock = {
            inventory: {
                sortList: sortList,
                orderList: orderList,
                datasetsSort: sortList[0],
                datasetsOrder: orderList[0]
            },
            import:{
                importTypes: [
                    {
                        "locationType": "local",
                        "contentType": "text/plain",
                        "parameters": [
                            {
                                "name": "datasetFile",
                                "type": "file",
                                "implicit": false,
                                "canBeBlank": false,
                                "placeHolder": "*.csv",
                                "description": "File",
                                "label": "File",
                                "default": ""
                            }
                        ],
                        "dynamic": false,
                        "defaultImport": true,
                        "label": "Local File",
                        "title": "Add local file dataset"
                    }
                ]
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        createElement = () => {
            element = angular.element('<dataset-header></dataset-header>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

/*    it('should render sort switch', () => {
        //when
        createElement();

        //then
        expect(element.find('.inventory-sort').text().replace(/[\s]+/g, ' ').trim()).toBe('Sorted by name in asc order');
    });*/
});
