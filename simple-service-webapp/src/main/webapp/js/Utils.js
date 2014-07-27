Utils = {};
Utils.currentLocation={"default":"default"};
Utils.drawFields = null;
Utils.idCounter = {};

Utils.resetFields = function(fields) {
	//Use this
	//$(this).closest('form').find('select').each(function() {
	//    this.selectedIndex = 0;
	//});
	//The same for the option buttons
	//the same for type text
	if (fields) {
	}
};
/** @param data - all property groups, @param currentData - ?*/
Utils.draw = function(data, currentData, edit, fields, disableFieldsIfinstructed) {
	
	$("#id").val(currentData.id);
	$("#name").val(currentData.name);
	$("#description").val(currentData.description);
	$('#img').attr('src', 'api/v1/images?imageId=' + currentData.id);
	
	var dataCopy = data.slice(0);
	var dataWithValues = Utils.digestData(dataCopy, currentData);
	if (!dataWithValues) {
		alert("Възникна проблем при редактиране на обект.\n Моля свържете се със системен програмист.");
	}
	var newContent = "";
	Utils.idCounter = { val : 0};
	if (dataWithValues) {
		newContent = Utils.prepareContent(dataWithValues, edit, Utils.idCounter, fields, null, null, disableFieldsIfinstructed);
	}
	if (dataCopy) {
		newContent += Utils.prepareContent(dataCopy, edit, Utils.idCounter, fields, null, null, disableFieldsIfinstructed);
	}
	return newContent;
};

Utils.prepareContent = function(data, edit, idCounter, fields, skipFormat, scope, disableFieldsIfinstructed) {
	var result = "";
	
	for (var i = 0; i < data.length; i++) {
		var e = data[i];
		var c = "";
		if (skipFormat) {
			c = e.name + ":<br>";
		} else {
			c = "<label>" + e.name + "</label>:<br>";			
		}
		if (e.type == "dropdown") {
			var id = "id" + (idCounter.val++);
			c += "<select name='" + e.id + "' id='" + id +"'";
			if (disableFieldsIfinstructed && edit) {
				c+=" disabled='disabled'";
			}
			c +="onchange=callshowHideChild(this)";
			c += " >";
			if (e.properties) {
				c += "<option value=''/>";
				for (var j = 0; j < e.properties.length; j++) {
					var value = e.properties[j].value;
					c += "<option value='" + e.properties[j].value + "'";
					c += " id = 'metadata: parentPropertyGroupsId=" + e.id + ";";
					if (e.properties[j].childPropertyGroups) {
						c += "isChildPropertyGroups=true; childPropertyGroupsId=" +
							e.properties[j].childPropertyGroups.id + ";";
					}
					c +="'";//close the ID attribute
					if (edit && e.locationPropertyValues && e.locationPropertyValues.indexOf(value) != -1) {
						c += " selected ";
					}
					c +=">" + e.properties[j].value + "</option>";
				}
			}
			c += "</select>";
			
		var childDivPlaceholder="<div id='childDiv" + e.id + "' class='childDiv' style='display:none'></div>";
			c +=childDivPlaceholder;
			fields.push(id);
		} else if (e.type == "radiobutton") {
			if (e.properties) {
				for (var j = 0; j < e.properties.length; j++) {
					var value = e.properties[j].value;
					var id = "id" + (idCounter.val++);
					c += "<input type='radio' name='" + e.id + "' id='" + id + "'";
					if (disableFieldsIfinstructed && edit) {
						c+= " disabled='disabled'";
					}
					c+= " value='" + value + "'";
					if (edit && e.locationPropertyValues &&  e.locationPropertyValues.indexOf(value) != -1) {
						c += " checked ";
					}
					c += ">"+ value + "&nbsp;";
					fields.push(id);
				}
			}
		} else if (e.type == "checkbox") {
			if (e.properties) {
				for (var j = 0; j < e.properties.length; j++) {
					var value = e.properties[j].value;
					var id = "id" + (idCounter.val++);
					c += "<input type='checkbox' name='" + e.id +  "' id='" + id + "'"; 
					if (disableFieldsIfinstructed && edit) { 
						c+= " disabled='disabled'";
					}
					c+= " value='" + value + "'";
					if (edit && e.locationPropertyValues &&  e.locationPropertyValues.indexOf(value) != -1) {
						c += " checked ";
					}
					c += ">" + value + "&nbsp;";
					fields.push(id);
				}
				var childDivPlaceholder="";
				childDivPlaceholder="<div id='childDiv" + e.id + "' class='childDiv' style='display:none'></div>";
				if (edit) {
				} else {
				}
				
				c +=childDivPlaceholder;
			}
		} else if (e.type == "simpletext") {
			var id = "id" + (idCounter.val++);
			c += "<input type='text' name='" + e.id + "' id='" + id + "' ";
			if (disableFieldsIfinstructed && edit) {
				c+= "disabled='disabled' ";
			}
			c +=" value='";
			if (edit && e.locationPropertyValues && e.locationPropertyValues.length > 0) {
				//value taken if current data value is assigned
				c += e.locationPropertyValues[0];
			} else {
				c += "";
			}
			c += "'>";
			fields.push(id);
		}
		result = result + c + "<br>";
	}
	return result;
};

