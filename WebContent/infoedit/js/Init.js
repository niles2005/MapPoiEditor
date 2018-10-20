var works = window.works = {};

works.ajaxtype = 0; //0:default,use jquery ajax,   1: use jsonP
works.actions = {};


if(!window.console) {
    window.console = {};
    window.console.log = function(){};
    window.console.dir = function(){};
}
works.isIE = (!!window.ActiveXObject || "ActiveXObject" in window) ;
works.IEVersion = 0;
if(works.isIE) {
    if(navigator.appName === "Microsoft Internet Explorer") {
        var pos = navigator.appVersion.indexOf("MSIE ");
        if(pos >= 0) {
            var pos1 = navigator.appVersion.indexOf(".",pos + 5);
            works.IEVersion = parseInt(navigator.appVersion.substring(pos + 5,pos1));
            works.isIELess9 = works.IEVersion < 9;
        }
    } else {
        if(!!window.MSStream) {
            works.IEVersion = 11;
        }
    }
}

works.isFirefox = navigator.userAgent.indexOf('Firefox') >= 0;
works.isOpera = navigator.userAgent.indexOf('Opera') >= 0 ;

works.supportSVG = document.createElementNS != null;

if(works.isIELess9) {//VML
        // document.namespaces.add("v", "urn:schemas-microsoft-com:vml");
        // var style = document.createStyleSheet();
        // var VMLel = ['line','stroke','path','polyline','fill','oval','shape'];
        // for (var i=0,l=VMLel.length;i<l;i++) {
        //         style.addRule('v\\:'+VMLel[i], "behavior: url(#default#VML);");
        //         style.addRule('v\\:'+VMLel[i], "antialias: true;");
        // }
   if (document.namespaces['v'] == null) {
       var e = ["shape", "shapetype", "group", "background", "path", "formulas", "handles", "fill", "stroke", "shadow", "textbox", "textpath", "imagedata", "line", "polyline", "curve", "roundrect", "oval", "rect", "arc", "image"], s = document.createStyleSheet();
       for (var i = 0; i < e.length; i++) {
           s.addRule("v\\:" + e[i], "behavior: url(#default#VML); display: inline-block;");
       }
       document.namespaces.add("v", "urn:schemas-microsoft-com:vml");
   }
}

works.alert = function(message,func) {
    new works.MyAlert().hiAlert(message,null,func);
    
//    alert(message);
}

works.myConfirmAlert = function(message,func) {
    new works.MyAlert().hiMyConfirm(message,null,func);
}

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


function printMap(printpage)
{
	window.print(); 
return false;
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
