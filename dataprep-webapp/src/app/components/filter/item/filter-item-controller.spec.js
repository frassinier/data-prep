/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Filter item controller', () => {
    'use strict';

    let createController, scope,
        filterType,
        filterValue,
        filterFns = {
            close: () => {
            }
        };

    beforeEach(angular.mock.module('data-prep.filter-item'));
    beforeEach(angular.mock.module('pascalprecht.translate', $translateProvider => {
        $translateProvider.translations('en', {
            'COLON': ': '
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $componentController) => {
        spyOn(filterFns, 'close').and.returnValue();

        scope = $rootScope.$new();

        createController = () => {
            const filter = {
                type: filterType,
                value: filterValue
            };

            let ctrl = $componentController('filterItem', {
                $scope: scope
            }, {
                filter: filter,
                onClose: filterFns.close
            });
            return ctrl;
        };
    }));

    it('should set the sign character to : in', () => {
        //given
        filterType = 'inside_range';
        const ctrl = createController();

        //when
        ctrl.$onInit();
        scope.$digest();

        //then
        expect(ctrl.sign).toEqual(' in ');
    });

    it('should set the sign character to : ":"', () => {
        //given
        filterType = 'valid_records';
        const ctrl = createController();

        //when
        ctrl.$onInit();
        scope.$digest();

        //then
        expect(ctrl.sign).toEqual(': ');
    });

    it('should set the sign character to : "≅"', () => {
        //given
        filterType = 'contains';
        const ctrl = createController();

        //when
        ctrl.$onInit();
        scope.$digest();

        //then
        expect(ctrl.sign).toEqual(' ≅ ');
    });

    it('should set the sign character to : "=" ', () => {
        //given
        filterType = 'exact';
        const ctrl = createController();

        //when
        ctrl.$onInit();
        scope.$digest();

        //then
        expect(ctrl.sign).toEqual(' = ');
    });
});