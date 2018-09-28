$(document).ready(function () {
    var currentPoi;
    var currentMarker;
    var pois = [];

    var mapObj = new qq.maps.Map(document.getElementById("mapPanel"), {
        center: new qq.maps.LatLng(31.218914, 121.425362),
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
        $.getJSON("service?name=createpoi&v=" + new Date().getTime(), function (ret) {
            if (ret.retCode >= 0) {
                var poi = ret.data;
                if (poi) {
                    poi.latitude = latLng.getLat().toFixed(6);
                    poi.longitude = latLng.getLng().toFixed(6);
                    buildPoi(poi, true);
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

    function buildPoi(poi, isCreate) {
        if (poi && poi.position) {
            poi.longitude = poi.position[0];
            poi.latitude = poi.position[1];
        }
        if (poi && poi.latitude && poi.longitude) {
            pois.push(poi);
            var center = new qq.maps.LatLng(poi.latitude, poi.longitude);
            var marker = new qq.maps.Marker({
                position: center,
                map: mapObj,
                title: poi.name || "",
                draggable: false,
            });
            if (isCreate) {
                marker.setIcon(newMarkerIcon);
            } else {
                marker.setIcon(defaultMarkerIcon);
            }
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

    function updatePOIImagesNum(poi,forceReload) {
        $("#poiImages").empty();
        if(poi.imagesNum) {
            let ver = poi.updateVersion || "";
            for(let i=0;i<poi.imagesNum;i++) {
                let ss = '<div class="browser-item">' + 
                    '<img src="p/' + poi.key + '/' + poi.key + '_' + ver + '_' + i + '"/>' +
                    '<div class="mask" imageIndex="' + i + '"></div>' +
                    '</div>';
                let jBrowserItem = $(ss);
                if(poi.imageIndex == i) {
                    jBrowserItem.find(".mask").addClass("selected");
                }
                $("#poiImages").append(jBrowserItem);
                jBrowserItem.click(function() {
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

    $.getJSON("service?name=datas&v=" + new Date().getTime(), function (ret) {
        if (ret.retCode >= 0 && ret.data) {
            var pois = ret.data.pois;
            if (pois) {
                for (let poi of pois) {
                    buildPoi(poi)
                }
            }
        }
    });

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

    $("#confirmOK").click(function () {
        $('#confirmModal').modal('hide');
        $.getJSON("service?name=removepoi&key=" + currentPoi.key + "&v=" + new Date().getTime(), function (ret) {
            if (ret.retCode == 0) {
                currentMarker.setMap(null);
                currentMarker = null;
                currentPoi = null;
                $('#poiModal').modal('hide');
            } else if (ret.message) {
                alert(ret.message)
            }
        });
    });

    $("#poiDelete").click(function () {
        $("#confirmTitle").text("确认");
        $("#confirmText").text("确认删除当前点?");
        $("#confirmModal").modal({ "backdrop": "static", "focus": true });

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
        if(!poiDetailUrl.startsWith("http")) {
            return;
        }
        $("#detailSave").attr("disabled",true);
        let tempPOI = {"key":currentPoi.key,"detailUrl":poiDetailUrl};
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
                    if(ret.data && ret.data.imagesNum) {
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
                    if(ret.data && ret.data.imagesNum) {
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


});
