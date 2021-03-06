/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataset upload list directive', function() {
    var scope, createElement;

    beforeEach(angular.mock.module('data-prep.dataset-upload-list'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'UPLOAD_PROCESSING':'Profiling data, please wait...'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function(directiveScope) {
            var element = angular.element('<dataset-upload-list datasets="datasets"></dataset-upload-list>');
            $compile(element)(directiveScope);
            directiveScope.$digest();
            return element;
        };
    }));

    it('should render progressing upload dataset', function() {
        //given
        scope.datasets = [
            {name: 'Customers (50 lines)', progress: 10, error: false, type: 'file'}
        ];

        //when
        var element = createElement(scope);
        var names = element.find('.inventory-title');
        var progress = element.find('.inventory-progress');

        //then
        expect(names.length).toBe(1);
        expect(names.eq(0).text()).toBe('Customers (50 lines)');
        expect(progress.eq(0).text().trim()).toBe('10 %');
        expect(progress.eq(0).hasClass('error')).toBe(false);
    });

    it('should show profiling data message once the upload reaches the 100%', function() {
        //given
        scope.datasets = [
            {name: 'Customers (50 lines)', progress: 100, error: false, type: 'file'}
        ];

        //when
        var element = createElement(scope);
        var progress = element.find('.inventory-progress');

        //then
        expect(progress.eq(0).text().trim()).toBe('Profiling data, please wait...');
    });

    it('should render progressing remote dataset import', function() {
        //given
        scope.datasets = [
            {name: 'remote 1', progress: 0, error: false, type: 'remote'}
        ];

        //when
        var element = createElement(scope);
        var names = element.find('.inventory-title');
        var progress = element.find('.inventory-progress');

        //then
        expect(names.length).toBe(1);
        expect(names.eq(0).text()).toBe('remote 1');
        expect(progress.eq(0).hasClass('error')).toBe(false);
    });

    it('should render upload error dataset', function() {
        //given
        scope.datasets = [
            {name: 'Customers (50 lines)', progress: 10, error: true}
        ];

        //when
        var element = createElement(scope);
        var names = element.find('.inventory-title');
        var progress = element.find('.inventory-progress');

        //then
        expect(names.length).toBe(1);
        expect(names.eq(0).text()).toBe('Customers (50 lines)');
        expect(progress.eq(0).text().trim()).not.toBe('10 %');
        expect(progress.eq(0).hasClass('error')).toBe(true);
    });

    it('should render multiple datasets', function() {
        //given
        scope.datasets = [
            {name: 'Customers (50 lines)', progress: 10, error: false, type: 'file'},
            {name: 'Us states', progress: 20, error: false, type: 'file'},
            {name: 'Customers (1K lines)', progress: 30, error: true, type: 'file'}
        ];

        //when
        var element = createElement(scope);
        var names = element.find('.inventory-title');
        var progress = element.find('.inventory-progress');

        //then
        expect(names.length).toBe(3);

        expect(names.eq(0).text()).toBe('Customers (50 lines)');
        expect(progress.eq(0).text().trim()).toBe('10 %');
        expect(progress.eq(0).hasClass('error')).toBe(false);

        expect(names.eq(1).text()).toBe('Us states');
        expect(progress.eq(1).text().trim()).toBe('20 %');
        expect(progress.eq(1).hasClass('error')).toBe(false);

        expect(names.eq(2).text()).toBe('Customers (1K lines)');
        expect(progress.eq(2).text().trim()).not.toBe('10 %');
        expect(progress.eq(2).hasClass('error')).toBe(true);
    });
});