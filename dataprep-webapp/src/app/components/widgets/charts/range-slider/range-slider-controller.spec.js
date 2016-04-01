/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('RangeSlider controller', function () {
    'use strict';

    var createController, scope;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrlFn = $controller('RangeSliderCtrl', {
                $scope: scope
            }, true);
            return ctrlFn();
        };
    }));


    it('should check if the maximum value of the filter to apply has reached to column maximum', inject(function () {
        //given
        var ctrl = createController();

        ctrl.rangeLimits = {
            min: -92,
            max: 98
        };

        //when
        var result = ctrl.isMaxReached({min: 2, max: 98});

        //then
        expect(result).toEqual({min: 2, max: 98, isMaxReached: true});
    }));

    it('should check if both of the entered min and max are numbers when entered value is invalid', inject(function () {
        //given
        var ctrl = createController();

        ctrl.minMaxModel = {
            minModel: '5 74',
            maxModel: '0'
        };

        //when
        var result = ctrl.areMinMaxNumbers();

        //then
        expect(result).toBe(false);
    }));

    it('should check if both of the entered min and max are numbers when entered value is valid', inject(function () {
        //given
        var ctrl = createController();

        ctrl.minMaxModel = {
            minModel: '574',
            maxModel: '0'
        };

        //when
        var result = ctrl.areMinMaxNumbers();

        //then
        expect(result).toBe(true);
    }));


    it('should transform number string to number', inject(function () {
        //given
        var ctrl = createController();

        //when
        var result = ctrl.toNumber(' 88');

        //then
        expect(result).toBe(88);
    }));

    it('should return null on invalid string', inject(function () {
        //given
        var ctrl = createController();

        //when
        var result = ctrl.toNumber('dqsfds10010');

        //then
        expect(result).toBe(null);
    }));

    it('should return true when string has comma', inject(function () {
        //given
        var ctrl = createController();

        //when
        var hasComma = ctrl.checkCommaExistence(',654');

        //then
        expect(hasComma).toBe(true);
    }));

    it('should return false when string has NO comma', inject(function () {
        //given
        var ctrl = createController();

        //when
        var hasComma = ctrl.checkCommaExistence('654');

        //then
        expect(hasComma).toBe(false);
    }));

    describe('decimal places', function() {
        it('should return 0 as decimal place when the input is not is the requested format', inject(function () {
            //given
            var ctrl = createController();

            //when
            var result = ctrl.decimalPlaces('azeazeaz');

            //then
            expect(result).toBe(0);
        }));

        it('should return the decimal place', inject(function () {
            //given
            var ctrl = createController();

            //when
            var result = ctrl.decimalPlaces('0.70');

            //then
            expect(result).toBe(2);
        }));

        it('should return the decimal place with scientific annonation ajustement', inject(function () {
            //given
            var ctrl = createController();

            //when
            var result = ctrl.decimalPlaces('0.70e-2');

            //then
            expect(result).toBe(4);
        }));
    });

    describe('range value adaptation', function () {

        it('should switch entered min and max to respect min < max', inject(function () {
            //given
            var ctrl = createController();
            var enteredMin = 50;
            var enteredMax = 20; // lower than enteredMin
            var minimum = 2;
            var maximum = 100;
            var nbDecimals = 2;

            //when
            var result = ctrl.adaptRangeValues(enteredMin, enteredMax, minimum, maximum, nbDecimals);

            //then
            expect(result).toEqual({min:20, max:50});
        }));

        it('should set enteredMin to the minimum if it is under minimum', inject(function () {
            //given
            var ctrl = createController();
            var enteredMin = -2;// lower than minimum
            var enteredMax = 50;
            var minimum = 2;
            var maximum = 100;
            var nbDecimals = 2;

            //when
            var result = ctrl.adaptRangeValues(enteredMin, enteredMax, minimum, maximum, nbDecimals);

            //then
            expect(result.min).toBe(2);
        }));

        it('should set enteredMin to the maximum if it is above', inject(function () {
            //given
            var ctrl = createController();
            var enteredMin = 105;// above maximum
            var enteredMax = 110;
            var minimum = 2;
            var maximum = 100;
            var nbDecimals = 2;

            //when
            var result = ctrl.adaptRangeValues(enteredMin, enteredMax, minimum, maximum, nbDecimals);

            //then
            expect(result.min).toBe(100);
        }));

        it('should set enteredMax to the minimum if it is under', inject(function () {
            //given
            var ctrl = createController();
            var enteredMin = -1;
            var enteredMax = 0; // under minimum
            var minimum = 2;
            var maximum = 100;
            var nbDecimals = 2;

            //when
            var result = ctrl.adaptRangeValues(enteredMin, enteredMax, minimum, maximum, nbDecimals);

            //then
            expect(result.max).toBe(2);
        }));

        it('should set enteredMax to the maximum if it is above', inject(function () {
            //given
            var ctrl = createController();
            var enteredMin = 20;
            var enteredMax = 200; // greater than maximum
            var minimum = 2;
            var maximum = 100;
            var nbDecimals = 2;

            //when
            var result = ctrl.adaptRangeValues(enteredMin, enteredMax, minimum, maximum, nbDecimals);

            //then
            expect(result.max).toBe(100);
        }));
    });
});