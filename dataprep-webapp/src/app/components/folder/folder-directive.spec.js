/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('folder directive', () => {
    'use strict';

    let scope, createElement, element, stateMock;

    beforeEach(angular.mock.module('data-prep.folder', ($provide) => {
        stateMock = {
            inventory: {
                foldersStack: [
                    { id: '', path: '', name: 'HOME_FOLDER' },
                    { id: '1', path: '1', name: '1' },
                    { id: '1/2', path: '1/2', name: '2' },
                ],
                menuChildren: [
                    {
                        'id': 'TDP-714',
                        'path': 'TDP-714',
                        'name': 'TDP-714',
                        'creationDate': 1448984715000,
                        'modificationDate': 1448984715000,
                    },
                    {
                        'id': 'lookups',
                        'path': 'lookups',
                        'name': 'lookups',
                        'creationDate': 1448895776000,
                        'modificationDate': 1448895776000,
                    },
                ],
            }
        };
        $provide.constant('state', stateMock);
    }));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            element = angular.element('<folder></folder>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render folders', () => {
        //when
        createElement();

        //then
        expect(element.find('.breadcrumb > ul > li').length).toBe(3);
        expect(element.find('.breadcrumb > ul > li > .folder-name').eq(0).text()).toBe('HOME_FOLDER');
        expect(element.find('.breadcrumb > ul > li > .folder-name').eq(1).text()).toBe('1');
        expect(element.find('.breadcrumb > ul > li > .folder-name').eq(2).text()).toBe('2');
        expect(element.find('sc-dropdown').length).toBe(3);
    });
});