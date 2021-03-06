/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Suggestions stats directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(angular.mock.module('data-prep.suggestions-stats'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile, $timeout) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<suggestions-stats></suggestions-stats>');
            $compile(element)(scope);
            scope.$digest();
            $timeout.flush();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render suggestions/stats splitter', inject(function() {
        //when
        createElement();

        //then
        expect(element.find('sc-splitter').length).toBe(1);
        expect(element.find('sc-splitter sc-split-first-pane actions-suggestions').length).toBe(1);
        expect(element.find('sc-splitter sc-split-second-pane stats-details').length).toBe(1);
    }));
});