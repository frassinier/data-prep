/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Badge component', () => {
    'use strict';

    let scope, createElement, element,
        fns = {
            close: () => {
            }
        };

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(() => {
        spyOn(fns, 'close').and.returnValue();
    });

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.close = fns.close;
        scope.removable = true;

        createElement = () => {
            const template = '<talend-badge on-close="close()" removable="removable"></talend-badge>';
            element = $compile(template)(scope);
            scope.$digest();
        };
    }));

    it('should call onClose method when badge close button is clicked', () => {
        //given
        createElement();

        //when
        element.find('.badge-btn-close').eq(0).click();

        //then
        expect(fns.close).toHaveBeenCalled();
    });

    it('should not render a close button', () => {
        //given
        scope.removable = false;
        createElement();

        //then
        expect(element.find('.badge-btn-close').length).toBe(0);
    });
});