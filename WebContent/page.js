$(document).ready(function () {
    var datas;
    var currentType;
    var currentPoi;
    var currentMarker;
    var configType;//app 配置中编辑的poiType
    var poiMarkerStore = {};
    var $confirmModal = $('#confirmModal');
    console.dir($confirmModal)
    var mapObj = new qq.maps.Map(document.getElementById("mapPanel"), {
        center: new qq.maps.LatLng(31.218914, 121.425362),
        mapTypeControlOptions: {
            position: qq.maps.ControlPosition.BOTTOM_RIGHT    //设置地图控件位置靠近顶部
        },
        draggingCursor: "pointer",
        draggableCursor: "crosshair",
        zoom: 17
    });

    $('body').on('shown.bs.modal', '.modal', function (e) {
        $(this).find(".open-focus").focus();
        $(this).find(".open-scroll .mask.selected").scrollintoview();
    })

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
            let $typeItem = $(ss);
            $("#typeNav").append($typeItem);
            $typeItem.click(function () {
                selectType(poiType);
            });
            if (currentType == poiType) {
                $typeItem.addClass("active");
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

    //==========================  poi config start ========================

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
                let $browserItem = $(ss);
                if (poi.imageIndex == i) {
                    $browserItem.find(".mask").addClass("selected");
                }
                $("#poiImages").append($browserItem);
                $browserItem.click(function () {
                    $("#poiImages").find(".mask.selected").removeClass("selected");
                    $browserItem.find(".mask").addClass("selected");
                });
            }
        }
    }

    function storePoiInfo() {
        if (!currentPoi) {
            return;
        }
        let $item = $("#poiName");
        let name = $.trim($item.val());
        if (checkError($item, !name, "名称不能为空！")) {
            return true;
        }

        $item = $("#poiAddress");
        let address = $.trim($item.val());


        $item = $("#poiLongitude");
        let lon = parseFloat($item.val());
        if (checkError($item, isNaN(lon), "经度数据错误，请重新设置!")) {
            return true;
        }

        $item = $("#poiLatitude");
        let lat = parseFloat($item.val());
        if (checkError($item, isNaN(lat), "纬度数据错误，请重新设置!")) {
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
    })

    $("#poiImageInput").keypress(function (event) {
        if (event.keyCode === 13) {//Enter
            var str = $(this).val();
            $("#poiImage").attr("src", str);
        }
    })

    $("#poiDelete").click(function () {
        $("#confirmText").text("确认删除当前点?");
        $confirmModal._callback = doPoiDelete;
        $confirmModal.modal({ "backdrop": "static", "focus": true });
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
        $confirmModal.modal('hide');
        if ($confirmModal._callback) {
            $confirmModal._callback();
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
    //==========================  poi config end ========================


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

    //==========================  app config start ========================

    $("#pageConfig").click(function () {
        $("#appTitle").val(datas.title);
        $("#appName").val(datas.name);

        $("#typesTable tbody").empty();
        for (let i = 0; i < datas.types.length; i++) {
            let poiType = datas.types[i];
            let row = "<tr id=" + poiType.key + ">" +
                "<td>" + poiType.name + "</td>" +
                "<td><img class='mapIcon' src='" + poiType.markerPath + "/" + poiType.markerImage + "'></td>" +
                "<td><img class='mapIcon' src='" + poiType.markerPath + "/focus" + poiType.markerImage + "'></td>" +
                "<td><img class='listImage' src='" + poiType.picturePath + "/" + poiType.pictureImage + "'></td>" +
                "<td><button class='btn btn-sm btn-outline-info'>配置</button></td>" +
                "</tr>";
            let $tableRow = $(row);
            $("#typesTable tbody").append($tableRow);
            $tableRow.click(function () {
                $tableRow.addClass("table-success").siblings().removeClass("table-success")
            })
            $tableRow.find("button").click(function() {
                configType = poiType;
                $("#newTypeModalLabel").text("更新地图类型");
                $("#newTypeName").val(poiType.name);
                loadPOITypeConfigImages(poiType.markerImage,poiType.pictureImage);
                $("#newTypeModal").modal({ "backdrop": "static", "focus": true });
            });
        }
        if (currentType) {
            $("#typesTable #" + currentType.key).addClass("table-success");
        }
        loadCoverImages(datas.coverImage)

        $('#configModal').modal({ "backdrop": "static", "focus": true });
    });

    function storeAppInfo() {
        let $item = $("#appTitle");
        let title = $.trim($item.val());
        if (checkError($item, !title, "标题不能为空！")) {
            return true;
        }
        $item = $("#appName");
        let name = $.trim($item.val());
        if (checkError($item, !name, "路名不能为空！")) {
            return true;
        }

        $item = $("#coverImages");
        let coverImage = $item.find(".mask.selected").attr("name");
        if (checkError($item, !coverImage, "请选择封面图片！")) {
            return true;
        }

        datas.title = title;
        datas.name = name;
        datas.coverImage = coverImage;
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
            "coverImage": datas.coverImage,
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
        let $selectTableRow = $("#typesTable .table-success");
        if ($selectTableRow.length == 1) {
            if ($(this).is('#moveUp')) {
                $selectTableRow.insertBefore($selectTableRow.prev());
            } else {
                $selectTableRow.insertAfter($selectTableRow.next());
            }
        }
    });

    
    //新增类型
    $("#newTypeButton").click(function () {
        let $item = $("#newTypeName");
        let newTypeName = $.trim($item.val());
        if (checkError($item, !newTypeName, "类型名称不能为空！")) {
            return true;
        }

        $item = $("#markerImages");
        let markerPath = $item.find(".mask.selected").attr("path");
        let markerImage = $item.find(".mask.selected").attr("name");
        if (checkError($item, !markerImage, "请选择地图图标！")) {
            return true;
        }

        $item = $("#pictureImages");
        let picturePath = $item.find(".mask.selected").attr("path");
        let pictureImage = $item.find(".mask.selected").attr("name");
        if (checkError($item, !pictureImage, "请选择列表图片！")) {
            return true;
        }
        if(configType) {//update
            configType.name = newTypeName;
            configType.markerPath = markerPath;
            configType.markerImage = markerImage;
            configType.picturePath = picturePath;
            configType.pictureImage = pictureImage;

            $('#newTypeModal').modal('hide');
            $("#newTypeName").val("");
            $("#markerImages").empty();
            $("#pictureImages").empty();

            let $tds = $("#typesTable #" + configType.key).find("td");
            if($tds.length === 5) {
                $tds.eq(0).text(configType.name)
                $tds.eq(1).find(">img").attr("src",configType.markerPath + "/" + configType.markerImage)
                $tds.eq(2).find(">img").attr("src",configType.markerPath + "/focus" + configType.markerImage)
                $tds.eq(3).find(">img").attr("src",configType.picturePath + "/" + configType.pictureImage)
            }
        } else {//new
            $.getJSON("service?name=createpoitype&typename=" + newTypeName + "&v=" + new Date().getTime(), function (ret) {
                if (ret.retCode >= 0) {
                    let newType = ret.data;
                    newType.pois = [];
                    newType._create = true;
                    newType.markerPath = markerPath;
                    newType.markerImage = markerImage;
                    newType.picturePath = picturePath;
                    newType.pictureImage = pictureImage;
                    datas.types.push(newType);
                    let row = "<tr id=" + newType.key + " style='color:red;'>" +
                        "<td>" + newType.name +
                        "</td>" +
                        "<td><img class='mapIcon' src='" + markerPath + "/" + markerImage + "'></td>" +
                        "<td><img class='mapIcon' src='" + markerPath + "/focus" + markerImage + "'></td>" +
                        "<td><img class='listImage' src='" + picturePath + "/" + pictureImage + "'></td>" +
                        "<td><button class='btn btn-sm btn-outline-info'>配置</button></td>" +
                        "</tr>";
                    let $tableRow = $(row);
                    $("#typesTable tbody").append($tableRow);
                    $tableRow.click(function () {
                        $tableRow.addClass("table-success").siblings().removeClass("table-success")
                    })
                    $tableRow.find("button").click(function() {
                        configType = newType;

                        $("#newTypeModalLabel").text("更新地图类型");
                        $("#newTypeName").val(newType.name);
                        loadPOITypeConfigImages(newType.markerImage,newType.pictureImage);
                        $("#newTypeModal").modal({ "backdrop": "static", "focus": true });
                    });
        
                    $('#newTypeModal').modal('hide');
                    $("#newTypeName").val("");
                    $("#markerImages").empty();
                    $("#pictureImages").empty();
                }
            });
        }


    });

    function doPoiTypeDelete() {
        let $selectTableRow = $("#typesTable .table-success");
        if ($selectTableRow.length == 1) {
            $selectTableRow.addClass("deleted");
        }
    }

    $("#deletePoiType").click(function () {
        $("#confirmText").text("确认删除当前类型?");
        $confirmModal._callback = doPoiTypeDelete;
        $confirmModal.modal({ "backdrop": "static", "focus": true });
    });

    $("#newPoiType").click(function() {
        configType = null;
        $("#newTypeModalLabel").text("新增地图类型");
        $("#newTypeName").val("");
        loadPOITypeConfigImages();
    });
    //==========================  app config end ========================


    function loadCoverImages(imageName) {
        let $coverImages = $("#coverImages");
        $coverImages.empty();
        $.getJSON("service?name=images&group=cover&v=" + new Date().getTime(), function (ret) {
            if (ret.retCode >= 0 && ret.data) {
                let path = ret.data.path;
                let images = ret.data.images;
                for(let image of images) {
                    let ss = '<div class="browser-item">' +
                        '<img src="' + path + '/' + image + '"/>' +
                        '<div class="mask" name="' + image + '"></div>' +
                        '</div>';
                    let $browserItem = $(ss);
                    $coverImages.append($browserItem);
                    if (image == imageName) {
                        $browserItem.find(".mask").addClass("selected");
                    }

                    $browserItem.click(function () {
                        $coverImages.find(".mask.selected").removeClass("selected");
                        $browserItem.find(".mask").addClass("selected");
                    });
                }
            }
        });
    }

    //处理POIType类型配置中（含marker和picture）,合并2次请求为1次
    function loadPOITypeConfigImages(markerImageName,pictureImageName) {
        let $markerImages = $("#markerImages");
        let $pictureImages = $("#pictureImages");
        $markerImages.empty();
        $pictureImages.empty();
        $.getJSON("service?name=images&group=poitype&v=" + new Date().getTime(), function (ret) {
            if (ret.retCode >= 0 && ret.data) {
                let marker = ret.data.marker;
                let picture = ret.data.picture;

                let markerPath = marker.path;
                let markerImages = marker.images;
                for(let image of markerImages) {
                    let ss = '<div class="browser-item">' +
                        '<img src="' + markerPath + '/' + image + '"/>' +
                        '<div class="mask" path="' +  markerPath + '" name="' + image + '"></div>' +
                        '</div>';
                    let $browserItem = $(ss);
                    $markerImages.append($browserItem);
                    if (image == markerImageName) {
                        $browserItem.find(".mask").addClass("selected");
                    }

                    $browserItem.click(function () {
                        $markerImages.find(".mask.selected").removeClass("selected");
                        $browserItem.find(".mask").addClass("selected");
                    });
                }

                let picturePath = picture.path;
                let pictureImages = picture.images;
                for(let image of pictureImages) {
                    let ss = '<div class="browser-item">' +
                        '<img src="' + picturePath + '/' + image + '"/>' +
                        '<div class="mask" path="' +  picturePath + '" name="' + image + '"></div>' +
                        '</div>';
                    let $browserItem = $(ss);
                    $pictureImages.append($browserItem);
                    if (image == pictureImageName) {
                        $browserItem.find(".mask").addClass("selected");
                    }

                    $browserItem.click(function () {
                        $pictureImages.find(".mask.selected").removeClass("selected");
                        $browserItem.find(".mask").addClass("selected");
                    });
                }


            }
        });
    }

    //检查是否hasError,是时显示错误tip，否则清除错误tip（改在问题后）
    function checkError($item, hasError, err) {
        if (hasError) {
            $item.addClass("hasErrorTip");
            $item.attr("data-original-title", err);
            $item.tooltip({
                placement: "top",
                trigger: "focus"
            })
            $item.focus();
            return err;
        } else {
            //移除遗留的错误tooltip
            if ($item.hasClass("hasErrorTip")) {
                $item.removeAttr("data-original-title");
                $item.removeClass("hasErrorTip");
            }
        }
    }






});

