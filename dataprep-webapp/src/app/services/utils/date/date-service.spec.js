/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Date service', function () {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.utils'));

    it('should transform timestamp to human readable date', inject(function (DateService) {
        // given
        const date = new Date(2016, 0, 1);

        // when
        const result = DateService.getFormattedDateFromTime(date.getTime());

        // then
        expect(result).toEqual('01/01/2016');
    }));

    it('should not transform empty timestamp to human readable date', inject(function (DateService) {
        // given
        const date = null;

        // when
        const result = DateService.getFormattedDateFromTime(date);

        // then
        expect(result).toBeNull();
    }));

    it('should transform human readable date to timestamp', inject(function (DateService) {
        // given
        const date = new Date(2016, 0, 1);

        // when
        const result = DateService.getTimeFromFormattedDate('01/01/2016');

        // then
        expect(result).toEqual(date.getTime());
    }));

    it('should not transform empty string to timestamp', inject(function (DateService) {
        // given
        const emptyFormattedDate = '';

        // when
        const result = DateService.getTimeFromFormattedDate(emptyFormattedDate);

        // then
        expect(result).toBeNull();
    }));

    it('should not transform null to timestamp', inject(function (DateService) {
        // given
        const nullFormattedDate = null;

        // when
        const result = DateService.getTimeFromFormattedDate(nullFormattedDate);

        // then
        expect(result).toBeNull();
    }));
});