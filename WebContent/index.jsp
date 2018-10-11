<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>

<head>
    <title></title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta name="format-detection" content="telephone=no, email=no">
    <link rel="stylesheet" href="css/bootstrap.min.css" />
    <link rel="stylesheet" href="css/jquery.fileupload.css">
    <link type="text/css" href="current.css" rel="stylesheet" />
</head>

<body class="my-modal-open">
    <div class="container">
        <div id='mapPanel' class='works-part  mapPanel'>
        </div>
        <div class="btn-group btn-group-toggle groupNav" id="groupNav" data-toggle="buttons">
        </div>
        <button type="button" class="btn btn-sm btn-success pageConfig" id="pageConfig" aria-label="Left Align"
            data-toggle="tooltip" data-placement="bottom" title="配置">
            <svg height="18" width="18" class="octicon octicon-gear" viewBox="0 0 14 16" version="1.1" aria-hidden="true">
                <path fill-rule="evenodd" fill="white" d="M14 8.77v-1.6l-1.94-.64-.45-1.09.88-1.84-1.13-1.13-1.81.91-1.09-.45-.69-1.92h-1.6l-.63 1.94-1.11.45-1.84-.88-1.13 1.13.91 1.81-.45 1.09L0 7.23v1.59l1.94.64.45 1.09-.88 1.84 1.13 1.13 1.81-.91 1.09.45.69 1.92h1.59l.63-1.94 1.11-.45 1.84.88 1.13-1.13-.92-1.81.47-1.09L14 8.75v.02zM7 11c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3z"></path>
            </svg>
        </button>
        <button type="button" id="saveAllButton" class="btn btn-sm btn-success saveAllButton" data-toggle="tooltip"
            data-placement="bottom" data-original-title="后台会5分钟保存一次数据，程序退出前，最好点击此按钮保存。" onpositionupdate="return;">保存后台数据</button>
        <div class="custom-control custom-checkbox clickCreatePoiDiv">
            <input type="checkbox" class="custom-control-input" id="clickCreatePoi">
            <label class="custom-control-label clickCreatePoi" for="clickCreatePoi">点击创建POI</label>
        </div>
        <jsp:include page="poiModal.htm"  flush="true"/>
        
        <jsp:include page="appModal.htm" flush="true"/>

        <jsp:include page="groupModal.htm"  flush="true"/>

        <jsp:include page="confirmModal.htm"  flush="true"/>

    </div>
</body>

</html>
<script src="lib/jquery-3.3.1.min.js"></script>
<script src="lib/jquery.scrollintoview.min.js"></script>
<script src="lib/jquery.ui.widget.js"></script>
<script src="lib/jquery.fileupload.js"></script>
<script src="lib/popper.min.js"></script>
<script src="lib/bootstrap.min.js"></script>
<script type="text/javascript" charset="utf-8" src="https://map.qq.com/api/js?v=2.exp"></script>
<script type="text/javascript" src="page.js"></script>