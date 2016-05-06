/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc component
 * @name talend.widget.component:TalendBadge
 * @usage
 <talend-badge
 ng-class="ngClass"
 removable="true|false"
 on-close="onClose()">
 </talend-badge>
 * @param {String} css Element CSS classes
 * @param {Boolean} removable If we provide a close button
 * @param {Function} onClose The callback that is triggered on badge close
 */
const TalendBadge = {
    bindings: {
        css: '@ngClass',
        removable: '<',
        onClose: '&'
    },
    templateUrl: 'app/components/widgets/badge/badge.html',
    transclude: true
};

export default TalendBadge;