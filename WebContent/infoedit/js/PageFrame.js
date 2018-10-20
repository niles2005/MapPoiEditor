(function() {
    works.PageFrame = PageFrame;

    var EXTEND = null;

    function PageFrame() {
        if(EXTEND) {
            EXTEND.apply(this,arguments);
        }
    }
    
    PageFrame.prototype = {
        initFrame: function() {
            this._c = document.getElementById("c");
            this._n = document.getElementById("n");
            this._w = document.getElementById("w");
            this._e = document.getElementById("e");
            this._s = document.getElementById("s");


            var self = this;
            var resetWindow = function() {
                self.resetSize();
            }
            window.onload = resetWindow;
            window.onresize = resetWindow;
        },
        isEnableW: function() {
          return this._w.style.display != "none";
        },
        disableW: function() {
            this._w.style.display = "none";
            this._w_hsp.style.display = "none";
            this.resetSize();
        },
        enableW: function(width) {
            if(width) {
                this._w.style.width = width + "px";
            }
            this._w.style.display = "block";
            this._w_hsp.style.left = (this._w.offsetWidth + 2) + "px";
            this._w_hsp.style.display = "block";
            this.resetSize();
        },
        isEnableE: function() {
          return this._e.style.display != "none";
        },
        disableE: function () {
            this._e.style.display = "none";
            this._e_hsp.style.display = "none";
            this.resetSize();
        },
        enableE: function (width) {
            if(width) {
                this._e.style.width = width + "px";
            }
            
            this._e.style.display = "block";
            this._e_hsp.style.right = this._e.offsetWidth + "px";
            this._e_hsp.style.display = "block";
            this.resetSize();
        },
        resetSize: function () {
            var cOffsetTop = this._c.offsetTop;
            var marginLeft = 0;
            this._c.style["marginLeft"] = marginLeft + "px";
            
            var marginRight = 0;
            this._c.style["marginRight"] = marginRight + "px";
            
            var newHeight = works.utils.getWindowHeight() - cOffsetTop - this._s.offsetHeight;
//            this._w.style.height = newHeight + "px";
//            this._e.style.height = newHeight + "px";
            this._c.style.height = newHeight + "px";
//            this._w_hsp.style.height = newHeight + "px";
//            this._e_hsp.style.height = newHeight + "px";
            this.onWindowResize();
        },
        onWindowResize: function() {
            
        }
    }


    function onMouseDown(event) {
        if(event.stopPropagation) {
            event.stopPropagation();
            event.preventDefault();
        } else {//IE
            event.cancelBubble = true;
            event.returnValue = false;
        }

        var webFrame = this;
        var dragDiv = webFrame._dd;
        
        var split = event.target;
        if(!split) {
            split = event.srcElement;
        }
            
        var offsetSplit = event.clientX - split.offsetLeft;
            
        dragDiv.style.left = split.offsetLeft + "px";
        dragDiv.style.top = split.offsetTop + "px";
        dragDiv.style.width = split.offsetWidth + "px";
        dragDiv.style.height = split.offsetHeight + "px";
        dragDiv.style.display = "block";
            
        document.body.style.cursor="ew-resize";

        var documentWidth = document.width;
            
        function onMouseMove(event) {
            if(event.stopPropagation) {
                event.stopPropagation();
                event.preventDefault();
            } else {//IE
                event.cancelBubble = true;
                event.returnValue = false;
            }
            var xx = event.clientX - offsetSplit;
            if(xx > documentWidth) {
                xx = documentWidth;
            }
            dragDiv.style.left = xx + "px";
        }
        function onMouseUp(event) {
            var xx;
            if(event.stopPropagation) {
                event.stopPropagation();
                event.preventDefault();
            } else {//IE
                event.cancelBubble = true;
                event.returnValue = false;
            }
            if(split.id == "w_hsp") {
                xx = event.clientX - offsetSplit - 2;
                webFrame.enableW(xx);
            } else if(split.id == "e_hsp") {
                xx = event.clientX - offsetSplit + 2+ split.offsetWidth;
                xx = window.innerWidth - xx;
                webFrame.enableE(xx);
            }
                
            document.body.style.cursor="default";
            works.utils.unbindEvent(document, "mousemove",onMouseMove);
            works.utils.unbindEvent(document, "mouseup",onMouseUp);
            dragDiv.style.display = "none";
        }
        works.utils.bindEvent(document, "mousemove",onMouseMove);
        works.utils.bindEvent(document, "mouseup",onMouseUp);
    }
    
    if(EXTEND) {
        works.utils.inherits(PageFrame, EXTEND);
    }
})();

