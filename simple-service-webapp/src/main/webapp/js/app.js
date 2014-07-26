var filterApp = angular.module('filterApp', [ 'ngRoute', 'ngResource' ]);

filterApp.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/filters', {
		templateUrl : 'templates/main2.view.html',
		controller : 'MainController'
	}).when('/attributes', {
		templateUrl : 'templates/attributes.view.html',
		controller : 'AttributesController'
	}).when('/linked_filters', {
		templateUrl : 'templates/linked_filters.view.html',
		controller : 'AttributesController'
	}).when('/location', {
		templateUrl : 'templates/location.view.html',
		controller : 'AddLocationController'
	}).when('/location-details', {
		templateUrl : 'templates/location-view-edit.view.html',
		controller : 'UpdateLocationController'
	}).when('/', {
		templateUrl : 'templates/search.view.html',
		controller : 'SearchController'
	}).otherwise({
		redirectTo : '/'
	});
} ]);

filterApp.factory('PropertyGroups', [ '$resource', function($resource) {
	return $resource('api/v1/propertygroups/:id',{id:'@id'},
			{
			'update': { method:'PUT' }
			},
			{
			'query': {method: 'GET', isArray: false }
			}
			);
} ]);
//
filterApp.factory('PropertyGroupsConnection', [ '$resource', function($resource) {
	return $resource('api/v1/propertygroups/:id/connection',{id:'@id'},
			{
				'disconnect' : {method : 'DELETE'}
			}
			);
} ]);

filterApp.factory('PropertyConnection', [ '$resource', function($resource) {
	return $resource('api/v1/property/:id/connection/:pGroupId',{id:'@id', pGroupId:'@pGroupId'},
			{
				'connect' : {method : 'PUT'}
			}
			);
} ]);

filterApp.factory('Locations', [ '$resource', function($resource) {
	return $resource('api/v1/locations/:id',{id:'@id'},
			{
			'query': {method: 'GET', isArray: false }
			});
} ]);

filterApp.factory('Attributes', [ '$resource', function($resource) {
	return $resource('api/v1/property/:id', null,
			 {
				'update': { method:'PUT' }
			 });
} ]);
filterApp.factory('AttributesByGroup', [ '$resource', function($resource) {
	return $resource('api/v1/property/bygroup/:groupId', {groupId:'@id'});
} ]);

filterApp.controller('MainController', [ '$scope','$routeParams','PropertyGroups',
		function($scope, $routeParams, PropertyGroups) {
			$scope.dataLoading = true;
			$scope.name=null;
			$scope.type=null;
			$scope.propertyGroups = PropertyGroups.query(function (value, responseHeaders){
				$scope.dataLoading = false;
			},
			function (httpResponse) {
				$scope.dataLoading = false;
				alert("Проблем при зареждането на филтри!");
			}
			);
			$scope.getCss = function(ngModelController) {
				var resultCss = ngModelController.$valid ? "ng-valid" : (ngModelController.$pristine ? "ng-valid":"invalidControl");
				return resultCss;
			};
			$scope.getButtonCss = function(disabled) {
				var resultCss = disabled ? "saveButtonDisabled" : "saveButtonEnabled";
				return resultCss;
			};
			$scope.addPropertyGroup = function() {
				PropertyGroups.save({
					name : $scope.name,
					type : $scope.type
				}, function(data) {
					if (data.status) {
						window.location.hash = "/attributes";
					}
				});
			};
			
			$scope.updatePropertyGroup = function(index, document) {
				var group = $scope.propertyGroups[index];
				if (group == undefined) {
					return;
				}
				group.processing = true;
				PropertyGroups.update({id: group.id}, group, 
						function(value, responseHeaders) {
							group.processing = false;
						}, 
						function(httpResponse) {
							group.processing = false;
							alert("Грешка по време на актуализиране на филтър!");
							console.log(httpResponse);
						});				
			};

			$scope.deletePropertyGroup = function(index) {
				var group = $scope.propertyGroups[index];
				if (group == undefined) {
					return;
				}
				group.processing = true;
				PropertyGroups.remove({id: group.id}, 
						function(value, responseHeaders) {
							group.processing = false;
							$scope.propertyGroups.splice(index, 1);
						},
						function(httpResponse) {
							group.processing = false;
							if (httpResponse.status === 410) {
								alert("Не може да изтриете този филтър, тъй като той се използва в поне един обект.");
							} else {
								alert("Грешка по време на изтриване на филтър!");
								console.log(httpResponse);
							}
						}
				);
			};
		} ]);

