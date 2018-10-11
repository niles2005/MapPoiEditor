$(document).ready(function () {
    var datas;
    var currentGroup;
    var currentPoi;
    var currentMarker;
    var configGroup;//app 配置中编辑的poiGroup
    var poiMarkerStore = {};
    var $confirmModal = $('#confirmModal');

    var createMarkerIcon = new qq.maps.MarkerImage("images/create.png",
        null,
        null,
        null,
        new qq.maps.Size(24, 34)
    );

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
        $.getJSON("service?name=createpoi&groupkey=" + currentGroup.key + "&v=" + new Date().getTime(), function (ret) {
            if (ret.retCode >= 0) {
                var poi = ret.data;
                if (poi) {
                    poi.latitude = latLng.getLat().toFixed(6);
                    poi.longitude = latLng.getLng().toFixed(6);
                    poi._create = true;
                    currentGroup.pois.push(poi);
                    buildPoi(poi);
                }
            }
        });

    }

    $.getJSON("service?name=datas&v=" + new Date().getTime(), function (ret) {
        if (ret.retCode >= 0 && ret.data) {
            datas = ret.data;
            if (!datas.groups) {
                datas.groups = [];
            }
            buildGroups();
        }
    });

    function buildGroups() {
        let groups = datas.groups;
        if (groups.length == 0) {
            return;
        }
        $("#groupNav").empty();
        for (let i = 0; i < groups.length; i++) {
            let poiGroup = groups[i];
            if (!poiGroup.key) {
                continue;
            }
            if (!poiGroup.pois) {
                poiGroup.pois = {};
            }
            let title = "切换图层"
            let btnType = "btn-primary";
            if (poiGroup.isBG) {
                btnType = "btn-secondary"
                title = "背景图层"
            }
            let ss = '<label id="' + poiGroup.key + '" title="' + title + '" class="btn btn-sm ' + btnType + '">' +
                '<input type="radio" name="options" id="option1" autocomplete="off" checked>' +
                poiGroup.name +
                '</label>';
            let $groupItem = $(ss);
            $("#groupNav").append($groupItem);
            $groupItem.click(function () {
                selectGroup(poiGroup);
            });
            if (currentGroup == poiGroup) {
                $groupItem.addClass("active");
            }
            selectDefaultGroup();
        }
    }

    function selectDefaultGroup() {
        //currentGroup为空或者已经删除
        if (!currentGroup || datas.groups.indexOf(currentGroup) < 0) {
            if (datas.groups.length > 0) {
                $("#" + datas.groups[0].key).addClass("active");
                selectGroup(datas.groups[0]);
            }
        }
    }


    function selectGroup(theGroup) {
        if (currentGroup !== theGroup) {
            clearCurrentGroupMarkers();
            currentGroup = theGroup;
            if (currentGroup.pois) {
                for (let poi of currentGroup.pois) {
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

        var defaultMarkerIcon = new qq.maps.MarkerImage(currentGroup.markerPath + currentGroup.markerImage,
            null,
            null,
            null,
            new qq.maps.Size(currentGroup.markerImageWidth, currentGroup.markerImageHeight)
        );

        if (poi.latitude && poi.longitude) {
            let marker = poiMarkerStore[poi.key];
            if (!marker) {
                let center = new qq.maps.LatLng(poi.latitude, poi.longitude);
                marker = new qq.maps.Marker({
                    position: center,
                    title: poi.name || "",
                    draggable: false,
                });
                if (poi._create) {
                    marker.setIcon(createMarkerIcon);
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

    function clearCurrentGroupMarkers() {
        if (currentGroup && currentGroup.pois) {
            for (let poi of currentGroup.pois) {
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
                    '<img src="p/' + poi.key + '/' + poi.key + '_' + i + '?v=' + ver + '"/>' +
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

        var defaultMarkerIcon = new qq.maps.MarkerImage(currentGroup.markerPath + currentGroup.markerImage,
            null,
            null,
            null,
            new qq.maps.Size(currentGroup.markerImageWidth, currentGroup.markerImageHeight)
        );

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
                for (let i = currentGroup.pois.length - 1; i >= 0; i--) {
                    if (currentGroup.pois[i] === currentPoi) {
                        currentGroup.pois.splice(i, 1);
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
                    if (ret.data && ret.data.imagesNum > 0) {
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
        if (datas.introPage) {
            $('#introDownload').attr("href", datas.introPage + ".zip");
            $('#introDownload').removeClass("disabled");
            $('#introOpen').attr("href", datas.introPage);
            $('#introOpen').removeClass("disabled");
        } else {
            $('#introDownload').removeAttr("href");
            $('#introDownload').addClass("disabled");
            $('#introOpen').removeAttr("href");
            $('#introOpen').addClass("disabled");
        }

        $("#groupsTable tbody").empty();
        for (let i = 0; i < datas.groups.length; i++) {
            let poiGroup = datas.groups[i];
            let groupCheck = poiGroup.isBG ? "是" : "否";
            let row = "<tr id=" + poiGroup.key + ">" +
                "<td class='groupName'>" + poiGroup.name + "</td>" +
                "<td class='groupBG'>" + groupCheck + "</td>" +
                "<td><img class='mapIcon' src='" + poiGroup.markerPath + poiGroup.markerImage + "'>" +
                "<img class='mapIcon' src='" + poiGroup.markerPath + "focus" + poiGroup.markerImage + "'></td>" +
                "<td><img class='listImage' src='" + poiGroup.picturePath + poiGroup.pictureImage + "'></td>" +
                "<td><button class='btn btn-sm btn-outline-info'>配置</button></td>" +
                "</tr>";
            let $tableRow = $(row);
            $("#groupsTable tbody").append($tableRow);
            $tableRow.click(function () {
                $tableRow.addClass("table-success").siblings().removeClass("table-success")
            })
            $tableRow.find("button").click(function () {
                configGroup = poiGroup;
                $("#groupModalLabel").text("更新地图类型");
                $("#groupName").val(poiGroup.name);
                $("#groupBG").text(groupCheck);
                $("#markerImageWidth").val(poiGroup.markerImageWidth);
                $("#markerImageHeight").val(poiGroup.markerImageHeight);
                loadPOIGroupConfigImages(poiGroup.markerImage, poiGroup.pictureImage);
                $("#groupModal").modal({ "backdrop": "static", "focus": true });
            });
        }
        if (currentGroup) {
            $("#groupsTable #" + currentGroup.key).addClass("table-success");
        }
        loadCoverImages(datas.coverImage)

        $('#appModal').modal({ "backdrop": "static", "focus": true });
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

        $item = $("#introOpen");
        let introPage = $item.attr("href");

        datas.title = title;
        datas.name = name;
        datas.coverImage = coverImage;
        datas.introPage = introPage;

    }

    function sortGroupsNav(groupsKeyArray) {
        let groups = datas.groups;
        groups.sort(function (a, b) {
            return groupsKeyArray.indexOf(a.key) - groupsKeyArray.indexOf(b.key);
        });
        buildGroups();
    }

    function saveAppInfo(callback) {
        let groupStore = {}
        for (group of datas.groups) {
            groupStore[group.key] = group;
        }
        let updateGroupsArray = [];
        let trs = $("#groupsTable tbody tr");
        for (let i = 0; i < trs.length; i++) {
            let key = trs.eq(i).attr("id");
            let group = groupStore[key];
            let updateGroup = {};
            updateGroup.key = group.key;
            updateGroup.name = group.name;
            updateGroup.isBG = group.isBG;
            updateGroup.markerPath = group.markerPath;
            updateGroup.markerImage = group.markerImage;
            updateGroup.markerImageWidth = group.markerImageWidth;
            updateGroup.markerImageHeight = group.markerImageHeight;
            updateGroup.picturePath = group.picturePath;
            updateGroup.pictureImage = group.pictureImage;

            //通过table tr.deleted 判断记录是否已删除
            if ($("#groupsTable #" + group.key).hasClass("deleted")) {
                updateGroup._deleted = true;
            }
            updateGroupsArray.push(updateGroup)
        }
        let appInfo = {
            "title": datas.title,
            "name": datas.name,
            "coverImage": datas.coverImage,
            "introPage": datas.introPage,
            "groups": updateGroupsArray
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

                    for (let i = datas.groups.length - 1; i >= 0; i--) {
                        //通过table tr.deleted 判断记录是否已删除
                        if ($("#groupsTable #" + datas.groups[i].key).hasClass("deleted")) {
                            datas.groups.splice(i, 1);
                        }
                    }
                    let groupsKeyArray = [];
                    let jRows = $("#groupsTable tbody").find(">tr");
                    for (let i = 0; i < jRows.length; i++) {
                        if (!jRows.eq(i).hasClass("deleted")) {
                            groupsKeyArray.push(jRows.eq(i).attr("id"));
                        }
                    }
                    sortGroupsNav(groupsKeyArray);
                    selectDefaultGroup();
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
                $('#appModal').modal('hide');
            });
        }

    });

    $("#moveUp,#moveDown").click(function () {
        let $selectTableRow = $("#groupsTable .table-success");
        if ($selectTableRow.length == 1) {
            if ($(this).is('#moveUp')) {
                $selectTableRow.insertBefore($selectTableRow.prev());
            } else {
                $selectTableRow.insertAfter($selectTableRow.next());
            }
        }
    });


    //类型新增或保存
    $("#groupSave").click(function () {
        let $item = $("#groupName");
        let groupName = $.trim($item.val());
        if (checkError($item, !groupName, "类型名称不能为空！")) {
            return true;
        }

        $item = $("#markerImages");
        let markerPath = $item.find(".mask.selected").attr("path");
        let markerImage = $item.find(".mask.selected").attr("name");
        if (checkError($item, !markerImage, "请选择地图图标！")) {
            return true;
        }

        $item = $("#markerImageWidth");
        let markerImageWidth = parseInt($item.val());
        if (checkError($item, isNaN(markerImageWidth), "图片宽度数据错误，请重新设置!")) {
            return true;
        }

        $item = $("#markerImageHeight");
        let markerImageHeight = parseInt($item.val());
        if (checkError($item, isNaN(markerImageHeight), "图片高度数据错误，请重新设置!")) {
            return true;
        }

        $item = $("#groupBG");
        let bgLabel = $.trim($item.text());
        let isBG = bgLabel === "是";

        $item = $("#pictureImages");
        let picturePath = $item.find(".mask.selected").attr("path");
        let pictureImage = $item.find(".mask.selected").attr("name");
        if (!isBG && checkError($item, !pictureImage, "请选择列表图片！")) {
            return true;
        }

        if (configGroup) {//update
            configGroup.name = groupName;
            configGroup.isBG = bgLabel === "是";
            configGroup.markerPath = markerPath;
            configGroup.markerImage = markerImage;
            configGroup.markerImageWidth = markerImageWidth;
            configGroup.markerImageHeight = markerImageHeight;
            configGroup.picturePath = picturePath;
            configGroup.pictureImage = pictureImage;

            $('#groupModal').modal('hide');
            $("#groupName").val("");
            $("#markerImageWidth").val("");
            $("#markerImageHeight").val("");
            $("#groupBG").text("否");
            $("#markerImages").empty();
            $("#pictureImages").empty();

            let $tr = $("#groupsTable #" + configGroup.key);
            $tr.find(".groupName").text(configGroup.name)
            $tr.find(".groupBG").text(bgLabel)
            $tr.find("img.mapIcon").eq(0).attr("src", configGroup.markerPath + configGroup.markerImage)
            $tr.find("img.mapIcon").eq(1).attr("src", configGroup.markerPath + "focus" + configGroup.markerImage)
            $tr.find("img.listImage").attr("src", configGroup.picturePath + configGroup.pictureImage)
        } else {//new
            $.getJSON("service?name=createpoigroup&groupname=" + groupName + "&v=" + new Date().getTime(), function (ret) {
                if (ret.retCode >= 0) {
                    let group = ret.data;
                    group.pois = [];
                    group._create = true;
                    group.isBG = bgLabel === "是";
                    group.markerPath = markerPath;
                    group.markerImage = markerImage;
                    group.markerImageWidth = markerImageWidth;
                    group.markerImageHeight = markerImageHeight;
                    group.picturePath = picturePath;
                    group.pictureImage = pictureImage;
                    datas.groups.push(group);
                    let row = "<tr id=" + group.key + " style='color:red;'>" +
                        "<td class='groupName'>" + group.name + "</td>" +
                        "<td class='groupBG'>" + bgLabel + "</td>" +
                        "<td><img class='mapIcon' src='" + group.markerPath + group.markerImage + "'>" +
                        "<img class='mapIcon' src='" + group.markerPath + "focus" + group.markerImage + "'></td>" +
                        "<td><img class='listImage' src='" + group.picturePath + group.pictureImage + "'></td>" +
                        "<td><button class='btn btn-sm btn-outline-info'>配置</button></td>" +
                        "</tr>";
                    let $tableRow = $(row);
                    $("#groupsTable tbody").append($tableRow);
                    $tableRow.click(function () {
                        $tableRow.addClass("table-success").siblings().removeClass("table-success")
                    })
                    $tableRow.find("button").click(function () {
                        configGroup = group;

                        $("#groupModalLabel").text("更新地图类型");
                        $("#groupName").val(group.name);
                        $("#groupBG").text(bgLabel);
                        $("#markerImageWidth").val(group.markerImageWidth);
                        $("#markerImageHeight").val(group.markerImageHeight);

                        loadPOIGroupConfigImages(group.markerImage, group.pictureImage);
                        $("#groupModal").modal({ "backdrop": "static", "focus": true });
                    });

                    $('#groupModal').modal('hide');
                    $("#groupName").val("");
                    $("#groupBG").text("否");
                    $("#markerImages").empty();
                    $("#pictureImages").empty();
                }
            });
        }


    });

    function doPoiGroupDelete() {
        let $selectTableRow = $("#groupsTable .table-success");
        if ($selectTableRow.length == 1) {
            $selectTableRow.addClass("deleted");
        }
    }

    $("#deletePoiGroup").click(function () {
        $("#confirmText").text("确认删除当前类型?");
        $confirmModal._callback = doPoiGroupDelete;
        $confirmModal.modal({ "backdrop": "static", "focus": true });
    });

    $("#newPoiGroup").click(function () {
        configGroup = null;
        $("#groupModalLabel").text("新增地图类型");
        $("#groupName").val("");
        $("#groupGg").text("否");
        $("#markerImageWidth").val("24");
        $("#markerImageHeight").val("35");
        loadPOIGroupConfigImages();
    });
    //==========================  app config end ========================


    function loadCoverImages(imageName) {
        let $coverImages = $("#coverImages");
        $coverImages.empty();
        $.getJSON("service?name=images&type=cover&v=" + new Date().getTime(), function (ret) {
            if (ret.retCode >= 0 && ret.data) {
                let path = ret.data.path;
                let images = ret.data.images;
                for (let image of images) {
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

    //处理POIGroup类型配置中（含marker和picture）,合并2次请求为1次
    function loadPOIGroupConfigImages(markerImageName, pictureImageName) {
        let $markerImages = $("#markerImages");
        let $pictureImages = $("#pictureImages");
        $markerImages.empty();
        $pictureImages.empty();
        $.getJSON("service?name=images&type=group&v=" + new Date().getTime(), function (ret) {
            if (ret.retCode >= 0 && ret.data) {
                let marker = ret.data.marker;
                let picture = ret.data.picture;

                let markerPath = marker.path;
                let markerImages = marker.images;
                for (let image of markerImages) {
                    let ss = '<div class="browser-item">' +
                        '<img class="markerIcon" src="' + markerPath + '/' + image + '"/>' +
                        '<img class="markerIcon" src="' + markerPath + '/focus' + image + '"/>' +
                        '<div class="mask" path="' + markerPath + '" name="' + image + '"></div>' +
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
                for (let image of pictureImages) {
                    let ss = '<div class="browser-item">' +
                        '<img src="' + picturePath + '/' + image + '"/>' +
                        '<div class="mask" path="' + picturePath + '" name="' + image + '"></div>' +
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




    $("#groupBG").click(function () {
        let text = $(this).text();
        text = text === "是" ? "否" : "是";
        $(this).text(text);
    })

    $('#introUpload').fileupload({
        url: "introUpload",
        dataType: 'json',
        done: function (e, data) {
            if (data.result.retCode === 0 && data.result.data) {
                $('#introDownload').attr("href", data.result.data.introPage + ".zip");
                $('#introDownload').removeClass("disabled");
                $('#introOpen').attr("href", data.result.data.introPage);
                $('#introOpen').removeClass("disabled");
            } else if (data.result.retCode < 0 && data.result.message) {
                alert(data.result.message)
            }
        },
        progressall: function (e, data) {
        }
    })

    $('.fileUpload').each(function(index) {
        let $upload = $('.fileUpload').eq(index);
        let path = $upload.attr("path");
        $upload.fileupload({
            url: "upload?name=image&path=" + path,
            dataType: 'json',
            done: function (e, data) {
                if (data.result.retCode === 0 && data.result.data) {
                    console.dir(data)
                } else if (data.result.retCode < 0 && data.result.message) {
                    alert(data.result.message)
                }
            },
            progressall: function (e, data) {
            }
        })
    }); 

});

