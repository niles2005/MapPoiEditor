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
                $('#poiModal').modal({ "backdrop": "static", "focus": true });
                if (!poi.name) {
                }
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

        currentPoi.name = name;
        currentPoi.address = address;
        currentPoi.latitude = lat;
        currentPoi.longitude = lon;
        currentPoi.detailUrl = poiDetailUrl;

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
    })


});