Utils.digestData = function(data, currentData) {
//	console.log("Digest input (data, currentData):");
//	console.log(data);
//	console.log(currentData);
	var iterator = data.slice(0);
	var result = new Array();
	var resultWithoutValues = new Array();
	
	for (var i=0; i < iterator.length; i++) {
		var propertyGroup = iterator[i];
		var locationPropGroupId = propertyGroup.id;
		var arrayOfValues = new Array();
		
		for (var j = 0; j < currentData.locationProperties.length; j++) {
			var locationProp = currentData.locationProperties[j];
			if (locationProp.group.id == locationPropGroupId) {
				arrayOfValues.push(locationProp.value);
			};
		}
		var resultPGroup = data.slice(i,(i+1))[0];
		if (arrayOfValues.length > 0) {
			if (resultPGroup.type =="simpletext" && arrayOfValues.length > 1) {
				console.log("PropertyGroup [" + resultPGroup.name + "] of simpletext has more than one values assigned for [" + currentData.name + "]");
				return null;
			}
			resultPGroup.locationPropertyValues = arrayOfValues;
			result.push(resultPGroup);
		} else {
			resultWithoutValues.push(resultPGroup);
		};
	}
	
	//remove all elements from data, which exist in the result
	for (var k = 0; k < result.length; k++) {
		var id = result[k].id;
		for (var l = 0; l < data.length; l++) {
			if (data[l].id == id) {
				data.splice(l,1);
				break;
			}
		}
	}
//	console.log("PropertyGroup with values and PropertyGroup wihout values after digest:");
//	console.log(result);
//	console.log(data);
	
	return result;
};

Utils.DragDropInfo = {};

Utils.DropHandler = function(event) {
	console.log(Utils.DragDropInfo.dragFrom);
	console.log("dropped ==========" + Utils.DragDropInfo.dragFrom + "," + event.target.id);
	Utils.DragDropInfo.dragFrom  = null;
};

Utils.DumpInfo = function (event) {
    var firedOn = event.target ? event.target : event.srcElement;
    if (firedOn.tagName === undefined) {
        firedOn = firedOn.parentNode;
    }

    var info = document.getElementById ("info");
//    if (firedOn.id == "source") {
//        info.innerHTML += "<span style='color:#008000'>" + event.type + "</span>, ";
//    }
//    else {
      info.innerHTML += "<span style='color:#800000'>" + event.type + ": " + firedOn.id + "</span>,<br/> ";
//    }
        
    if ("dragleave" == event.type) { 
    	//console.log("drag starts from " + event.target.id);
    	if (event.target && !Utils.DragDropInfo.dragFrom) {
    		Utils.DragDropInfo.dragFrom=event.target.id;
    	}
    }

    if (event.type == "dragover") {
            // the dragover event needs to be canceled to allow firing the drop event
    	//console.log("prevent dragover");
        if (event.preventDefault) {
            event.preventDefault ();
        }
        if (event.defaultPrevented) {
        	event.defaultPrevented=true;
        }
    }

	    
    if (event.type == "dragenter") {
		// the dragover event needs to be canceled to allow firing the drop
		// event
//		console.log("prevent drag enter");
		if (event.preventDefault) {
			event.preventDefault();
		}
		 if (event.defaultPrevented) {
	        	event.defaultPrevented=true;
	        }
	}
    
};

Utils.drop = function(ev) {
    ev.preventDefault();
    var source = ev.dataTransfer.getData("Text");
    var target = ev.target.id;
    var result = [source,target];
    console.log("Drag & drop :" + result);
    
    return result;
};

Utils.dropGetSource = function(ev) {
    ev.preventDefault();
    var source = ev.dataTransfer.getData("Text");
    var result = source;
    console.log("Drag & drop source:" + result);
    return result;
};

Utils.dropGetTarget = function(ev) {
    ev.preventDefault();
    var target = ev.target.id;
    var result = target;
    console.log("Drag & drop target:" + result);
    return result;
};

Utils.allowDrop = function(ev) {
    ev.preventDefault();
};

Utils.drag = function(ev) {
	console.log("drag: " + ev.target.id);
	ev.dataTransfer.setData("Text", ev.target.id);
};

Utils.dropAndDisconnectFilters = function(event, expectedTarget, scope, callback) {
	
	var r = Utils.drop(event);
	var source = r[0];
	var target = r[1];
	
	if (source == target) {
		return;
	}
	if (target.indexOf( expectedTarget) < 0) {
		//the target is not a free filter section
		return;
	}
	if (source.indexOf(expectedTarget) >= 0) {
		//the source is a free filter. We cannot disconnect free filters
		return;
	}
	
	//The source is not a free filter and the target is the free filter section
	scope.disconnect(r[0], callback);
};

