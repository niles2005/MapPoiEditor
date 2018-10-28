var works = window.works = {};


if (!Array.prototype.indexOf)
{
  Array.prototype.indexOf = function(elt /*, from*/)
  {
    var len = this.length >>> 0;

    var from = Number(arguments[1]) || 0;
    from = (from < 0)
         ? Math.ceil(from)
         : Math.floor(from);
    if (from < 0)
      from += len;

    for (; from < len; from++)
    {
      if (from in this &&
          this[from] === elt)
        return from;
    }
    return -1;
  };
}

String.prototype.endsWith = function(str) {
    if(str == null || str == "" || this.length < str.length) {
        return false;	
    }
    return (this.substring(this.length - str.length) == str);
}

String.prototype.startsWith = function(str) {
    if(str==null || str=="" || this.length < str.length) {
        return false;
    }		
    return (this.substring(0,str.length) == str);
}

String.prototype.replaceAll = function(s1,s2) { 
    return this.replace(new RegExp(s1,"gm"),s2); 
}

//Array Remove - By John Resig (MIT Licensed)   
Array.remove = function(array, from, to) {   
    var rest = array.slice((to || from) + 1 || array.length);   
    array.length = from < 0 ? array.length + from : from;   
    return array.push.apply(array, rest);   
};

function createGuid() {
    function S4() {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    }

    // then to call it, plus stitch in '4' in the third group
    guid = (S4() + S4() + "-" + S4() + "-4" + S4().substr(0, 3) + "-" + S4() + "-" + S4() + S4() + S4()).toLowerCase();
    return guid;
}

function setCookie(name,value,expireDays){
	var exp  = new Date();  
	if(expireDays) {
		exp.setTime(exp.getTime() + expireDays*24*60*60*1000);
	} else {
		exp.setTime(exp.getTime() + 30*24*60*60*1000);//缺省30天
	}
	document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();
}
function getCookie(name){
	var arr = document.cookie.match(new RegExp("(^| )"+name+"=([^;]*)(;|$)"));
	 if(arr != null) return unescape(arr[2]); return null;
}
function delCookie(name){
	var exp = new Date();
	exp.setTime(exp.getTime() - 1);
	var cval=getCookie(name);
	if(cval!=null) document.cookie= name + "="+cval+";expires="+exp.toGMTString();
}			


(function() {
    function Base() {
    }

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
        }
    };
})();
