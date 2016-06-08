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


    it('should check if the maximum value of the filter to apply has reached to column maximum', function () {
        //given
        var ctrl = createController();

        ctrl.rangeLimits = {
            min: -92,
            max: 98
        };

        //when
        var result = ctrl.adaptFilterInterval({min: 2, max: 98});

        //then
        expect(result).toEqual({min: 2, max: 98, isMaxReached: true});
    });

    it('should check if both of the entered min and max are numbers when entered value is invalid', function () {
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
    });

    it('should check if both of the entered min and max are numbers when entered value is valid', function () {
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
    });

    it('should check if both of the entered min and max are dates when entered value is invalid', () => {
        //given
        const ctrl = createController();

        ctrl.minMaxModel = {
            minModel: 'abc',
            maxModel: (new Date(2010, 1, 5)).getTime()
        };

        //when
        const result = ctrl.areMinMaxDates();

        //then
        expect(result).toBeFalsy();
    });



    it('should check if both of the entered min and max are dates when entered value is valid', () => {
        //given
        const ctrl = createController();

        ctrl.minMaxModel = {
            minModel: (new Date(2010, 1, 1)).getTime(),
            maxModel: (new Date(2010, 1, 5)).getTime()
        };

        //when
        const result = ctrl.areMinMaxDates();

        //then
        expect(result).toBeTruthy();
    });

    it('should transform number string to number', function () {
        //given
        var ctrl = createController();

        //when
        var result = ctrl.toNumber(' 88');

        //then
        expect(result).toBe(88);
    });

    it('should return null on invalid string', function () {
        //given
        var ctrl = createController();

        //when
        var result = ctrl.toNumber('dqsfds10010');

        //then
        expect(result).toBe(null);
    });

    it('should return null on invalid date string', () => {
        //given
        const ctrl = createController();

        //when
        const result = ctrl.toDate('01/012010');

        //then
        expect(result).toBeNull();
    });

    it('should return Date instance on valid date string', () => {
        //given
        const ctrl = createController();

        //when
        const result = ctrl.toDate('01/01/2010');

        //then
        expect(result).toEqual(new Date('01/01/2010'));
        expect(isNaN(result.getTime())).toBeFalsy();
    });

    it('should convert timestamp with hours, minutes to midnight', () => {
        //given
        const ctrl = createController();
        const timeStamp = 1465844414000; //Mon Jun 13 2016 21:00:14

        //when
        const result = ctrl.setDateTimeToMidnight(timeStamp);

        //then
        expect(result).toBe(1465768800000);//Mon Jun 13 2016 00:00:00
    });

    it('should return true when string has comma', function () {
        //given
        var ctrl = createController();

        //when
        var hasComma = ctrl.checkCommaExistence(',654');

        //then
        expect(hasComma).toBe(true);
    });

    it('should return false when string has NO comma', function () {
        //given
        var ctrl = createController();

        //when
        var hasComma = ctrl.checkCommaExistence('654');

        //then
        expect(hasComma).toBe(false);
    });

    describe('decimal places', function() {
        it('should return 0 as decimal place when the input is not is the requested format', function () {
            //given
            var ctrl = createController();

            //when
            var result = ctrl.decimalPlaces('azeazeaz');

            //then
            expect(result).toBe(0);
        });

        it('should return the decimal place', function () {
            //given
            var ctrl = createController();

            //when
            var result = ctrl.decimalPlaces('0.70');

            //then
            expect(result).toBe(2);
        });

        it('should return the decimal place with scientific annonation ajustement', function () {
            //given
            var ctrl = createController();

            //when
            var result = ctrl.decimalPlaces('0.70e-2');

            //then
            expect(result).toBe(4);
        });
    });

    describe('range value adaptation', function () {

        it('should switch entered min and max to respect min < max', function () {
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
        });

        it('should set enteredMin to the minimum if it is under minimum', function () {
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
        });

        it('should set enteredMin to the maximum if it is above', function () {
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
        });

        it('should set enteredMax to the minimum if it is under', function () {
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
        });

        it('should set enteredMax to the maximum if it is above', function () {
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
        });
    });
});