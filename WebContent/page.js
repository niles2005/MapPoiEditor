$(document).ready(function () {
    var datas;
    var currentType;
    var currentPoi;
    var currentMarker;
    var poiMarkerStore = {};
    var jConfirmModel = $('#confirmModal');

    var mapObj = new qq.maps.Map(document.getElementById("mapPanel"), {
        center: new qq.maps.LatLng(31.218914, 121.425362),
        mapTypeControlOptions: {
            position: qq.maps.ControlPosition.BOTTOM_RIGHT    //设置地图控件位置靠近顶部
        },
        draggingCursor: "pointer",
        draggableCursor: "crosshair",
        zoom: 17
    });


    qq.maps.event.addListener(
        mapObj,
        'click',
        function (event) {
            if ($("#clickCreatePoi").get(0).checked) {
                createPoi(event.latLng);
            }
        }
    );

    function createPoi(latLng) {
        $.getJSON("service?name=createpoi&typekey=" + currentType.key + "&v=" + new Date().getTime(), function (ret) {
            if (ret.retCode >= 0) {
                var poi = ret.data;
                if (poi) {
                    poi.latitude = latLng.getLat().toFixed(6);
                    poi.longitude = latLng.getLng().toFixed(6);
                    poi._create = true;
                    buildPoi(poi);
                }
            }
        });

    }

    var anchor = new qq.maps.Point(11, 33);
    var size = new qq.maps.Size(24, 34);
    var origin = new qq.maps.Point(0, 0);
    var defaultMarkerIcon = new qq.maps.MarkerImage("images/marker.png",
        size,
        origin,
        anchor
    );

    var newMarkerIcon = new qq.maps.MarkerImage("images/marker1.png",
        size,
        origin,
        anchor
    );


    $.getJSON("service?name=datas&v=" + new Date().getTime(), function (ret) {
        if (ret.retCode >= 0 && ret.data) {
            datas = ret.data;
            if (!datas.types) {
                datas.types = [];
            }
            buildTypes();
        }
    });

    function buildTypes() {
        let types = datas.types;
        if (types.length == 0) {
            return;
        }
        $("#typeNav").empty();
        for (let i = 0; i < types.length; i++) {
            let poiType = types[i];
            if (!poiType.key) {
                continue;
            }
            if (!poiType.pois) {
                poiType.pois = {};
            }
            let ss = '<label id="' + poiType.key + '" class="btn btn-sm btn-primary">' +
                '<input type="radio" name="options" id="option1" autocomplete="off" checked>' +
                poiType.name +
                '</label>';
            let jTypeItem = $(ss);
            $("#typeNav").append(jTypeItem);
            jTypeItem.click(function () {
                selectType(poiType);
            });
            if (currentType == poiType) {
                jTypeItem.addClass("active");
            }
            selectDefaultType();
        }
    }

    function selectDefaultType() {
        //currentType为空或者已经删除
        if (!currentType || datas.types.indexOf(currentType) < 0) {
            if (datas.types.length > 0) {
                $("#" + datas.types[0].key).addClass("active");
                selectType(datas.types[0]);
            }
        }
    }


    function selectType(theType) {
        if (currentType !== theType) {
            clearCurrentTypeMarkers();
            currentType = theType;
            if (currentType.pois) {
                for (let poi of currentType.pois) {
                    buildPoi(poi)
                }
            }
        }
    }

    function buildPoi(poi) {
        if (!poi) {
            return;
        }
        if (poi.position) {
            poi.longitude = poi.position[0];
            poi.latitude = poi.position[1];
        }
        if (poi.latitude && poi.longitude) {
            if (poi._create) {
                currentType.pois.push(poi);
            }
            let marker = poiMarkerStore[poi.key];
            if (!marker) {
                let center = new qq.maps.LatLng(poi.latitude, poi.longitude);
                marker = new qq.maps.Marker({
                    position: center,
                    title: poi.name || "",
                    draggable: false,
                });
                if (poi._create) {
                    marker.setIcon(newMarkerIcon);
                } else {
                    marker.setIcon(defaultMarkerIcon);
                }
                poiMarkerStore[poi.key] = marker;
            }
            marker.setMap(mapObj);
            qq.maps.event.addListener(marker, 'click', function () {
                bindPoiInfo(poi, marker);
                $("#detailSave").removeAttr("disabled");
                $('#poiModal').modal({ "backdrop": "static", "focus": true });
            });
            //at android,move poi has bug
            // qq.maps.event.addListener(marker, 'dragend', function (event) {
            //     poi.latitude = event.latLng.lat.toFixed(6);
            //     poi.longitude = event.latLng.lng.toFixed(6);
            // });
        }
    }

    function bindPoiInfo(poi, marker) {
        currentPoi = poi;
        currentMarker = marker;
        $("#key").val(poi.key);
        $("#poiName").val(poi.name);
        $("#poiAddress").val(poi.address);
        $("#poiDetailUrl").val(poi.detailUrl);
        if (poi.latitude && poi.longitude) {
            $("#poiLatitude").val(poi.latitude);
            $("#poiLongitude").val(poi.longitude);
        }
        updatePOIImagesNum(poi);
    }

    function clearCurrentTypeMarkers() {
        if (currentType && currentType.pois) {
            for (let poi of currentType.pois) {
                let marker = poiMarkerStore[poi.key];
                if (marker) {
                    marker.setMap(null);
                }
            }
        }
    }

    function updatePOIImagesNum(poi, forceReload) {
        $("#poiImages").empty();
        if (poi.imagesNum) {
            let ver = poi.updateVersion || "";
            for (let i = 0; i < poi.imagesNum; i++) {
                let ss = '<div class="browser-item">' +
                    '<img src="p/' + poi.key + '/' + poi.key + '_' + ver + '_' + i + '"/>' +
                    '<div class="mask" imageIndex="' + i + '"></div>' +
                    '</div>';
                let jBrowserItem = $(ss);
                if (poi.imageIndex == i) {
                    jBrowserItem.find(".mask").addClass("selected");
                }
                $("#poiImages").append(jBrowserItem);
                jBrowserItem.click(function () {
                    $("#poiImages").find(".mask.selected").removeClass("selected");
                    jBrowserItem.find(".mask").addClass("selected");
                });
            }
        }
    }

    //检查是否hasError,是时显示错误tip，否则清除错误tip（改在问题后）
    function checkError(jItem, hasError, err) {
        if (hasError) {
            jItem.addClass("hasErrorTip");
            jItem.attr("data-original-title", err);
            jItem.tooltip({
                placement: "top",
                trigger: "focus"
            })
            jItem.focus();
            return err;
        } else {
            //移除遗留的错误tooltip
            if (jItem.hasClass("hasErrorTip")) {
                jItem.removeAttr("data-original-title");
                jItem.removeClass("hasErrorTip");
            }
        }
    }

    function storePoiInfo() {
        if (!currentPoi) {
            return;
        }
        let jItem = $("#poiName");
        let name = $.trim(jItem.val());
        if (checkError(jItem, !name, "名称不能为空！")) {
            return true;
        }

        jItem = $("#poiAddress");
        let address = $.trim(jItem.val());


        jItem = $("#poiLongitude");
        let lon = parseFloat(jItem.val());
        if (checkError(jItem, isNaN(lon), "经度数据错误，请重新设置!")) {
            return true;
        }

        jItem = $("#poiLatitude");
        let lat = parseFloat(jItem.val());
        if (checkError(jItem, isNaN(lat), "纬度数据错误，请重新设置!")) {
            return true;
        }

        let poiDetailUrl = $.trim($("#poiDetailUrl").val());

        let imageIndex = $("#poiImages").find(".mask.selected").attr("imageIndex");
        currentPoi.name = name;
        currentPoi.address = address;
        currentPoi.latitude = lat;
        currentPoi.longitude = lon;
        currentPoi.detailUrl = poiDetailUrl;
        currentPoi.imageIndex = imageIndex;

        currentMarker.setTitle(name);
        currentMarker.setIcon(defaultMarkerIcon);
    }


    $("#poiImageInput").blur(function () {
        var str = $(this).val();
        $("#poiImage").attr("src", str);
        console.log($("#poiImage").attr("src"))
    })

    $("#poiImageInput").keypress(function (event) {
        if (event.keyCode === 13) {//Enter
            var str = $(this).val();
            $("#poiImage").attr("src", str);
            console.log($("#poiImage").attr("src"))
        }
    })

    $("#poiDelete").click(function () {
        $("#confirmText").text("确认删除当前点?");
        jConfirmModel._callback = doPoiDelete;
        jConfirmModel.modal({ "backdrop": "static", "focus": true });
    });

    function doPoiDelete() {
        $.getJSON("service?name=removepoi&key=" + currentPoi.key + "&v=" + new Date().getTime(), function (ret) {
            if (ret.retCode == 0) {
                currentMarker.setMap(null);
                for (let i = currentType.pois.length - 1; i >= 0; i--) {
                    if (currentType.pois[i] === currentPoi) {
                        currentType.pois.splice(i, 1);
                        break;
                    }
                }
                delete poiMarkerStore[currentPoi.key];
                currentMarker = null;
                currentPoi = null;
                $('#poiModal').modal('hide');
            } else if (ret.message) {
                alert(ret.message)
            }
        });
    }

    $("#deleteConfirmOK").click(function () {
        jConfirmModel.modal('hide');
        if (jConfirmModel._callback) {
            jConfirmModel._callback();
        }
    });

    $("#poiSave").click(function () {
        let hasErr = storePoiInfo();
        if (!hasErr) {
            savePoi(currentPoi, function () {
                $('#poiModal').modal('hide');
            });
        }
    });

    $("#detailSave").click(function () {
        let poiDetailUrl = $.trim($("#poiDetailUrl").val());
        if (!poiDetailUrl.startsWith("http")) {
            return;
        }
        $("#detailSave").attr("disabled", true);
        let tempPOI = { "key": currentPoi.key, "detailUrl": poiDetailUrl };
        $.ajax({
            type: "POST",
            url: "service?name=updatedetail",
            dataType: "json",
            data: JSON.stringify(tempPOI),
            contentType: 'text/plain; charset=UTF-8',
            cache: false,
            success: function (ret) {
                $("#detailSave").removeAttr("disabled");
                if (ret.retCode === 0) {
                    currentPoi.detailUrl = poiDetailUrl;
                    if (ret.data && ret.data.imagesNum) {
                        currentPoi.imagesNum = ret.data.imagesNum;
                        currentPoi.updateVersion = ret.data.updateVersion;
                        updatePOIImagesNum(currentPoi);
                    }
                    //加载images
                } else {
                    if (ret.message) {
                        alert(ret.message)
                    }
                }
            }
        });

    });

    function savePoi(poi, callback) {
        $.ajax({
            type: "POST",
            url: "service?name=updatepoi",
            dataType: "json",
            data: JSON.stringify(poi),
            contentType: 'text/plain; charset=UTF-8',
            cache: false,
            success: function (ret) {
                if (ret.retCode === 0) {
                    if (ret.data && ret.data.imagesNum) {
                        delete currentPoi._create;
                        currentPoi.imagesNum = ret.data.imagesNum;
                    }
                    callback();
                } else {
                    if (ret.message) {
                        alert(ret.message)
                    }
                }
            }
        });
    }


    $("#saveAllButton").click(function () {
        $.getJSON("service?name=saveall&v=" + new Date().getTime());
        if ($("#saveAllButton").attr("data-original-title")) {
            $("#saveAllButton").tooltip("show")
            setTimeout(function () {
                $("#saveAllButton").tooltip("hide");
                $("#saveAllButton").removeAttr("data-original-title");
            }, 3000);
        }
    });

    //modal显示后focus名称栏（空时）
    $('#poiModal').on('shown.bs.modal', function (e) {
        !$("#poiName").val() && $("#poiName").focus();
        $("#poiImages").find(".mask.selected").scrollintoview();
    })


    $("#pageConfig").click(function () {
        $("#appTitle").val(datas.title);
        $("#appName").val(datas.name);

        $("#typesTable tbody").empty();
        for (let i = 0; i < datas.types.length; i++) {
            let poiType = datas.types[i];
            let row = "<tr id=" + poiType.key + ">" +
                "<td>" + poiType.name + "</td>" +
                "<td><img class='mapIcon' src='images/文化场馆.png'></td>" +
                "<td><img class='mapIcon' src='images/文化场馆1.png'></td>" +
                "<td><img class='listImage' src='images/house.jpg'></td>" +
                "<td><button class='btn btn-sm btn-outline-info'>配置</button></td>" +
                "</tr>";
            let jTableRow = $(row);
            $("#typesTable tbody").append(jTableRow);
            jTableRow.click(function () {
                jTableRow.addClass("table-success").siblings().removeClass("table-success")
            })
        }
        if (currentType) {
            $("#typesTable #" + currentType.key).addClass("table-success");
        }

        $('#configModal').modal({ "backdrop": "static", "focus": true });
    });

    function storeAppInfo() {
        let jItem = $("#appTitle");
        let title = $.trim(jItem.val());
        if (checkError(jItem, !title, "标题不能为空！")) {
            return true;
        }
        jItem = $("#appName");
        let name = $.trim(jItem.val());
        if (checkError(jItem, !name, "路名不能为空！")) {
            return true;
        }

        datas.title = title;
        datas.name = name;
    }

    function sortTypesNav(newTypesKeyArray) {
        let types = datas.types;
        types.sort(function (a, b) {
            return newTypesKeyArray.indexOf(a.key) - newTypesKeyArray.indexOf(b.key);
        });
        buildTypes();
    }

    function saveAppInfo(callback) {
        let typesKeyArray = [];
        for (type of datas.types) {
            //通过table tr.deleted 判断记录是否已删除,提交加x标志
            if ($("#typesTable #" + type.key).hasClass("deleted")) {
                typesKeyArray.push("x" + type.key);
            } else {
                typesKeyArray.push(type.key);
            }
        }
        let appInfo = {
            "title": datas.title,
            "name": datas.name,
            "typesKey": typesKeyArray
        }


        $.ajax({
            type: "POST",
            url: "service?name=updateapp",
            dataType: "json",
            data: JSON.stringify(appInfo),
            contentType: 'text/plain; charset=UTF-8',
            cache: false,
            success: function (ret) {
                if (ret.retCode === 0) {

                    for (let i = datas.types.length - 1; i >= 0; i--) {
                        //通过table tr.deleted 判断记录是否已删除,提交加x标志
                        if ($("#typesTable #" + datas.types[i].key).hasClass("deleted")) {
                            datas.types.splice(i, 1);
                        }
                    }
                    let newTypesKeyArray = [];
                    let jRows = $("#typesTable tbody").find(">tr");
                    for (let i = 0; i < jRows.length; i++) {
                        if (!jRows.eq(i).hasClass("deleted")) {
                            newTypesKeyArray.push(jRows.eq(i).attr("id"));
                        }
                    }
                    sortTypesNav(newTypesKeyArray);
                    selectDefaultType();
                    callback();
                } else {
                    if (ret.message) {
                        alert(ret.message)
                    }
                }
            }
        });
    }

    $("#configSave").click(function () {
        let hasErr = storeAppInfo();
        if (!hasErr) {
            saveAppInfo(function () {
                $('#configModal').modal('hide');
            });
        }

    });

    $("#moveUp,#moveDown").click(function () {
        let jSelectTableRow = $("#typesTable .table-success");
        if (jSelectTableRow.length == 1) {
            if ($(this).is('#moveUp')) {
                jSelectTableRow.insertBefore(jSelectTableRow.prev());
            } else {
                jSelectTableRow.insertAfter(jSelectTableRow.next());
            }
        }
    });

    //新增类型
    $("#newTypeButton").click(function () {
        let jItem = $("#newTypeName");
        let newTypeName = $.trim(jItem.val());
        if (checkError(jItem, !newTypeName, "类型名称不能为空！")) {
            return true;
        }
        $.getJSON("service?name=createpoitype&typename=" + newTypeName + "&v=" + new Date().getTime(), function (ret) {
            if (ret.retCode >= 0) {
                let newType = ret.data;
                newType.pois = [];
                newType._create = true;
                datas.types.push(newType);
                let row = "<tr id=" + newType.key + " style='color:red;'>" +
                    "<td>" + newType.name +
                    "</td>" +
                    "<td><img class='mapIcon' src='images/文化场馆.png'></td>" +
                    "<td><img class='mapIcon' src='images/文化场馆1.png'></td>" +
                    "<td><img class='listImage' src='images/house.jpg'></td>" +
                    "<td><button class='btn btn-sm btn-outline-info'>配置</button></td>" +
                    "</tr>";
                let jTableRow = $(row);
                $("#typesTable tbody").append(jTableRow);
                jTableRow.click(function () {
                    jTableRow.addClass("table-success").siblings().removeClass("table-success")
                })

                $('#newTypeModal').modal('hide');
                $("#newTypeName").empty();
            }
        });

    });

    function doPoiTypeDelete() {
        let jSelectTableRow = $("#typesTable .table-success");
        if (jSelectTableRow.length == 1) {
            jSelectTableRow.addClass("deleted");
        }
    }

    $("#deletePoiType").click(function () {
        $("#confirmText").text("确认删除当前类型?");
        jConfirmModel._callback = doPoiTypeDelete;
        jConfirmModel.modal({ "backdrop": "static", "focus": true });

    });

});
