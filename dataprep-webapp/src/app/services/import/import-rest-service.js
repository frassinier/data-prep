/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const LOCATION_TYPE_REMOTE_JOB = 'job';

/**
 * @ngdoc service
 * @name data-prep.services.import.service:ImportRestService
 * @description Import service. This service provide the entry point to the backend import REST api.<br/>
 */
export default class ImportRestService {
    constructor($http, RestURLs) {
        'ngInject';
        this.$http = $http;
        this.url = RestURLs.datasetUrl;
    }

    /**
     * @ngdoc method
     * @name importTypes
     * @methodOf data-prep.services.import.service:ImportRestService
     * @description Fetch the available import types
     * @returns {Promise}  The GET call promise
     */
    importTypes() {
        return this.$http.get(this.url + '/imports');
    }

    /**
     * @ngdoc method
     * @name importParameters
     * @methodOf data-prep.services.import.service:ImportRestService
     * @description Fetch the available import parameters
     * @returns {Promise}  The GET call promise
     */
    importParameters(locationType) {
        return this.$http.get(this.url + '/imports/'+ locationType + '/parameters' );
    }

    /**
     * @ngdoc method
     * @name importRemoteJobParameters
     * @methodOf data-prep.services.import.service:ImportRestService
     * @description Fetch the available remote job import parameters
     * @returns {Promise}  The GET call promise
     */
    importRemoteJobParameters() {
        return this.importParameters(LOCATION_TYPE_REMOTE_JOB);
    }
}