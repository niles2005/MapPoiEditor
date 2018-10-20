(function() {
    function Base() {
    }

    var jModalSearching,jGlobalPopLayer,jDownloadFrame;
    works.utils = {
        inherits: function(subClass, superClass) {
            var sub = subClass.prototype;
            Base.prototype = superClass.prototype;
            var sup = new Base();
            Base.prototype = null;
            for (prop in sub) {
                sup[prop] = sub[prop];
            }
            subClass.prototype = sup;
            sup.constructor = subClass;
        },
        extend: function(dest, src) {
            for (var prop in src) {
                dest[prop] = src[prop];
            }
            return dest;
        },
        removeItem: function(array, item) {
            var k = array.length;
            if (k <= 0) {
                return;
            }
            while (k--) {
                if (array[k] === item) {
                    array.splice(k, 1);
                    break;
                }
            }
        },
        loadJsonData: function(url, listener, param) {
            if (works.ajaxtype === 0) {
                $.ajax({
                    url: url,
                    data: param,
                    dataType: "json", //这里的dataType就是返回回来的数据格式了html,xml,json 
                    cache: false, //设置是否缓存，默认设置成为true，当需要每次刷新都需要执行数据库操作的话，需要设置成为false 
                    success: listener
                });
            } else {
                FlyJSONP.get({
                    url: url,
                    parameters: param,
                    success: listener,
                    error: function(errorMsg) {
                        //console.log(errorMsg);
                    }
                });
            }
        },
        loadXMLFile: function(url, listener, caller) {
            $.ajax({
                url: url,
                dataType: "xml",
                cache: false,
                success: function() {
                    listener.apply(caller, arguments);
                }
            });
        },
        getStyleWidth: function(control) {
            var value = control.style.width;
            if (value.endsWith("px")) {
                value = value.substring(0, value.length - 2);
            }
            value = parseInt(value);
            return value;
        },
        getStyleHeight: function(control) {
            var value = control.style.height;
            if (value.endsWith("px")) {
                value = value.substring(0, value.length - 2);
            }
            value = parseInt(value);
            return value;
        },
        getStyleTop: function(control) {
            var value = control.style.top;
            if (value.endsWith("px")) {
                value = value.substring(0, value.length - 2);
            }
            value = parseInt(value);
            return value;
        },
        getStyleLeft: function(control) {
            var value = control.style.left;
            if (value.endsWith("px")) {
                value = value.substring(0, value.length - 2);
            }
            value = parseInt(value);
            return value;
        },
        bindEvent: function(elementTarget, eventType, func) {
            if (window.addEventListener) {
                elementTarget.addEventListener(eventType, func, false);
            } else if (window.attachEvent) {
                elementTarget.attachEvent("on" + eventType, func);
            }
        },
        unbindEvent: function(elementTarget, eventType, func) {
            if (window.addEventListener) {
                elementTarget.removeEventListener(eventType, func, false);
            } else if (window.attachEvent) {
                elementTarget.detachEvent("on" + eventType, func);
            }
        },
        getWindowWidth: function() {
            var myWidth = 0;
            if (typeof(window.innerWidth) === 'number') {
                //Non-IE
                myWidth = window.innerWidth;
                //                myHeight = window.innerHeight;
            } else if (document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight)) {
                //IE 6+ in 'standards compliant mode'
                myWidth = document.documentElement.clientWidth;
                //                myHeight = document.documentElement.clientHeight;
            } else if (document.body && (document.body.clientWidth || document.body.clientHeight)) {
                //IE 4 compatible
                myWidth = document.body.clientWidth;
                //                myHeight = document.body.clientHeight;
            }
            return myWidth;
        },
        getWindowHeight: function() {
            var myHeight = 0;
            if (typeof(window.innerHeight) === 'number') {
                //Non-IE
                myHeight = window.innerHeight;
            } else if (document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight)) {
                //IE 6+ in 'standards compliant mode'
                myHeight = document.documentElement.clientHeight;
            } else if (document.body && (document.body.clientWidth || document.body.clientHeight)) {
                //IE 4 compatible
                myHeight = document.body.clientHeight;
            }
            return myHeight;
        },
        getOffset: function(event) {//for firefox bug
            var target = event.target;
            if (target.offsetLeft === undefined)
            {
                target = target.parentNode;
            }
            var pageCoord = getPageCoord(target);
            var eventCoord =
                    {//计算鼠标位置（触发元素与窗口的距离）
                        x: window.pageXOffset + event.clientX,
                        y: window.pageYOffset + event.clientY
                    };
            var offset =
                    {
                        offsetX: eventCoord.x - pageCoord.x,
                        offsetY: eventCoord.y - pageCoord.y
                    };
            return offset;
        },
        getClientSize: function() {
            if (window.innerHeight) {
                return {width: window.innerWidth, height: window.innerHeight};
            }
            else {
                if (document.documentElement && document.documentElement.clientHeight) {
                    return {width: document.documentElement.clientWidth, height: document.documentElement.clientHeight};
                }
                else {
                    return {width: document.body.clientWidth, height: document.body.clientHeight};
                }
            }
        },
        formatDateTime: function(datetime) {
            if(!datetime) {
                return "";
            }
            if(datetime.length === 14) {//20140227153135
                var newDatetime = datetime.substring(0,4) + "-" + datetime.substring(4,6) + "-" + datetime.substring(6,8) +
                        " " + datetime.substring(8,10) + ":" + datetime.substring(10,12) + ":" + datetime.substring(12,14);
                return newDatetime;
            }
            return datetime;
        },
        formatDate: function(date, form) {
            var year;
           var isIE = (navigator.userAgent.indexOf('MSIE') >= 0) && (navigator.userAgent.indexOf('Opera') < 0);
           if (isIE) {
               if (works.IEVersion < 9) {
                   year = date.getYear();
               }else{
                   year = (1900 + date.getYear());
               }
           } else {
                   year = (1900 + date.getYear());
           }
    
            var month = date.getMonth() + 1;
            if(month < 10) {
                month = "0" + month;
            }
            var day = date.getDate();
            if(day < 10) {
                day = "0" + day;
            }
            var hour = date.getHours();
            if(hour < 10) {
                hour = "0" + hour;
            }
            var minute = date.getMinutes();
            if(minute < 10) {
                minute = "0" + minute;
            }
            var second = date.getSeconds();
            if(second < 10) {
                second = "0" + second;
            }
            if (form === 'day') {
                return year + "-" + month + "-" + day;
            } else if (form === 'day8') {
                return year + "" + month + "" + day;
            } else if (form === 'day10') {
                return year + "-" + month + "-" + day;
            } else if(form === '中文日期'){
                return year + "年" + month + "月" + day +"日";
            } else if (form === 'time') {
                return hour + ":" + minute + ":" + second;
            } else if (form === 'ym6') {
                return year + "" + month;
            } else {
                return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
            }
        },
	date8ToDate10: function(strDate,split) {//20130731 -> 2013-07-31
            if(strDate.length === 8) {
                if(!split) {
                    split = "-";
                }
                return strDate.substring(0,4) + split + strDate.substring(4,6) + split + strDate.substring(6);
            } else if(strDate.length === 10) {
                    return strDate;
            }
            return null;
	},
	date10ToDate8: function(strDate) {  //2013-07-31  -> 20130731
		if(strDate.length === 10) {
			return strDate.substring(0,4) + strDate.substring(5,7) + strDate.substring(8);
		} else if(strDate.length === 8) {
			return strDate;
		}
		return null;
	},
        getToday: function(offsetDayNum,format) {
           var date = new Date();
           if(offsetDayNum) {
               date = new Date(date.getTime() + offsetDayNum * 86400000);
           }
           if(!format) {
               format = "day10";
           }
           return works.utils.formatDate(date, format);
        },
        getTime: function(format) {
           var date = new Date();
            var hour = date.getHours();
            if(hour < 10) {
                hour = "0" + hour;
            }
            var minute = date.getMinutes();
            if(minute < 10) {
                minute = "0" + minute;
            }
            var second = date.getSeconds();
            if(second < 10) {
                second = "0" + second;
            }
           
           if(!format) {
               format = "time8";
           }
           if("time8" === format) {
               return hour + ":" + minute + ":" + second;
           } else if("time6" === format) {
               return hour + minute + second;
           }
       },
	
        parseTime: function(timeString) {
            if(!timeString) {
                return;
            }
            timeString = $.trim(timeString);
            var arr = timeString.split(":");
            if(arr.length === 3) {
                var hour = parseInt(arr[0],10);
                var minute = parseInt(arr[1],10);
                var second = parseInt(arr[2],10);
                return hour * 3600 + minute * 60 + second;
            } else if(arr.length === 2) {
                var hour = parseInt(arr[0],10);
                var minute = parseInt(arr[1],10);
                return hour * 3600 + minute * 60;
            } else if(arr.length === 1) {
                if(timeString.length === 4) {//1230
                    var hour = parseInt(timeString.substring(0,2),10);
                    var minute = parseInt(timeString.substring(2,4),10);
                    return hour * 3600 + minute * 60;
                } else if(timeString.length === 6) {//123000
                    var hour = parseInt(timeString.substring(0,2),10);
                    var minute = parseInt(timeString.substring(2,4),10);
                    var second = parseInt(timeString.substring(4,6),10);
                    return hour * 3600 + minute * 60 + second;
                } else if(timeString.length === 14) {//20140716111449
                    var hour = parseInt(timeString.substring(8,10),10);
                    var minute = parseInt(timeString.substring(10,12),10);
                    var second = parseInt(timeString.substring(12,14),10);
                    return hour * 3600 + minute * 60 + second;
                }
            } else {
                return;
            }
        },
        formatTime: function(timeString) {
            if(!timeString) {
                return null;
            }
            var pos = timeString.indexOf("：");
            if(pos !== -1) {
                timeString = timeString.substring(0,pos) + ":" + timeString.substring(pos);
            }
            var timeArr = timeString.split(":");
            if(timeArr.length === 2) {
                var minute = parseInt(timeArr[0],10);
                var second = parseInt(timeArr[1],10);
                if(minute >= 0 && minute < 24 && second >= 0 && second < 60) {
                    var ss = "";
                    if(minute < 10) {
                        ss += 0;
                    }
                    ss += minute;
                    ss += ":";
                    if(second < 10) {
                        ss += 0;
                    }
                    ss += second;
                    return ss;
                }
            }
            return null;
       },
       buildTimeValue: function(timeString) {//return:  1230   50(0050)
            if(!timeString) {
                return null;
            }
            var pos = timeString.indexOf("：");
            if(pos !== -1) {
                timeString = timeString.substring(0,pos) + ":" + timeString.substring(pos);
            }
            var timeArr = timeString.split(":");
            if(timeArr.length === 2) {
                var minute = parseInt(timeArr[0],10);
                var second = parseInt(timeArr[1],10);
                if(minute >= 0 && minute < 24 && second >= 0 && second < 60) {
                    return minute * 100 + second;
                }
            }
            return null;
       },
       formatTimeValue: function(timeValue,formatLength) {//1230  => 12:30   formatLength(4: 1130   5:11:30)
            if(!formatLength) {
                formatLength = 5;
            }
            var minute = parseInt(timeValue / 100);
            var second = parseInt(timeValue % 100);
            var ss = "";
            if(minute < 10) {
                ss += 0;
            }
            ss += minute;
            if(formatLength === 4) {
                
            } else {
                ss += ":";
            }
            if(second < 10) {
                ss += 0;
            }
            ss += second;
            return ss;
       },
       ajaxGet: function(url,callback) {
            $.ajax({
                url: url,
                dataType: "json", //这里的dataType就是返回回来的数据格式了html,xml,json 
                cache: false, //设置是否缓存，默认设置成为true，当需要每次刷新都需要执行数据库操作的话，需要设置成为false 
                success: function(data) {
                    callback.call(this,data);
                }
            });
        },
        dealwithUrl: function(url) {
            var url = $.trim(url);
            if(works.test) {
                var pos = url.indexOf("?");
                var param = "";
                if(pos >= 0) {
                    param = url.substring(pos);
                    url = url.substring(0,pos);
                }
                pos = url.lastIndexOf("/");
                if(pos || pos === 0) {
                    url = url.substring(pos + 1);
                }
                if(!url.endsWith(".json")) {
                    url = url + ".json";
                }
                url += param;
                return url;
            }
            if(url.startsWith("/Service")) {
                return url;
            } else if(url.startsWith("/service")) {
                return url;
            } else if(url.startsWith("/gisdata/")) {    
                return url;
            } else if(url.startsWith("/mapserver/")) {    
                return url;
            } else if(url.startsWith("/mapdata/")) {    
                return url;
            } else if(url.startsWith("http://")) {
                return url;
            } else if(url.startsWith("https://")) {
                return url;
            } else if(url.startsWith("/KSBP/")) {
                
            } else if(url.startsWith("2014now_busStation/")) {
                return url;
            } else if(url.startsWith("/ZTTraffic/")) {

            } else if(url.startsWith("/Transport/")) {

            } else if(url.startsWith("/ZTTransport/")) {

            } else if(url.startsWith("/ConstructMonitor/")) {

            } else if (url.startsWith("/KSTransport/")) {

            } else if(url.startsWith("/")) {
                url = "/ZTTraffic" + url;
            } else {
                url = "/ZTTraffic/" + url;
            }
            return url;
        },
       dealwithImage: function(url) {
            var url = $.trim(url);
            if(url.startsWith("http://")) {
                return url;
            } else if(url.startsWith("https://")) {
                return url;
            } else if(url.startsWith("/images/")) {
                return url;
            } else if(url.startsWith("/mapimages/")) {
                return url;
            } else if(url.startsWith("/mapserver/")) {
                return url;
            } else if(url.startsWith("/wcs?")) {    
                return url;
            } else if(url.startsWith("/KSBP/")) {
                
            } else if(url.startsWith("/ZTTraffic/")) {
                
            } else if(url.startsWith("/")) {
                url = "/ZTTraffic" + url;
            } else {
                url = "/ZTTraffic/" + url;
            }
            if(works.test) {
                var pos = url.indexOf("?");
                if(pos >= 0) {
                    url = url.substring(0,pos);
                }
                var pos = url.lastIndexOf("/");
                if(pos) {
                    url = url.substring(pos + 1);
                }
            }
            return url;
        },
       dealwithWebsocketUrl: function(url) {
           if(!works.appName) {
               works.appName = "/";
           }
            var url = $.trim(url);
            if(url.startsWith(works.appName)) {
                
            } else if(url.startsWith("/")) {
                url = works.appName + url;
            } else {
                url = works.appName + "/" + url;
            }
            return "ws://" + window.location.host + url;
        },
        buildRegex: function(strPattern) {
           var regParts = strPattern.match(/^\/(.*?)\/([gim]*)$/);
           var regexp;
           if (regParts) {
               // the parsed pattern had delimiters and modifiers. handle them. 
               regexp = new RegExp(regParts[1], regParts[2]);
           } else {
               // we got pattern string without delimiters
               regexp = new RegExp(inputstring);
           }
           return regexp;
       },
       shake: function(obj,Rate,speed) {
            var oL=obj.offsetLeft;
            var oT=obj.offsetTop;
            this.stop=null;
            this.oTime=null;
            var om=this;
            this.start=function(){
                if(parseInt(obj.style.left) == oL - 2) {
                    obj.style.top = oT + 2 + "px";
                    setTimeout(function() {
                        obj.style.left = oL + 2 + "px"
                    }, Rate)
                }
                else {
                    obj.style.top = oT - 2 + "px";
                    setTimeout(function() {
                        obj.style.left = oL - 2 + "px"
                    }, Rate);
                }
                   this.oTime=setTimeout(function(){om.start()},speed);
            };
            this.stop=function(){
              clearTimeout(this.oTime);
            };
        },
        openWaitDialog:function() {
            if(!jModalSearching) {
                var jPops = $("body").find(">#pops");
                if(jPops.length === 0) {
                    jPops = $("<div id='pops'></div>");
                    $("body").append(jPops);
                }
                
                var modelDiv = ['<div id="modelPopLayer">',
                    ' <div id="modalSearching">',
                    '   <img src="/images/searching.gif"> Searching......',
                    ' </div>',
                    '</div>'
                ];
                jModalSearching = $(modelDiv.join(""));
                jPops.append(jModalSearching);
            }
            jModalSearching.show();
        },

        closeWaitDialog: function() {
            if(jModalSearching) {
                jModalSearching.hide();
            }
        },
        
        openPopPanel:function(popId,style) {
            var self = this;
            var jPop = $(popId);
            var jCloseButtons = jPop.find(".closePop");
            for(var k =0;k<jCloseButtons.length;k++) {//防止重复注册click function
                var closeButton = jCloseButtons.get(k);
                if(closeButton) {
                    if(!closeButton.closeFunc) {
                        closeButton.closeFunc = true;
                       $(closeButton).click(function() {
                           self.closePopPanel(popId);
                       });
                    }
                }
            }
            if(!jGlobalPopLayer) {
                var jPops = $("body").find(">#pops");
                if (jPops.length === 0) {
                    jPops = $("<div id='pops'></div>");
                    $("body").append(jPops);
                }
                jGlobalPopLayer = jPops.find(">#popLayer");
                if (jGlobalPopLayer.length === 0) {
                    jGlobalPopLayer = $("<div id='global-pop-layer'></div>");
                    jPops.append(jGlobalPopLayer);
                }
            }

            var modal_height = jPop.outerHeight();
            var modal_width = jPop.outerWidth();
            jGlobalPopLayer.css({"display": "block", opacity: 0});
            jGlobalPopLayer.fadeTo(200, 0.5);
            var popStyle = {"display": "block", "position": "fixed", "opacity": 0, "z-index": 11000, "left": 50 + "%", "margin-left": -(modal_width / 2) + "px", "top": 50 + "%","margin-top":-(modal_height / 2) + "px"};
            
            if(style) {
                $.extend(popStyle,style);
            }
            jPop.css(popStyle);
            jPop.fadeTo(200, 1);
           
            var jDownlinks = jPop.find(".downloadLink");
            for(var i = 0;i<jDownlinks.length;i++) {
               jDownlinks.eq(i).width(jDownlinks.eq(i).parent().width());
            }
        },
        closePopPanel: function(popId) {
            if(jGlobalPopLayer) {
                jGlobalPopLayer.fadeOut(200);
            }
            $(popId).hide();
        },
        download: function(url) {
            if(!url) {
                return;
            }
            if(!jDownloadFrame) {
                var jPops = $("body").find(">#pops");
                if (jPops.length === 0) {
                    jPops = $("<div id='pops'></div>");
                    $("body").append(jPops);
                }
                jDownloadFrame = jPops.find(">#downloadFrame");
                if (jDownloadFrame.length === 0) {
                    jDownloadFrame = $("<iframe style='display: none;'></iframe>");
                    jPops.append(jDownloadFrame);
                }
            }
            jDownloadFrame.attr("src",url);
        }       
    };

    function getPageCoord(element) {   //计算从触发到root间所有元素的offsetLeft值之和,被getOffset方法使用。
        var coord = {x: 0, y: 0};
        while (element)
        {
            coord.x += element.offsetLeft;
            coord.y += element.offsetTop;
            element = element.offsetParent;
        }
        return coord;
    }
})();