filterApp.controller('AttributesController', [ '$scope', '$routeParams', 'Attributes','AttributesByGroup',
		'PropertyGroups','PropertyGroupsConnection', 'PropertyConnection', 
		function($scope, $routeParams, Attributes, AttributesByGroup, PropertyGroups, PropertyGroupsConnection, PropertyConnection) {
			$scope.dataLoading = true;
			$scope.propertyGroups = PropertyGroups.query(
					{excludeConnectionType : "CHILD"}, 
					function (value, responseHeaders){
						$scope.dataLoading = false;
				}, function(httpResponse) {
					$scope.dataLoading = false;
					alert("Грешка по време на зареждане на филтри!");
					console.log(httpResponse);
				});
			$scope.dataLoading = true;
			$scope.freePropertyGroups = PropertyGroups.query({includeConnectionType : "NOT_CONNECTED"}, function (value, responseHeaders){
				$scope.dataLoading = false;
				}, function(httpResponse) {
					$scope.dataLoading = false;
					alert("Грешка по време на зареждане на свободни филтри!");
					console.log(httpResponse);
				});
			$scope.attributes = [];
			$scope.myForm={};
			$scope.myForm.attrValue = null;
			$scope.getCss = function(ngModelController) {
				var resultCss = ngModelController.$valid ? "ng-valid" : (ngModelController.$pristine ? "ng-valid":"invalidControl");
				return resultCss;
			};
			$scope.getButtonCss = function(disabled) {
				var resultCss = disabled ? "saveButtonDisabled" : "saveButtonEnabled";
				return resultCss;
			};
			$scope.addProperty = function() {
				var data = {};
				$scope.addProcessing = true;
				data.propertyGroups = {};
				data.value = $scope.name.toString();
				data.propertyGroups.id = $scope.selectedFilter.id;
				Attributes.save(data, function(value, responseHeaders) {
					$scope.attributes.push(value);
					$scope.name = "";
					$scope.myFormNg2.name.$pristine = true;
					$scope.myFormNg2.name.$dirty = false;
					$scope.myFormNg2.name.$valid = true;
					$scope.addProcessing = false;
				},
				function(httpResponse) {
					$scope.addProcessing = false;
					alert("Грешка по време на добавяне на атрибут!");
					console.log(httpResponse);
				});
			};
			
			$scope.deleteProperty = function(index) {
				var attribute = $scope.attributes[index];
				if (attribute == undefined) {
					return;
				}
				attribute.processing = true;
				Attributes.remove({id: attribute.id}, 
						function(value, responseHeaders) {
							attribute.processing = false;
							$scope.attributes.splice(index, 1);
						},
						function(httpResponse) {
							attribute.processing = false;
							if (httpResponse.status === 410) {
								alert("Не може да изтриете този атрибут на филтър, тъй като той се използва в поне един обект.");
							} else if (httpResponse.status === 411) {
								alert("Не може да изтриете този атрибут, тъй като към него има свързан филтър.\n" +
												"Ако наистина желаете да изтриете този атрибут, моля първо премахнете връзката между тях от секция 'Свързани атрибути'." );
							} else {
							
								alert("Грешка по време на изтриване на атрибут!");
								console.log(httpResponse);
							}
						}
				);
			};
			$scope.myForm.updateProperty = function(index, document) {
				var attribute = $scope.attributes[index];
				if (attribute == undefined) {
					return;
				}
				attribute.processing = true;
				Attributes.update({id: attribute.id}, attribute, 
						function(value, responseHeaders) {
							attribute.processing = false;
						}, 
						function(httpResponse) {
							attribute.processing = false;
							alert("Грешка по време на актуализиране на атрибут!");
							console.log(httpResponse);
						});				
			};
			// Flow: 
			//    1. attrbutes loaded on selectedIndexChange. 
			//    2. Then Utils.createAttributesAndDraggableChildFilters displays them & attaches drag & drop events
			//    3. The drop event on a parent attribute (given filters attribute) calls $scope.disconnect
			//    4. Scope.disconnect() recompute the free filters (property group) and calls a custom "callback" function (for disconnect it is
			// "loadParentFilterAttributes('filterAttributes')", specified from the linked_filters.view.html
			//	
			$scope.loadParentFilterAttributes = function (uiControlId) {
				$scope.dataLoading = true;
				$scope.uiControl = document.getElementById(uiControlId);
				var selectedPropertyGroup = $scope.selectedFilter.id;
				$scope.attributes = AttributesByGroup.query({groupId:selectedPropertyGroup},
 						function(value, responseHeaders) {
							$scope.dataLoading = false;
							Utils.createAttributesAndDraggableChildFilters($scope, document);
						});
			};
			$scope.loadAttributes = function () {
				$scope.dataLoading = true;
				var selectedPropertyGroup = $scope.selectedFilter.id;
				$scope.attributes = AttributesByGroup.query({groupId:selectedPropertyGroup},
 						function(value, responseHeaders) {
							$scope.dataLoading = false;
						});
			};
			$scope.disconnect = function (filterToDisconnect, callback) {
				$scope.dataLoading = true;
				PropertyGroupsConnection.disconnect({id: filterToDisconnect}, 
						function(value, responseHeaders){
							console.log("Deleted. Reloading free groups...");
							$scope.freePropertyGroups = PropertyGroups.query({includeConnectionType : "NOT_CONNECTED"}, function (value, responseHeaders){
									$scope.dataLoading = false;
									console.log("Free Property groups reloaded ");
									if (callback) {
										callback();
									}
								}, function(httpResponse) {
									$scope.dataLoading = false;
									alert("Грешка по време на презареждане на свободни филтри!");
									console.log(httpResponse);
								});
						}, 
						function(httpResponse) {
							$scope.dataLoading = false;
							alert("Грешка по време на изтирване на връзка на атрибут!");
							console.log(httpResponse);
						});	
			};
			$scope.connect = function (child, parentAttribute) {
				$scope.dataLoading = true;
				var _child = child;
				var _parentAttribute = parentAttribute;
				PropertyConnection.connect({id: parentAttribute, pGroupId: child},
					function(value, responseHeaders){
						console.log("Connected child [" + _child + "] to [" + _parentAttribute + "]. Reloading....");
						$scope.freePropertyGroups = PropertyGroups.query({includeConnectionType : "NOT_CONNECTED"}, function (value, responseHeaders){
							$scope.dataLoading = false;
							console.log("Free property groups reloaded ");
							$scope.loadParentFilterAttributes('filterAttributes');
						}, function(httpResponse) {
							$scope.dataLoading = false;
							alert("Грешка по време на презареждане на свободни филтри!");
							console.log(httpResponse);
						});
				}, 
				function(httpResponse) {
					$scope.dataLoading = false;
					alert("Грешка по време на създаване на връзка на атрибут!");
					console.log(httpResponse);
				}
				);
			};
		} ]);

