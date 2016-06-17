/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Filter list directive', function() {
    'use strict';

    var scope, createElement, element;

    var filters = [
        {
            'type': 'exact',
            'colId': '0000',
            'colName': 'name',
            'editable': true,
            'args': {
                'phrase': [
                    {
                        'value': 'AMC Gremlin'
                    }
                ],
                'caseSensitive': true
            },
            'value': [
                {
                    'value': 'AMC Gremlin'
                }
            ]
        },
        {
            'type': 'exact',
            'colId': '0000',
            'colName': 'name',
            'editable': true,
            'args': {
                'phrase': [
                    {
                        'value': 'Chevrolet Caprice Classic'
                    }
                ],
                'caseSensitive': true
            },
            'value': [
                {
                    'value': 'Chevrolet Caprice Classic'
                }
            ]
        },
        {
            'type': 'exact',
            'colId': '0000',
            'colName': 'lastname',
            'editable': true,
            'args': {
                'phrase': [
                    {
                        'value': 'Audi 100 LS'
                    }
                ],
                'caseSensitive': true
            },
            'value': [
                {
                    'value': 'Audi 100 LS'
                }
            ]
        },
        {
            'type': 'inside_range',
            'colId': '0003',
            'colName': 'displacement (cc)',
            'editable': false,
            'args': {
                'intervals': [
                    {
                        'value': [214, 233]
                    }
                ]
            },
            'value': [
                {
                    'value': '[214 .. 233]'
                }
            ]
        }
    ];

    beforeEach(angular.mock.module('data-prep.filter-list'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<filter-list filters="filters"></filter-list>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render filter list badges', function() {
        //given
        scope.filters = filters;

        //when
        createElement();

        //then
        expect(element.find('filter-item').length).toBe(4);
        // FIXME
        // expect(element.find('.badge-notice').eq(0).find('.badge-item').eq(0).text()).toBe('name = ');
        // expect(element.find('.badge-notice').eq(0).find('.editable-input').val()).toBe('AMC Gremlin');
        // expect(element.find('.badge-notice').eq(1).find('.badge-item').eq(0).text()).toBe('name = ');
        // expect(element.find('.badge-notice').eq(1).find('.editable-input').val()).toBe('Chevrolet Caprice Classic');
        // expect(element.find('.badge-notice').eq(2).find('.badge-item').eq(0).text()).toBe('lastname = ');
        // expect(element.find('.badge-notice').eq(2).find('.editable-input').val()).toBe('Audi 100 LS');
        // expect(element.find('.badge-notice').eq(3).find('.badge-item').eq(0).text()).toBe('displacement (cc) in ');
        // expect(element.find('.badge-notice').eq(3).find('.description').eq(0).text()).toBe('[214 .. 233]');
    });
});