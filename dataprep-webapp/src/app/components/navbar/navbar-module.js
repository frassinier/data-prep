/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import NavbarCtrl from './navbar-controller';
import Navbar from './navbar-directive';

(() => {
    'use strict';

    angular.module('data-prep.navbar',
        [
            'ui.router',
            'data-prep.services.dataset',
            'data-prep.services.feedback',
            'data-prep.services.onboarding',
            'data-prep.services.utils',
            'data-prep.inventory-search',
        ])
        .controller('NavbarCtrl', NavbarCtrl)
        .directive('navbar', Navbar);
})();