filterApp.controller('AddLocationController', [ '$scope', '$http', 'PropertyGroups',
		'Locations', function($scope, $http, PropertyGroups, Locations) {
			$scope.propertyGroups = PropertyGroups.query();
			$scope.s = "-1";
			$scope.onSave = function() {
				var data = {
					name : $scope.name,
					properties : []
				};
				for (var i = 0; i < $scope.propertyGroups.length; i++) {
					var propGroup = $scope.propertyGroups[i];
					for (var k = 0; k < propGroup.properties.length; k++) {
						var prop = propGroup.properties[k];
						if (!prop.realValue) {
							continue;
						}
						data.properties.push({
							id : prop.id,
							value : prop.realValue
						});
					}
				}
				Locations.save(data);
			};
			$scope.getCss = function(ngModelController) {
				var resultCss = ngModelController.$valid ? "ng-valid" : (ngModelController.$pristine ? "ng-valid":"invalidControl");
				return resultCss;
			};
			$scope.showHideChild = function(childPropertyGroupId, isChild, uiControlId) {
				
				Utils.showHideChildBase(childPropertyGroupId, isChild, uiControlId, $scope, $http, null, false, new Array());
			};
		} ]);

filterApp.controller('UpdateLocationController', [ '$scope', '$http', '$routeParams', 'PropertyGroups', 'Locations', 
                    function($scope, $http, $routeParams, PropertyGroups, Locations) {
	$scope.newLocation = {};
	$scope.getLocation = function (hash, callback) {
		if (!hash || hash.indexOf("?") == -1) {
			return; //no location selected
		}
		var questionMarkPos = hash.indexOf("?");
		var locationId = hash.substring(questionMarkPos + 4);
		if (!locationId) {
			return;
		}
		Locations.query({id:locationId}, 
				function(value, responseHeaders) {
					$scope.locationsLoading = false;
					Utils.currentLocation = value;
					callback();
				},
				function(httpResponse) {
					$scope.locationsLoading = false;
					if (httpResponse.status !== 404) {
						alert("Грешка по време на извличане на данни за обект!\n Моля свържете се със системния програмист.");
						console.log(httpResponse);
					}
				});
	};
	$scope.showHideChild = function(childPropertyGroupId, isChild, uiControlId, locationId, editMode, fields, disableFieldsIfinstructed) {
		
		Utils.showHideChildBase(childPropertyGroupId, isChild, uiControlId, $scope, $http, locationId, editMode, fields, disableFieldsIfinstructed);
	};
	$scope.s = "-1";

} ]);