Utils.dropAndConnectFilters = function(event, scope) {
	var r = Utils.drop(event);
	var sourceId = r[0];
	if (sourceId.indexOf('free') <0) {
		alert("Не може да свържете филтри, които не са свободни. Опитайте с друг, несвързан филтър, или изтрийте връзката между двата филтъра.");
		return;
	}
	var child = sourceId.substring(sourceId.indexOf('_')+1, r[0].length);
	var parentAttribute= r[1];
	
	console.log("Connecting child filter : " + child + " to parent filter value: "  + parentAttribute);
	scope.connect(child, parentAttribute);
};

Utils.createAttributesAndDraggableChildFilters = function(scope, document) {
	var attributes = scope.attributes;
	var uiControl = scope.uiControl;
	
	uiControl.innerHTML="";
	for (var i = 0; i < attributes.length; i++) {
		var attribute =  attributes[i];
		var content = document.createElement('div');
		content.id=attribute.id;
		content.ondrop=function(event, attributes) { Utils.dropAndConnectFilters(event, scope);};
		content.ondragover=Utils.allowDrop;
		content.innerHTML=attribute.value;
		content.className="attributeValue";
		content.draggable = false;
		
		var childDiv = null;
		if (attribute.childPropertyGroups) {
			childDiv = document.createElement('div');
			childDiv.id = attribute.childPropertyGroups.id;
			childDiv.innerHTML = attribute.childPropertyGroups.name;
			childDiv.className="childDiv2";
			//TODO prevent drag on this child element!
			childDiv.ondragstart=Utils.drag;
			content.appendChild(childDiv);
			
			var tooltip = document.createElement("div");
			tooltip.id = 'tooltipfreeFilter_' +  attribute.childPropertyGroups.id;
			tooltip.style.display = 'none';
			tooltip.className='tooltipfreeFilter';
			content.appendChild(tooltip);
		} else {
			
		}
		
		uiControl.appendChild(content);
		if (childDiv) {
			document.getElementById(childDiv.id).draggable = true;
		}
	}
};

Utils.showHideChildBase = function(childPropertyGroupId, isChild, uiControlId, scope, http, locationId, editMode, fields, disableFieldsIfinstructed) {
	console.log("show child called : " + childPropertyGroupId + ", " + isChild + ", " + uiControlId);
	var childDiv = document.getElementById("childDiv" + uiControlId);
	var getPropertyGroupURL = 'api/v1/propertygroups/' + childPropertyGroupId;
	if (locationId) {
		getPropertyGroupURL +="?locationId=" + locationId;
	}
	if (isChild) {
		this.searching = true;
		http({
			method : 'GET',
			data:'',
			url : getPropertyGroupURL,
			headers: {
		        "Content-Type": "application/json;charset=UTF-8"
		    }
		}).success(function(data, status, headers, config) {
			scope.searching = false;
			
			$("#childDiv" + uiControlId).hide();
			//Todo MIGHT NEED TO ACCEPT FILEDS FROM OUTSIDE
			var content =  Utils.prepareContent([data], editMode, Utils.idCounter, fields, true, disableFieldsIfinstructed);
			childDiv.innerHTML = content;
			$(childDiv).fadeIn(500);
			
		}).error(function(data, status, headers, config) {
			scope.searching = false;
			alert("Грешка по време на зареждане на свързан филтър [" + childPropertyGroupId + "] .");
			console.log('Error calling backend' + status);
		});
	} else {
		$(childDiv).fadeOut(200, function () {childDiv.innerHTML = "";});
	}
};

Utils.extractValue = function(data, key) {
	var keyLocation=data.indexOf(key); 
	if (keyLocation == -1) {
		return null;
	}
	var start = keyLocation + key.length + "=".length;
	var end = data.indexOf(";", keyLocation);
	return data.substring(start, end);
};

Utils.Metadata = function(rawData) {
	
	this.childPropertyGroupId = null;
	this.parentPropertyGroupsId = null;
	this.isMetadata = false;
	
	if (rawData && rawData.indexOf("metadata") >=0) {
		this.isMetadata = true;
	}
	
	try {
		if (this.isMetadata) {
			this.childPropertyGroupId = Utils.extractValue(rawData, "childPropertyGroupsId");
			this.parentPropertyGroupsId = Utils.extractValue(rawData, "parentPropertyGroupsId");
		}
	} catch (err) {
		alert("Системен проблем. Моля свържете се с програмист и му продиктувайте това съобщение:\n" +
				"Проблем при обработката на метаданни [" + rawData + "].\n " + err);
	}
	
	this.isChild = function() {
		var isChild = false;
		if (this.isMetadata && this.childPropertyGroupId) {
			isChild = true;
		}
		return isChild;
	};
};
Utils.getErrorCode = function (response) {
	var errorCode = Utils.extractValue(response, "errorCode");
	return errorCode;
};
