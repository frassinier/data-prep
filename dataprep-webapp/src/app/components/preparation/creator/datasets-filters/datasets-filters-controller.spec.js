/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Datasets Filters Controller', () => {

    let createController, scope, ctrl;

    beforeEach(angular.mock.module('data-prep.datasets-filters'));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = () => {
            return $componentController('datasetsFilters',
                {$scope: scope},
                {onFilterSelect: jasmine.createSpy()});
        };
    }));

    describe('Filter selection', () => {
        it('should select a datasets filter', () => {
            //given
            ctrl = createController();
            let filter = {value: 'RECENT_DATASETS'};

            //when
            ctrl.selectFilter(filter);

            //then
            expect(ctrl.selectedFilter).toBe(filter);
            expect(ctrl.selectedFilter.isSelected).toBe(true);
            expect(ctrl.onFilterSelect).toHaveBeenCalledWith({filter: filter.value});
        });

        it('should NOT select a datasets filter while import', () => {
            //given
            ctrl = createController();
            let filter = {value: 'RECENT_DATASETS'};
            ctrl.importing = true;
            expect(ctrl.onFilterSelect).not.toHaveBeenCalled();

            //when
            ctrl.selectFilter(filter);

            //then
            expect(ctrl.selectedFilter).not.toBe(filter);
            expect(ctrl.selectedFilter.isSelected).toBe(true);
            expect(ctrl.onFilterSelect).not.toHaveBeenCalled();
        });
    });
});