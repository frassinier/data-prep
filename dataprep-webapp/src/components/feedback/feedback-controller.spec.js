describe('Feedback controller', function () {
    'use strict';

    var createController, scope, $stateMock;

    beforeEach(module('data-prep.feedback'));

    beforeEach(inject(function ($rootScope, $controller) {
        jasmine.clock().install();
        scope = $rootScope.$new();
        $stateMock = {};

        createController = function () {
            return $controller('FeedbackCtrl', {
                $scope: scope,
                $state: $stateMock
            });
        };
    }));

    afterEach(function() {
        jasmine.clock().uninstall();
    });

    describe('feedback ', function() {
        beforeEach(inject(function($q, FeedbackRestService, MessageService) {
            spyOn(FeedbackRestService, 'sendFeedback').and.returnValue($q.when(true));
            spyOn(MessageService, 'success').and.returnValue(false);

        }));

        it('should send feedback', inject(function (FeedbackRestService) {
            //given
            var feedback = {
                title : 'test',
                mail : 'test',
                severity : 'test',
                type : 'test',
                description: 'test'
            };
            var ctrl = createController();
            ctrl.feedbackForm = {$commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue()};
            ctrl.feedback = feedback;

            //given
            ctrl.sendFeedback();

            //then
            expect(FeedbackRestService.sendFeedback).toHaveBeenCalledWith(feedback);
        }));

        it('should manage sending flag', function () {
            //given
            var feedback = {
                title : 'test',
                mail : 'test',
                severity : 'test',
                type : 'test',
                description: 'test'
            };
            var ctrl = createController();
            ctrl.feedbackForm = {$commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue()};
            ctrl.feedback = feedback;
            ctrl.isSendingFeedback = false;

            //given
            ctrl.sendFeedback();
            expect(ctrl.isSendingFeedback).toBe(true);
            scope.$digest();

            //then
            expect(ctrl.isSendingFeedback).toBe(false);
        });

        it('should close feedback modal on send success', function () {
            //given
            var feedback = {
                title : 'test',
                mail : 'test',
                severity : 'test',
                type : 'test',
                description: 'test'
            };
            var ctrl = createController();
            ctrl.feedbackForm = {$commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue()};
            ctrl.feedback = feedback;
            ctrl.state.feedback.visible = true;

            //given
            ctrl.sendFeedback();
            scope.$digest();

            //then
            expect(ctrl.state.feedback.visible).toBe(false);
        });

        it('should show message on send success', inject(function (MessageService) {
            //given
            var feedback = {
                title : 'test',
                mail : 'test',
                severity : 'test',
                type : 'test',
                description: 'test'
            };
            var ctrl = createController();
            ctrl.feedbackForm = {$commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue()};
            ctrl.feedback = feedback;
            ctrl.state.feedback.visible = true;

            //given
            ctrl.sendFeedback();
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('FEEDBACK_SENT_TITLE', 'FEEDBACK_SENT_CONTENT');
        }));

        it('should reset form on send success', function () {
            //given
            var feedback = {
                title : 'test',
                mail : 'test',
                severity : 'test',
                type : 'test',
                description: 'test'
            };
            var ctrl = createController();
            ctrl.feedbackForm = {$commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue()};
            ctrl.feedback = feedback;
            ctrl.state.feedback.displayFeedback = true;

            //given
            ctrl.sendFeedback();
            scope.$digest();

            //then
            expect(ctrl.feedback).toEqual({
                title: '',
                mail: '',
                severity: 'MINOR',
                type: 'BUG',
                description: ''
            });
        });
    });
});
