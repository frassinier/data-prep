'use strict';

// Declare app level module which depends on views, and components
var app = angular.module('data-prep', []);

app.controller('RecordsCtrl', function($scope, $http) {
	
	$scope.loadFile = function() {
		$http.get('ranking').success(function(data) {
			$scope.columns = data.columns;
			$scope.records = data.records;
//			loadTableFeedbackStyles();
		});
	};
	$scope.loadFileJson = function() {
		$http.get('customers_100_full.json').success(function(data) {
			$scope.columns = data.columns;
			$scope.records = data.records;
//			loadTableFeedbackStyles();
		});
	};
	$scope.loadFileJson();
});

app.controller('ColumnCtrl', ['$scope', function($scope) {
	$scope.column.total = $scope.column.quality.valid + $scope.column.quality.empty + $scope.column.quality.invalid;
	
	$scope.column.quality.empty_percent = Math.ceil($scope.column.quality.empty * 100 / $scope.column.total);
	$scope.column.quality.invalid_percent = Math.ceil($scope.column.quality.invalid * 100 / $scope.column.total);
	
	$scope.column.quality.valid_percent = 100 - $scope.column.quality.empty_percent - $scope.column.quality.invalid_percent;
}]);

app.directive('datasetGrid', function() {
	return {
		restrict: 'E',
		templateUrl: 'partials/dataset-grid.html'
	};
});
app.directive('datasetColumn', function() {
	return {
		restrict: 'A',
		templateUrl: 'partials/dataset-column.html'
	};
});
app.directive('importLocalFile', function() {
	return {
		restrict: 'E',
		templateUrl: 'partials/import-local-file.html'
	};
});
