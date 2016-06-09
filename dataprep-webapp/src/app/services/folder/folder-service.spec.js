/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder services', () => {
    'use strict';

    let stateMock;

    const sortList = [
        { id: 'name', name: 'NAME_SORT', property: 'name' },
        { id: 'date', name: 'DATE_SORT', property: 'created' },
    ];

    const orderList = [
        { id: 'asc', name: 'ASC_ORDER' },
        { id: 'desc', name: 'DESC_ORDER' },
    ];

    beforeEach(angular.mock.module('data-prep.services.folder', ($provide) => {
        stateMock = {
            inventory: {
                homeFolderId: 'L215L2ZvbGRlcg==',

                sortList: sortList,
                orderList: orderList,
                preparationsSort: sortList[0],
                preparationsOrder: orderList[0],
            },
        };
        $provide.constant('state', stateMock);
    }));

    describe('simple REST calls', () => {
        beforeEach(inject(($q, FolderRestService) => {
            spyOn(FolderRestService, 'create').and.returnValue($q.when());
            spyOn(FolderRestService, 'children').and.returnValue($q.when());
            spyOn(FolderRestService, 'rename').and.returnValue($q.when());
            spyOn(FolderRestService, 'remove').and.returnValue($q.when());
        }));
        
        it('should call rest children', inject((FolderService, FolderRestService) => {
            //given
            const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
            
            //when
            FolderService.children(folderId);

            //then
            expect(FolderRestService.children).toHaveBeenCalledWith(folderId);
        }));
        
        it('should call rest children with home folder by default', inject((FolderService, FolderRestService) => {
            //when
            FolderService.children();

            //then
            expect(FolderRestService.children).toHaveBeenCalledWith(stateMock.inventory.homeFolderId);
        }));

        it('should call rest create', inject((FolderService, FolderRestService) => {
            //given
            const folderName = 'azerty';
            const parentId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
            
            //when
            FolderService.create(parentId, folderName);

            //then
            expect(FolderRestService.create).toHaveBeenCalledWith(parentId, folderName);
        }));

        it('should call rest create with default folder', inject((FolderService, FolderRestService) => {
            //given
            const name = 'azerty';
            
            //when
            FolderService.create(undefined, name);

            //then
            expect(FolderRestService.create).toHaveBeenCalledWith(stateMock.inventory.homeFolderId, name);
        }));

        it('should call rest rename', inject((FolderService, FolderRestService) => {
            //given
            const newName = 'azerty';
            const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
            
            //when
            FolderService.rename(folderId, newName);

            //then
            expect(FolderRestService.rename).toHaveBeenCalledWith(folderId, newName);
        }));

        it('should call rest rename', inject((FolderService, FolderRestService) => {
            //given
            const newName = 'azerty';
            
            //when
            FolderService.rename(undefined, newName);

            //then
            expect(FolderRestService.rename).toHaveBeenCalledWith(stateMock.inventory.homeFolderId, newName);
        }));

        it('should call rest remove', inject((FolderService, FolderRestService) => {$
            //given
            const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
            
            //when
            FolderService.remove(folderId);

            //then
            expect(FolderRestService.remove).toHaveBeenCalledWith(folderId);
        }));
    });

    describe('init', () => {
        it('should set the preparation sort when there is a saved one', inject((StateService, StorageService, FolderService) => {
            // given
            spyOn(StorageService, 'getPreparationsSort').and.returnValue('date');
            spyOn(StateService, 'setPreparationsSort').and.returnValue();

            // when
            FolderService.init('/my/path');

            // then
            expect(StateService.setPreparationsSort).toHaveBeenCalledWith(sortList[1]);
        }));

        it('should NOT set the preparation sort when there is NO saved one', inject((StateService, StorageService, FolderService) => {
            // given
            spyOn(StorageService, 'getPreparationsSort').and.returnValue(null);
            spyOn(StateService, 'setPreparationsSort').and.returnValue();

            // when
            FolderService.init('/my/path');

            // then
            expect(StateService.setPreparationsSort).not.toHaveBeenCalled();
        }));

        it('should set the preparation order when there is a saved one', inject((StateService, StorageService, FolderService) => {
            // given
            spyOn(StorageService, 'getPreparationsOrder').and.returnValue('desc');
            spyOn(StateService, 'setPreparationsOrder').and.returnValue();

            // when
            FolderService.init('/my/path');

            // then
            expect(StateService.setPreparationsOrder).toHaveBeenCalledWith(orderList[1]);
        }));

        it('should NOT set the preparation order when there is NO saved one', inject((StateService, StorageService, FolderService) => {
            // given
            spyOn(StorageService, 'getPreparationsOrder').and.returnValue(null);
            spyOn(StateService, 'setPreparationsOrder').and.returnValue();

            // when
            FolderService.init('/my/path');

            // then
            expect(StateService.setPreparationsOrder).not.toHaveBeenCalled();
        }));

        it('should refresh folder content', inject(($q, FolderRestService, FolderService) => {
            // given
            const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
            spyOn(FolderRestService, 'getContent').and.returnValue($q.when());

            // when
            FolderService.init(folderId);

            // then
            expect(FolderRestService.getContent).toHaveBeenCalledWith(folderId, 'name', 'asc');
        }));

        it('should refresh folder content with home folder by default', inject(($q, FolderRestService, FolderService) => {
            // given
            spyOn(FolderRestService, 'getContent').and.returnValue($q.when());

            // when
            FolderService.init();

            // then
            expect(FolderRestService.getContent).toHaveBeenCalledWith(stateMock.inventory.homeFolderId, 'name', 'asc');
        }));
    });

    describe('refresh', () => {
        const folderMetadata = {
            folder: { id: 'L215L3BlcnNvbmFsL2ZvbGRlcg==' },
            hierarchy: [
                { id: 'L215' },
                { id: 'L215L3BlcnNvbmFs' },
            ],
        };
        const content = {
            folders: [{ path: 'toto', name: 'toto' }],
            preparations: [{ id: '5a8cf1763b58' }]
        };

        beforeEach(inject(($q, StateService, FolderRestService) => {
            spyOn(FolderRestService, 'getById').and.returnValue($q.when(folderMetadata));
            spyOn(FolderRestService, 'getContent').and.returnValue($q.when(content));
            spyOn(StateService, 'setFolder').and.returnValue();
            spyOn(StateService, 'setBreadcrumb').and.returnValue();
        }));

        it('should get content with sort and order', inject((FolderService, FolderRestService) => {
            // given
            const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
            stateMock.inventory.preparationsSort = sortList[1];
            stateMock.inventory.preparationsOrder = orderList[1];

            // when
            FolderService.refresh(folderId);

            // then
            expect(FolderRestService.getContent).toHaveBeenCalledWith(folderId, 'date', 'desc');
        }));

        it('should get folder metadata', inject((FolderService, FolderRestService) => {
            // given
            const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

            // when
            FolderService.refresh(folderId);

            // then
            expect(FolderRestService.getById).toHaveBeenCalledWith(folderId);
        }));

        it('should set the folder content in app state', inject(($rootScope, FolderService, StateService) => {
            // given
            const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
            stateMock.inventory.preparationsSort = sortList[1];
            stateMock.inventory.preparationsOrder = orderList[1];
        
            // when
            FolderService.refresh(folderId);
            $rootScope.$digest();
        
            // then
            expect(StateService.setFolder).toHaveBeenCalledWith(folderMetadata.folder, content);
        }));

        it('should get folder metadata', inject(($rootScope, StateService, FolderService) => {
            // given
            const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

            // when
            FolderService.refresh(folderId);
            $rootScope.$digest();

            // then
            expect(StateService.setBreadcrumb).toHaveBeenCalledWith(folderMetadata.hierarchy.concat(folderMetadata.folder));
        }));

        it('should get folder metadata with homeFolderId', inject((FolderService, FolderRestService) => {
            // given
            stateMock.inventory.homeFolderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

            // when
            FolderService.refresh();

            // then
            expect(FolderRestService.getById).toHaveBeenCalledWith('L215L3BlcnNvbmFsL2ZvbGRlcg==');
        }));
    });

    describe('refreshBreadcrumbChildren', () => {
        const children = [{ id: '5' }];
        
        beforeEach(inject(($q, FolderRestService, StateService) => {
            spyOn(FolderRestService, 'children').and.returnValue($q.when(children));
            spyOn(StateService, 'setBreadcrumbChildren').and.returnValue();
        }));
        
        it('should fetch folder children', inject((FolderService, FolderRestService) => {
            // given
            const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
            expect(FolderRestService.children).not.toHaveBeenCalled();

            // when
            FolderService.refreshBreadcrumbChildren(folderId);

            // then
            expect(FolderRestService.children).toHaveBeenCalledWith(folderId);
        }));

        it('should set folder children as breadcrumb item children in app state', inject(($rootScope, StateService, FolderService) => {
            // given
            const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
            expect(StateService.setBreadcrumbChildren).not.toHaveBeenCalledWith();
        
            // when
            FolderService.refreshBreadcrumbChildren(folderId);
            $rootScope.$digest();
        
            // then
            expect(StateService.setBreadcrumbChildren).toHaveBeenCalledWith(
                folderId,
                children
            );
        }));
    });
});
