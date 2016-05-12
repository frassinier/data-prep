/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Playground header component', () => {
    'use strict';

    let scope, createElement, element;

    beforeEach(angular.mock.module('data-prep.playground'));
    beforeEach(angular.mock.module('htmlTemplates'));
    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            'FILE_DETAILS_LINES': '{{records}} lines',
            'FILE_DETAILS_LIMIT': 'cut at {{records}} lines'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            element = angular.element(`
                <playground-header dataset="dataset"
                    display-nb-lines="displayNbLines"
                    preview="preview"
                    lookup-visible="lookupVisible"
                    feedback-visible="feedbackVisible"
                    parameters-visible="parametersVisible"
                    on-parameters="onParameters()"
                    on-lookup="onLookup()"
                    on-onboarding="onOnboarding()"
                    on-feedback="onFeedback()"
                    on-close="onClose()"></playground-header>`);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    beforeEach(inject((StorageService) => {
        spyOn(StorageService, 'getExportParams').and.returnValue({});
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('preview badge', () => {
        it('should NOT be rendered', () => {
            //given
            scope.preview = false;

            //when
            createElement();

            //then
            expect(element.find('#preview').length).toBe(0);
        });

        it('should be rendered', () => {
            //given
            scope.preview = true;

            //when
            createElement();

            //then
            expect(element.find('#preview').length).toBe(1);
        });
    });

    describe('left header', () => {
        it('should render dataset name and nb lines', () => {
            //given
            scope.dataset = {
                id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
                name: 'US States',
                records: '3'
            };
            scope.displayNbLines = true;

            //when
            createElement();

            //then
            expect(element.find('#playground-left-header').text().trim().replace(/[\s]+/g, ' ')).toBe('US States - 3 lines');
        });

        it('should render dataset name and cut lines number when dataset is truncated', () => {
            //given
            scope.dataset = {
                id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
                name: 'US States',
                records: '3',
                limit: 50
            };
            scope.displayNbLines = true;

            //when
            createElement();

            //then
            expect(element.find('#playground-left-header').text().trim().replace(/[\s]+/g, ' ')).toBe('US States - cut at 50 lines');
        });

        it('should not render dataset nb lines', () => {
            //given
            scope.dataset = {
                id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
                name: 'US States',
                records: '3',
                limit: 50
            };
            scope.displayNbLines = false;

            //when
            createElement();

            //then
            expect(element.find('#playground-left-header').text().trim()).toBe('US States');
        });

        it('should render insertion playground left header', () => {
            //when
            createElement();

            //then
            expect(element.find('#playground-left-header').eq(0)[0].hasAttribute('insertion-playground-left-header')).toBe(true);
        });

        it('should dataset parameters toggle button looks inactive by default', function () {
            //given
            createElement();

            //when
            let playgroundGearIcon = element.find('#playground-gear-icon');

            //then
            expect(playgroundGearIcon.hasClass('pressed')).toBe(false);
        });

        it('should dataset parameters toggle button looks active when its panel is shown', function () {
            //given
            scope.parametersVisible = true;
            createElement();

            //when
            let playgroundGearIcon = element.find('#playground-gear-icon');
            playgroundGearIcon.click();

            //then
            expect(playgroundGearIcon.hasClass('pressed')).toBe(true);
        });

        it('should call parameters callback', function () {
            //given
            scope.onParameters = jasmine.createSpy('onParameters');
            createElement();

            //when
            element.find('#playground-gear-icon').click();

            //then
            expect(scope.onParameters).toHaveBeenCalled();
        });
    });

    describe('right header', function() {
        it('should lookup toggle button looks inactive by default', function () {
            //given
            createElement();

            //when
            let playgroundLookupIcon = element.find('#playground-lookup-icon');

            //then
            expect(playgroundLookupIcon.hasClass('pressed')).toBe(false);
        });

        it('should lookup toggle button looks active when its panel is shown', function () {
            //given
            scope.lookupVisible = true;
            createElement();

            //when
            let playgroundLookupIcon = element.find('#playground-lookup-icon');
            playgroundLookupIcon.click();

            //then
            expect(playgroundLookupIcon.hasClass('pressed')).toBe(true);
        });

        it('should call lookup callback', function () {
            //given
            scope.onLookup = jasmine.createSpy('onLookup');
            createElement();

            //when
            element.find('#playground-lookup-icon').click();

            //then
            expect(scope.onLookup).toHaveBeenCalled();
        });

        it('should call onboarding callback', () => {
            //given
            scope.onOnboarding = jasmine.createSpy('onOnboarding');
            createElement();

            //when
            element.find('#playground-onboarding-icon').click();

            //then
            expect(scope.onOnboarding).toHaveBeenCalled();
        });

        it('should not render  feedback icon', () => {
            //when
            scope.feedbackVisible = false;
            createElement();

            //then
            expect(element.find('#playground-feedback-icon').length).toBe(0);
        });

        it('should call feedback callback', () => {
            //given
            scope.onFeedback = jasmine.createSpy('onFeedback');
            scope.feedbackVisible = true;
            createElement();

            //when
            element.find('#playground-feedback-icon').click();

            //then
            expect(scope.onFeedback).toHaveBeenCalled();
        });

        it('should call close callback', () => {
            //given
            scope.onClose = jasmine.createSpy('onClose');
            createElement();

            //when
            element.find('#playground-close').click();

            //then
            expect(scope.onClose).toHaveBeenCalled();
        });

        it('should render export', () => {
            //when
            createElement();

            //then
            expect(element.find('export').length).toBe(1);
        });

        it('should render history control', () => {
            //when
            createElement();

            //then
            expect(element.find('history-control').length).toBe(1);
        });

        it('should NOT render preview badge', () => {
            //given
            scope.preview = false;

            //when
            createElement();

            //then
            expect(element.find('#preview').length).toBe(0);
        });
    });
});