filterApp.controller('SearchController', [ '$scope', '$http', '$routeParams', 'PropertyGroups', 'Locations',
		function($scope, $http, $routeParams, PropertyGroups, Locations) {
			$scope.dataLoading = true;
			$scope.childs = new Array();
			$scope.myModel = {};
			$scope.propertyGroups = PropertyGroups.query({excludeConnectionType : "CHILD"},
			function(value, responseHeaders) {
				$scope.dataLoading = false;
			}, 
			function(httpResponse) {
				$scope.dataLoading = false;
				alert("Проблем при зареждането на филтрите.");
				console.log(httpResponse);
			});
			$scope.testFunction = function () {
				alert("I'm a test function.");
			};
			
			$scope.showHideChild = function(k,uiControlId) {
				var childpropertyGroupsId = null;
				var isChild = k.childPropertyGroups != undefined;
				if (isChild) {
					childpropertyGroupsId = k.childPropertyGroups.id;
				}
				document.getElementById(uiControlId).value = k.value;
				Utils.showHideChildBase(childpropertyGroupsId, isChild, uiControlId, $scope, $http, null, false, new Array());
			};
			
			$scope.addChilds = function(propGroupId, propValue, childGroupId) {
				console.log("Called addChilds : " + propGroupId + ", " + propValue + ", " +  childGroupId);
				child.propGroupId = propGroupId;
				child.propValue = propValue;
				child.childGroupId = childGroupId;
				childs.push(child);
			};
			$scope.result=[];
			$scope.doSearch = function() {
				$scope.searching = true;
				$scope.result=[];
				$http({
					method : 'GET',
					data:'',
					url : 'api/v1/search?' + $scope.query,
					headers: {
				        "Content-Type": "application/json;charset=UTF-8"
				    }
				}).success(function(data, status, headers, config) {
					$scope.searching = false;
					$scope.result = data;
				}).error(function(data, status, headers, config) {
					$scope.searching = false;
					alert("Грешка по време на изпълнение на търсенето.");
					console.log('Error calling backend' + status);
				});
			};
			$scope.deleteLocation = function(index) {
				var location = $scope.result[index];
				if (location == undefined) {
					return;
				}
				location.processing = true;
				Locations.remove({id: location.id}, 
						function(value, responseHeaders) {
							location.processing = false;
							$scope.result.splice(index, 1);
						},
						function(httpResponse) {
							location.processing = false;
							alert("Грешка по време на изтриване на обект!");
							console.log(httpResponse);
						}
				);
			};
			$scope.openLocationDetails = function(index) {
				var location = $scope.result[index];
				if (location == undefined) {
					return;
				}
				//Utils.currentLocation = location;
				window.location.hash = "/location-details?id=" + location.id;
			};
		} ]);