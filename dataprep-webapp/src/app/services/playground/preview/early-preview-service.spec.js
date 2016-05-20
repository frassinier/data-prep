describe('Early Preview Service', () => {
    'use strict';

    var stateMock;
    var dataset = {id: '123456'};
    var preparation = {id: '456789'};
    var column = {id: '0001', name: 'firstname'};
    var line = {tdpId: 125};
    var transfoScope;
    var transformation;
    var params;

    beforeEach(angular.mock.module('data-prep.services.playground', ($provide) => {
        stateMock = {
            playground: {
                dataset: dataset,
                preparation: preparation,
                grid: {}
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, PlaygroundService, PreviewService, RecipeService, EarlyPreviewService) => {
        stateMock.playground.grid.selectedColumns[0] = column;
        stateMock.playground.grid.selectedLine = line;

        transfoScope = 'column';
        transformation = {
            name: 'replace_on_value'
        };
        params = {
            value: 'James',
            replace: 'Jimmy'
        };

        spyOn(RecipeService, 'earlyPreview').and.returnValue();
        spyOn(RecipeService, 'cancelEarlyPreview').and.returnValue();
        spyOn(PreviewService, 'getPreviewAddRecords').and.returnValue($q.when());
        spyOn(PreviewService, 'cancelPreview').and.returnValue();
        spyOn(EarlyPreviewService, 'cancelPendingPreview').and.returnValue();
    }));

    it('should trigger preview without preparation id after a 1s delay', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService) => {
        //when
        stateMock.playground.preparation = null;
        EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
        expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
        $timeout.flush(700);

        //then
        expect(RecipeService.earlyPreview).toHaveBeenCalledWith(
            transformation,
            {
                value: 'James',
                replace: 'Jimmy',
                scope: transfoScope,
                column_id: column.id,
                column_name: column.name,
                row_id: 125
            }
        );
        expect(PreviewService.getPreviewAddRecords).toHaveBeenCalledWith(
            null,
            dataset.id,
            'replace_on_value',
            {
                value: 'James',
                replace: 'Jimmy',
                scope: transfoScope,
                column_id: column.id,
                column_name: column.name,
                row_id: 125
            }
        );
    }));

    it('should trigger preview with preparation id after a 1s delay', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService) => {
        //when
        EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
        expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
        $timeout.flush(700);

        //then
        expect(RecipeService.earlyPreview).toHaveBeenCalledWith(
            transformation,
            {
                value: 'James',
                replace: 'Jimmy',
                scope: transfoScope,
                column_id: column.id,
                column_name: column.name,
                row_id: 125
            }
        );
        expect(PreviewService.getPreviewAddRecords).toHaveBeenCalledWith(
            preparation.id,
            dataset.id,
            'replace_on_value',
            {
                value: 'James',
                replace: 'Jimmy',
                scope: transfoScope,
                column_id: column.id,
                column_name: column.name,
                row_id: 125
            }
        );
    }));

    it('should cancel pending early preview', inject(($timeout, RecipeService, PreviewService, EarlyPreviewService) => {
        //given
        EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
        expect(RecipeService.earlyPreview).not.toHaveBeenCalled();
        expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();

        //when
        EarlyPreviewService.cancelEarlyPreview();
        $timeout.flush();

        //then
        expect(RecipeService.earlyPreview).not.toHaveBeenCalled();
        expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
    }));

    it('should cancel current early preview after a 100ms delay', inject(($timeout, RecipeService, EarlyPreviewService, PreviewService) => {
        //when
        EarlyPreviewService.cancelEarlyPreview();
        expect(RecipeService.cancelEarlyPreview).not.toHaveBeenCalled();
        expect(PreviewService.cancelPreview).not.toHaveBeenCalled();
        $timeout.flush(100);

        //then
        expect(RecipeService.cancelEarlyPreview).toHaveBeenCalled();
        expect(PreviewService.cancelPreview).toHaveBeenCalled();
    }));

    it('should NOT trigger preview when it is disabled', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService) => {
        //given
        EarlyPreviewService.deactivatePreview();

        //when
        EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
        $timeout.flush(700);

        //then
        expect(RecipeService.earlyPreview).not.toHaveBeenCalled();
        expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();
    }));

    it('should NOT cancel current early preview when it is disabled', inject(($timeout, RecipeService, EarlyPreviewService, PreviewService) => {
        //given
        EarlyPreviewService.deactivatePreview();

        //when
        EarlyPreviewService.cancelEarlyPreview();
        $timeout.flush(100);

        //then
        expect(RecipeService.cancelEarlyPreview).not.toHaveBeenCalled();
        expect(PreviewService.cancelPreview).not.toHaveBeenCalled();
    }));

    it('should trigger preview when it is enabled', inject(($timeout, PreviewService, EarlyPreviewService, RecipeService) => {
        //given
        EarlyPreviewService.deactivatePreview();
        EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
        $timeout.flush(700);
        expect(PreviewService.getPreviewAddRecords).not.toHaveBeenCalled();

        //when
        EarlyPreviewService.activatePreview();
        EarlyPreviewService.earlyPreview(transformation, transfoScope)(params);
        $timeout.flush(700);

        //then
        expect(RecipeService.earlyPreview).toHaveBeenCalledWith(
            transformation,
            {
                value: 'James',
                replace: 'Jimmy',
                scope: transfoScope,
                column_id: column.id,
                column_name: column.name,
                row_id: 125
            }
        );
        expect(PreviewService.getPreviewAddRecords).toHaveBeenCalledWith(
            preparation.id,
            dataset.id,
            'replace_on_value',
            {
                value: 'James',
                replace: 'Jimmy',
                scope: transfoScope,
                column_id: column.id,
                column_name: column.name,
                row_id: 125
            }
        );
    }));
});