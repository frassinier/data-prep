/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Resizable input directive', () => {
    'use strict';

    let scope, createElement, element;
    let body = angular.element('body');
    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('htmlTemplates'));

    const defaultInput = {
        content: 'Lorem ipsum',
        width: '84px'
    };
    const newInput = {
        content: 'Lorem ipsum dolor sit amet',
        width: '189px'
    };

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.value = defaultInput.content;
        scope.removable = false;
        createElement = () => {
            element = angular.element(`<input ng-model="value" resizable-input removable="removable" >`);
            body.append(element);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render input', () => {
        //when
        createElement();

        //then
        expect(element.length).toBe(1);
        expect(element.css('width')).toBe(defaultInput.width);
    });

    it('should adjust input size on ngModel change', () => {
        //given
        createElement();
        expect(element.css('width')).toBe(defaultInput.width);

        //when
        scope.value = newInput.content;
        scope.$digest();

        //then
        expect(element.css('width')).toBe(newInput.width);
    });
});