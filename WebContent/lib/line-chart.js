var chartwork = window.chartwork = {};

chartwork.buildChart = {};


(function() {
    function Base() {
    }

    chartwork.utils = {
        inherits: function(subClass, superClass) {
            var sub = subClass.prototype;
            Base.prototype = superClass.prototype;
            var sup = new Base();
            Base.prototype = null;
            for (var prop in sub) {
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
        isArray: function(obj) {
            return Object.prototype.toString.call(obj) === '[object Array]';
        },
        isIELess9: function() {
            var isIE = (!!window.ActiveXObject || "ActiveXObject" in window) ;
            var IEVersion = 0;
            if(isIE) {
                if(navigator.appName === "Microsoft Internet Explorer") {
                    var pos = navigator.appVersion.indexOf("MSIE ");
                    if(pos >= 0) {
                        var pos1 = navigator.appVersion.indexOf(".",pos + 5);
                        var IEVersion = parseInt(navigator.appVersion.substring(pos + 5,pos1));
                        return IEVersion < 9;
                    }
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
        getPageX: function(event) {
            if (event.pageX || event.pageY) {
                return event.pageX;
            } else {
                return event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
            }
        },
        getPageY: function(event) {
            if (event.pageX || event.pageY) {
                return event.pageY;
            } else {
                return event.clientY + document.documentElement.scrollTop;
            }
        }        
    }
})();




(function() {
    chartwork.BaseChart = BaseChart;

    var EXTEND = null;
    
    chartwork.drawChart = function(canvas,data,titleDiv) {
        if(chartwork.utils.isIELess9() && window.G_vmlCanvasManager){
            window.G_vmlCanvasManager.initElement(canvas);            
        }
        var ctx;
        if (canvas.getContext) {
            ctx = canvas.getContext("2d");
        }

        if(ctx && data) {
            
            var type = data["type"]; 
            if(type) {
                if(chartwork.buildChart[type]) {
                    return chartwork.buildChart[type](ctx,titleDiv,data,canvas);
                }
            }
        }
    };

    function BaseChart(ctx,legendDiv,chartData,canvas) {
        if(EXTEND) {
            EXTEND.apply(this,arguments);
        }
        this._canvas = canvas;
        this._ctx = ctx;
        this._legendDiv = legendDiv;
        this._chartData = chartData;
        
        this.checkUndefinedValues();
        
        this._x = this._chartData.x || 0;
        this._y = this._chartData.y || 0;
        this._reverseXYAxis = this._chartData["reverse-x-y-axis"];
        this._bgColor = this._chartData["bg-colour"];
        this._gridX = this._chartData["grid-margin-left"];
        this._gridY = this._chartData["grid-margin-top"];
        
        this._legend = this._chartData["legend"];
        this._keys = this._chartData["keys"];

        this._gridW = this._canvas.width - this._chartData["grid-margin-left"] - this._chartData["grid-margin-right"];
        this._gridH = this._canvas.height - this._chartData["grid-margin-top"] - this._chartData["grid-margin-bottom"];

        this._xAxis = this._chartData["x-axis"];
        if(this._xAxis) {
            this._xLabels = this._xAxis["labels"];
            this._xMin = this._xAxis["min"];
            this._xMax = this._xAxis["max"];
            if(this._xMin === undefined || this._xMax === undefined) {
                if(!this._xLabels[0]) {
                }
                this._xMin = this._xLabels[0].x;
                this._xMax = this._xLabels[this._xLabels.length - 1].x;
            }
            //处理柱子宽度，解决label挤在一起的情况
            if(this._xAxis["minXSpace"] && this._gridW / (this._xMax - this._xMin) < this._xAxis["minXSpace"]) {
                this._xMax = this._gridW / this._xAxis["minXSpace"] + this._xMin;
            }
        }
        this._yAxis = this._chartData["y-axis"];
        if(this._yAxis) {
            this._yLabels = this._yAxis["labels"];

            this._yMin = this._yAxis["min"];
            this._yMax = this._yAxis["max"];
            if(this._yMin === undefined || this._yMax === undefined) {
                this._yMin = this._yLabels[0].y;
                this._yMax = this._yLabels[this._yLabels.length - 1].y;
            }
        }
        
        this.checkChartDataValue("bar-width",1);
        this._barWidthRate = this._chartData["bar-width"];
        
        this._xOffset = 0;
        var self = this;
        if(this._chartData["dragXAxis"]) {
            chartwork.utils.bindEvent(this._canvas, 'mousedown', function(event) {
                self.onMouseDown.call(self, event);
            });
        }
    }

    
    BaseChart.prototype = {
        update: function() {
            this._xAxis = this._chartData["x-axis"];
            if(this._xAxis) {
                this._xLabels = this._xAxis["labels"];
                this._xMin = this._xAxis["min"];
                this._xMax = this._xAxis["max"];
                if(this._xMin === undefined || this._xMax === undefined) {
                    if(!this._xLabels[0]) {
                    }
                    this._xMin = this._xLabels[0].x;
                    this._xMax = this._xLabels[this._xLabels.length - 1].x;
                }
                //处理柱子宽度，解决label挤在一起的情况
                if(this._xAxis["minXSpace"] && this._gridW / (this._xMax - this._xMin) < this._xAxis["minXSpace"]) {
                    this._xMax = this._gridW / this._xAxis["minXSpace"] + this._xMin;
                }
            }
            this._yAxis = this._chartData["y-axis"];
            if(this._yAxis) {
                this._yLabels = this._yAxis["labels"];

                this._yMin = this._yAxis["min"];
                this._yMax = this._yAxis["max"];
                if(this._yMin === undefined || this._yMax === undefined) {
                    this._yMin = this._yLabels[0].y;
                    this._yMax = this._yLabels[this._yLabels.length - 1].y;
                }
            }
        },
        resetSize: function() {
            this._gridW = this.getWidth() - this._chartData["grid-margin-left"] - this._chartData["grid-margin-right"];
            this._gridH = this.getHeight() - this._chartData["grid-margin-top"] - this._chartData["grid-margin-bottom"];
            
            this._xAxis = this._chartData["x-axis"];
            if(this._xAxis) {
                this._xLabels = this._xAxis["labels"];
                this._xMin = this._xAxis["min"];
                this._xMax = this._xAxis["max"];
                if(this._xMin === undefined || this._xMax === undefined) {
                    if(!this._xLabels[0]) {
                    }
                    this._xMin = this._xLabels[0].x;
                    this._xMax = this._xLabels[this._xLabels.length - 1].x;
                }
                //处理柱子宽度，解决label挤在一起的情况
                if(this._xAxis["minXSpace"] && this._gridW / (this._xMax - this._xMin) < this._xAxis["minXSpace"]) {
                    this._xMax = this._gridW / this._xAxis["minXSpace"] + this._xMin;
                }
            }
            this._yAxis = this._chartData["y-axis"];
            if(this._yAxis) {
                this._yLabels = this._yAxis["labels"];

                this._yMin = this._yAxis["min"];
                this._yMax = this._yAxis["max"];
                if(this._yMin === undefined || this._yMax === undefined) {
                    this._yMin = this._yLabels[0].y;
                    this._yMax = this._yLabels[this._yLabels.length - 1].y;
                }
            }
            this.drawChart();
        },
        getWidth: function() {
            return this._chartData.width || this._canvas.width;
        },        
        getHeight: function() {
            return this._chartData.height || this._canvas.height;
        },
        checkUndefinedValues: function() {
            this.checkChartDataValue("grid-margin-left",0);
            this.checkChartDataValue("grid-margin-right",0);
            this.checkChartDataValue("grid-margin-top",0);
            this.checkChartDataValue("grid-margin-bottom",0);
        },
        //初始化未定义的字段的值
        checkChartDataValue: function(name,defaultValue) {
            if(this._chartData[name] === undefined) {
                this._chartData[name] = defaultValue;
            }
        },
        setLineDash: function(dash) {
            if ( this._ctx.setLineDash !== undefined ) {//for chrome
                if(dash) {
                    this._ctx.setLineDash(dash);
                }
            } else if(this._ctx.mozDash !== undefined){//for firefox
                this._ctx.mozDash = dash;
            }
        },
        setFocusXPos: function(focusXPos,focusText) {
            this._focusXPos = focusXPos;
            this._focusText = focusText;
        },
        getXPos: function (value,reverseXYAxis) {
            var rate = 1.0 * (value - this._xMin) / (this._xMax - this._xMin);
            if(reverseXYAxis) {
                return this._gridY + this._gridH - rate * this._gridH + this._xOffset;
            } else {
                return this._gridX + rate * this._gridW + this._xOffset;
            }
        },
        getYPos: function (value,reverseXYAxis) {
            if(value === '' || value === undefined) {
                return undefined;
            }
            var rate = 1.0 * (value - this._yMin) / (this._yMax - this._yMin);
            if(reverseXYAxis) {
                return this._gridX + rate * this._gridW;
            } else {
                return this._gridY + this._gridH - rate * this._gridH;
            }
        },
        drawChart: function() {
        },
        strokeLine: function(x1,y1,x2,y2) {
            this._ctx.beginPath();
            this._ctx.moveTo(x1,y1);
            this._ctx.lineTo(x2,y2);
            this._ctx.stroke();
        },
        strokeCircle: function(x,y,radius) {
            if(radius < 0) {
                return;
            }
            this._ctx.beginPath();
            this._ctx.arc(x, y, radius, 0, 2 * Math.PI);
            this._ctx.stroke();
        },
        fillCircle: function(x,y,radius) {
            if(radius < 0) {
                return;
            }
            this._ctx.beginPath();
            this._ctx.arc(x, y, radius, 0, 2 * Math.PI);
            this._ctx.fill();
        },
        drawLabel: function(x,y,text,scale) {
            if(!text && text !== 0) {
                return;
            }
            this._ctx.translate(x,y);
            if(scale) {
                this._ctx.scale(scale, scale);
            }
            this._ctx.fillText(text,0,0);
            if(scale) {
                this._ctx.scale(1.0 / scale, 1.0 / scale);
            }
            this._ctx.translate(-x,-y);
         },
        drawLabels: function(x,y,labels,scale,labelHeight) {
            if(!labels) {
                return;
            }
            this._ctx.translate(x,y);
            if(scale) {
                this._ctx.scale(scale, scale);
            }
            var yPos = 0;
            for(var i=0;i<labels.length;i++) {
                this._ctx.fillText(labels[i],0,yPos);
                yPos += labelHeight;
            }
            if(scale) {
                this._ctx.scale(1.0 / scale, 1.0 / scale);
            }
            this._ctx.translate(-x,-y);
         },
         drawBackground: function() {
            if(this._bgColor) {
                this._ctx.fillStyle = this._bgColor;
                this._ctx.fillRect(this._x,this._y,this.getWidth(),this.getHeight());
            } else {//无背景色时，做透明
                this._ctx.clearRect(this._x,this._y,this.getWidth(),this.getHeight());
            }
            if(this._chartData["grid-bg-colour"]) {
                this._ctx.fillStyle = this._chartData["grid-bg-colour"];
                this._ctx.fillRect(this._gridX,this._gridY,this._gridW,this._gridH);
            }
         },
         clearBackground: function() {
            this._ctx.clearRect(0,0,this.getWidth(),this.getHeight());
         },
         drawLegend: function() {
            if(!this._legend) {
                return;
            }
            var visible = this._legend["visible"];
            if(visible && this._legendDiv) {
                var jLegendDiv = $(this._legendDiv);
                jLegendDiv.empty();
                var icon = this._legend["icon"];
                
                for(var k =0;k<this._keys.length;k++) {
                    var key = this._keys[k];
                    var colour = key["colour"];
                    var text = key["text"] || key["label"];
                    var labelHtml = "<i style='display: inline-block;width: 23px;height: 16px;vertical-align: top;background-image: url(/images/" + icon + ".png);background-repeat: no-repeat;background-color:" + colour + "'></i><span>" + text + "</span>";
                    jLegendDiv.append(labelHtml);
                }
            }
         },        
         getXLabel: function(xLabelName) {
            if(this._xAxis.labelHash) {
                return this._xAxis.labelHash[xLabelName];
            }
            return null;
         },
         drawXAxisAndLabels: function() {
            if(this._xLabels) {
                 this._ctx.lineWidth = 1;
                this._ctx.save();
                var theFont = this._xAxis["font"];
                if(theFont) {
                    this._ctx.font = theFont;
                }
                var fontScale = this._xAxis["font-scale"];
                if(!fontScale) {
                    fontScale = 1.0;
                }
                var poleLength = this._xAxis["pole-length"];
                if(!poleLength) {
                    poleLength = 0;
                }
                var colour = this._xAxis["colour"];
                
                var xNum = this._xLabels.length;

                var xSpace;
                if(this._reverseXYAxis) {
                    xSpace = 1.0 * this._gridH / (xNum + 1);
                } else {
                    xSpace = 1.0 * this._gridW / (xNum + 1);
                }
                
                this._subXBarWidth = xSpace * this._barWidthRate * 0.5;
                
                var y0 = this._gridY;
                var y1 = this._gridY + this._gridH;
                if(this._reverseXYAxis) {
                    y0 = this._gridX;
                    y1 = this._gridX + this._gridW;
                }
                
                var gridVisible = this._xAxis["grid-visible"];
                if(gridVisible === undefined) {//default show
                   gridVisible = true; 
                }
                
                if(!this._xAxis["label-height"]) {
                    this._xAxis["label-height"] = 18;
                }
                this._xAxis.labelHash = {};
                for(var i=0;i<xNum;i++) {
                    var xLabel = this._xLabels[i];
                    var showLabel = xLabel["show"];
                    var xGridVisible = gridVisible;
                    if(!showLabel && gridVisible === "onlyShow") {
                        xGridVisible = false;
                    }
                        
                    this._xAxis.labelHash[xLabel.text] = xLabel;
                    var xx = parseInt(this.getXPos(xLabel.x,this._reverseXYAxis)) + 0.5;
                    xLabel["xPos"] = xx;
                    if(xGridVisible) {
                        var fillStyle = xLabel["fill-style"];
                        if(fillStyle) {
                            this._ctx.fillStyle = fillStyle;
                            if(this._reverseXYAxis) {
                                this._ctx.fillRect(this._gridX,xx - this._subXBarWidth,this._gridW,this._subXBarWidth * 2);
                            } else {
                                this._ctx.fillRect(xx - this._subXBarWidth,this._gridY,this._subXBarWidth * 2,this._gridH);
                            }
                        }

                        var lineDash = xLabel["line-dash"];
                        this.setLineDash(lineDash);

                        var strokeStyle = xLabel["stroke-style"];
                        if(strokeStyle) {
                            this._ctx.strokeStyle = strokeStyle;
                            if(this._reverseXYAxis) {
                                this.strokeLine(this._gridX,xx,this._gridX + this._gridW + poleLength,xx);
                            } else {
                                this.strokeLine(xx,this._gridY,xx,this._gridY + this._gridH + poleLength);
                            }
                        } else {
                            if(this._xAxis["grid-colour"]) {
                                this._ctx.strokeStyle = this._xAxis["grid-colour"];
                                if(this._reverseXYAxis) {
                                    this.strokeLine(y0,xx,y1,xx);
                                } else {
                                    this.strokeLine(xx,y0,xx,y1);
                                }
                            }
                            if(this._xAxis["pole-colour"]) {
                                if(poleLength > 0) {
                                    this._ctx.strokeStyle = this._xAxis["pole-colour"];
                                    this.strokeLine(xx,y1,xx,y1 + poleLength);
                                }
                            }
                        }
                    }

                    if(showLabel === undefined) {
                        showLabel = true;
                    }
                    if(showLabel) {
                        this._ctx.fillStyle = colour;
                        
                        var label = xLabel["label"];//优先显示label
                        if(label) {//label可能为datetime等长字符串，按空格切分，多行显示
                            label = label.split(" ");
                        }
                        if(!label) {
                            label = xLabel["text"];
                        }
                        if(label || label === 0) {
                            var xx = xLabel["xPos"];
                            this._ctx.textAlign = 'center';
                            if(this._reverseXYAxis) {
                                if(chartwork.utils.isArray(label)) {
                                    this.drawLabels(this._gridX / 2,xx + 3,label,fontScale,this._xAxis["label-height"]);
                                } else {
                                    this.drawLabel(this._gridX / 2,xx + 3,label,fontScale);
                                }
                            } else {
                                if(chartwork.utils.isArray(label)) {
                                    this.drawLabels(xx,y1 + 15,label,fontScale,this._xAxis["label-height"]);
                                } else {
                                    this.drawLabel(xx,y1 + 15,label,fontScale);
                                }
                            }
                        }
                    }
                }
                this._ctx.restore();
            }
        },
        drawYAxisAndLabels: function() {
            if(this._yLabels) {
                 this._ctx.lineWidth = 1;
                var drawLabel = true;
                if(this._yAxis.drawLabel === false) {
                    drawLabel = false;
                }
                
                this._ctx.save();
                
                
                var gridVisible = this._yAxis["grid-visible"];
                if(gridVisible === undefined) {
                   gridVisible = true; 
                }
                
                
                var theFont = this._yAxis["font"];
                if(theFont) {
                    this._ctx.font = theFont;
                }
                var fontScale = this._yAxis["font-scale"];
                if(!fontScale) {
                    fontScale = 1.0;
                }
                
                var yNum = this._yLabels.length;

                var ySpace;
                if(this._reverseXYAxis) {
                    ySpace = Math.ceil(1.0 * this._gridW / (yNum - 1));
                } else {
                    ySpace = Math.ceil(1.0 * this._gridH / (yNum - 1));
                }              
                
                if(this._yAxis["grid-fill-colour"]) {
                    for(var i=0;i<yNum - 1;i++) {
                        if(chartwork.utils.isArray(this._yAxis["grid-fill-colour"])) {
                            if(this._yAxis["grid-fill-colour"][i]) {
                                this._ctx.fillStyle = this._yAxis["grid-fill-colour"][i];
                            }
                        } else {
                            this._ctx.fillStyle = this._yAxis["grid-fill-colour"];
                        }
                        var yLabel = this._yLabels[i];
                    
                        var yValue = yLabel["y"];
                        var yy = parseInt(this.getYPos(yValue,this._reverseXYAxis)) + 0.5;

                        if(gridVisible) {
                            //+1为去除space不取整，造成有边界线的情况
                            if(this._reverseXYAxis) {
                                this._ctx.fillRect(yy,this._gridY,ySpace + 1,this._gridH);
                            } else {
                                this._ctx.fillRect(this._gridX,yy - ySpace - 1 ,this._gridW,ySpace + 1);
                            }
                        }
                    }
                }
                for(var i=0;i<yNum;i++) {
                    var yLabel = this._yLabels[i];
                    
                    var yValue = yLabel["y"];
                    var yy = Math.round(this.getYPos(yValue,this._reverseXYAxis)) + 0.5;
                    if(gridVisible) {
                        var lineDash = yLabel["line-dash"];
                        this.setLineDash(lineDash);

                        if(this._yAxis["grid-colour"]) {
                            this._ctx.strokeStyle = this._yAxis["grid-colour"];
                            if(this._reverseXYAxis) {
                                this.strokeLine(yy,this._gridY,yy,this._gridY + this._gridH);
                            } else {
                                this.strokeLine(this._gridX,yy,this._gridX + this._gridW,yy);
                            }
                        }
                    }
                    
                    if(drawLabel) {
                        var yLabel = this._yLabels[i];
                        var text = yLabel["text"];
                        this._ctx.textAlign = 'center';
                         this._ctx.fillStyle = this._yAxis["colour"];
                         
                         
                        if(this._reverseXYAxis) {
                            this.drawLabel(yy,this._gridY + this._gridH + 15,text,fontScale);
                        } else {
                            this.drawLabel(this._gridX / 2,yy+3,text,fontScale);
                        }
                    }
                }
                this._ctx.restore();
            }
        },
        onMouseDown: function(event) {
            if (event.stopPropagation) {
                event.stopPropagation();
                event.preventDefault();
                if (event.button != 0) {//只能左键，屏蔽右键的拖动
                    return;
                }
            } else {//IE
                event.cancelBubble = true;
                event.returnValue = false;
                if (event.button != 1) {//只能左键，屏蔽右键的拖动
                    return;
                }
            }
            var pageX = chartwork.utils.getPageX(event),
            pageY = chartwork.utils.getPageY(event);
            var bakXOffset = this._xOffset;
            var self = this;
            var draggingDiv = function(event) {
                if (event.stopPropagation) {
                    event.stopPropagation();
                    event.preventDefault();
                } else {//IE
                    event.cancelBubble = true;
                    event.returnValue = false;
                }
                var tx = chartwork.utils.getPageX(event);
                var ty = chartwork.utils.getPageY(event);
                
                self._xOffset = bakXOffset + (tx - pageX);
                self.drawChart();
            }
            var droppedDiv = function(event) {
                if (event.stopPropagation) {
                    event.stopPropagation();
                    event.preventDefault();
                } else {//IE
                    event.cancelBubble = true;
                    event.returnValue = false;
                }
                chartwork.utils.unbindEvent(document, 'mousemove', draggingDiv);
                chartwork.utils.unbindEvent(document, 'mouseup', droppedDiv);

            };
            chartwork.utils.bindEvent(document, 'mousemove', draggingDiv);
            chartwork.utils.bindEvent(document, 'mouseup', droppedDiv);
        }
        
    };
    
    

    if(EXTEND) {
        chartwork.utils.inherits(BaseChart, EXTEND);
    }    
})();
(function() {
    chartwork.LineChart = LineChart;

    var EXTEND = chartwork.BaseChart;

    chartwork.buildChart["line"] = function(ctx,titleDiv,data,canvas) {
        return new chartwork.LineChart(ctx,titleDiv,data,canvas);
    }

    function LineChart() {
        if(EXTEND) {
            EXTEND.apply(this,arguments);
        }
        this.checkChartDataValue("stroke-weight",1);
        this._drawPointRadius = this._chartData["draw-point-radius"];//line线上点的半径（带颜色值），没有的时候，即不画点
        //
        //line线上点的白色半径，一般做外圈或者内环，相对draw-point-radius大时，为外环，小时为内环
        this._drawPointWhiteRadius = this._chartData["draw-point-white-radius"];

        this._strokeWeight = this._chartData["stroke-weight"];//画线的粗细宽度
       this.drawChart();
    }

    LineChart.prototype = {
        drawChart: function() {
            this.drawBackground();
            this.drawLegend();
            this.drawXAxisAndLabels();
            
            this.drawYAxisAndLabels();
            for(var i=0;i<this._keys.length;i++) {
                var key = this._keys[i];
                this.drawPolyline(i,key);
            }    	
            this.drawFocusXPos();
        },
        drawFocusXPos: function() {
            if(this._focusXPos || this._focusXPos === 0) {
                this._ctx.beginPath();
                var yPos0 = this.getYPos(this._yMin,this._reverseXYAxis);
                var yPos1 = this.getYPos(this._yMax,this._reverseXYAxis);
                if(this._reverseXYAxis) {
                    this._ctx.moveTo(yPos0,this._focusXPos);
                    this._ctx.lineTo(yPos1,this._focusXPos);
                } else {
                    this._ctx.moveTo(this._focusXPos,yPos0);                
                    this._ctx.lineTo(this._focusXPos,yPos1);           
                }
                this._ctx.strokeStyle = "red";
                this._ctx.stroke();
                if(this._focusText) {
                    this.drawLabel(this._focusXPos,yPos1,this._focusText,1.0);
                }
            }
        },
        drawPolyline: function(index,key) {
            var colour = key["colour"];
            var name = key["text"] || key["label"];
            
            var thisDrawPointRadius = key["draw-point-radius"];
            if(thisDrawPointRadius === undefined) {
               thisDrawPointRadius = this._drawPointRadius;
            }
            var thisDrawPointWhiteRadius = key["draw-point-white-radius"];
            if(thisDrawPointWhiteRadius === undefined) {
               thisDrawPointWhiteRadius = this._drawPointWhiteRadius;
            }
            this._ctx.strokeStyle = colour;
            this._ctx.lineWidth = this._strokeWeight;
            this._ctx.beginPath();
            
            var self = this;
            var values = this._chartData["values"];
            values.sort(function(x, y) {
                var xLabel1 = self.getXLabel(x[0]);
                var xLabel2 = self.getXLabel(y[0]);
                if(xLabel1 === undefined || xLabel2 === undefined) {
                    return x[0] - y[0];
                }
                return xLabel1.x - xLabel2.x;
            });
            var currIndex = 0;
            if(values) {
                currIndex = values.length;
            }
            var lastArr;
            var isFirstPoint = true;
            
            for (var i = 0; i < values.length; i++) {
                if(i > currIndex) {
                    break;
                }
                var arr = values[i];
                var xLabel = this.getXLabel(arr[0]);
                if(xLabel) {
                    var v = arr[index + 1];//index = 0 is label name
                    if(v) {
                        lastArr = arr;
                    }
                    var yPos = this.getYPos(v,this._reverseXYAxis);
                    if(yPos !== undefined) {
                        var xPos = xLabel["xPos"];
                        if(isFirstPoint) {
                            isFirstPoint = false;
                            if(this._reverseXYAxis) {
                                this._ctx.moveTo(yPos,xPos);
                                if(values.length === 1) {
                                    this._ctx.lineTo(yPos + 1,xPos);
                                }
                            } else {
                                
                                
                                this._ctx.moveTo(xPos,yPos);
                                
                                if(values.length === 1) {
                                    this._ctx.lineTo(xPos + 1,yPos);
                                }
                            }
                        } else {
                            if(this._reverseXYAxis) {
                                this._ctx.lineTo(yPos,xPos);
                            } else {
                                this._ctx.lineTo(xPos,yPos);
                            }
                        }
                    }
                } else {//数值不在整点x坐标轴上
                    var xPos = this.getXPos(arr[0],this._reverseXYAxis);
                    var v = arr[index + 1];//index = 0 is label name
                    if(v) {
                        lastArr = arr;
                    }

                    var yPos = this.getYPos(v,this._reverseXYAxis);
                    if(yPos !== undefined) {
                        if(isFirstPoint) {
                            isFirstPoint = false;
                            if(this._reverseXYAxis) {
                                this._ctx.moveTo(yPos,xPos);
                                if(values.length === 1) {
                                    this._ctx.lineTo(yPos + 1,xPos);
                                }
                            } else {
                                
                                
                                this._ctx.moveTo(xPos,yPos);
                                
                                if(values.length === 1) {
                                    this._ctx.lineTo(xPos + 1,yPos);
                                }
                            }
                        } else {
                            if(this._reverseXYAxis) {
                                this._ctx.lineTo(yPos,xPos);
                            } else {
                                this._ctx.lineTo(xPos,yPos);
                            }
                        }
                    }
                }
            }
            
            this._ctx.stroke();
            
            if(key["fill-colour"]) {
                if(lastArr) {
                    var arr = lastArr;//values[values.length - 1];
                    var xLabel = this.getXLabel(arr[0]);
                    if(xLabel) {
                        var v = arr[index + 1];//index = 0 is label name
                        var yPos = this.getYPos(v,this._reverseXYAxis);
                        if(yPos !== undefined) {
                            var xPos = xLabel["xPos"];
                            this._ctx.lineTo(xPos,this._gridY + this._gridH);
                            this._ctx.lineTo(this._gridX,this._gridY + this._gridH);

                            this._ctx.closePath();

                            this._ctx.fillStyle = key["fill-colour"];
                            this._ctx.fill();
                        }
                    }
                }
            }
            if(this._drawPointRadius || this._drawPointWhiteRadius) {
                for (var i = 0; i < values.length; i++) {
                    if(i > currIndex) {
                        break;
                    }
                    var arr = values[i];
                    var xLabel = this.getXLabel(arr[0],this._reverseXYAxis);
                    if(xLabel) {
                        var v = arr[index + 1];
                        var yPos = this.getYPos(v,this._reverseXYAxis);
                        if(yPos !== undefined) {
                            var xPos = xLabel["xPos"];

                            //判断是否位于预测的带里，可能会画出白色阴影
        //                    var fillStyle = xLabel["fill-style"];
        //                    if(fillStyle) {
        //                        this._ctx.strokeStyle = "rgba(255,255,255,0.7)";
        //                    } else {
        //                        this._ctx.strokeStyle = "white";
        //                    }

                            if(thisDrawPointRadius > thisDrawPointWhiteRadius) {
                                this._ctx.fillStyle = colour;
                                if(this._reverseXYAxis) {
                                    this.fillCircle(yPos,xPos,thisDrawPointRadius);
                                } else {
                                    this.fillCircle(xPos,yPos,thisDrawPointRadius);
                                }
                                if(thisDrawPointWhiteRadius > 0) {
                                    this._ctx.fillStyle = "white";
                                    if(this._reverseXYAxis) {
                                        this.fillCircle(yPos,xPos,thisDrawPointWhiteRadius);
                                    } else {
                                        this.fillCircle(xPos,yPos,thisDrawPointWhiteRadius);
                                    }
                                }
                            } else if(thisDrawPointRadius === thisDrawPointWhiteRadius) {//相等，白环不画
                                this._ctx.fillStyle = colour;
                                if(this._reverseXYAxis) {
                                    this.fillCircle(yPos,xPos,thisDrawPointRadius);
                                } else {
                                    this.fillCircle(xPos,yPos,thisDrawPointRadius);
                                }
                            } else {
                                this._ctx.fillStyle = "white";
                                if(this._reverseXYAxis) {
                                    this.fillCircle(yPos,xPos,thisDrawPointWhiteRadius);

                                    this._ctx.fillStyle = colour;
                                    this.fillCircle(yPos,xPos,thisDrawPointRadius);
                                } else {
                                    this.fillCircle(xPos,yPos,thisDrawPointWhiteRadius);

                                    this._ctx.fillStyle = colour;
                                    this.fillCircle(xPos,yPos,thisDrawPointRadius);
                                }
                            }
                        }
                    }
                }           
            }
        }            
    };

    if(EXTEND) {
        chartwork.utils.inherits(LineChart, EXTEND);
    }    
})();
(function() {
    chartwork.BarGroupChart = BarGroupChart;

    var EXTEND = chartwork.BaseChart;

    chartwork.buildChart["bar-group"] = function(ctx,titleDiv,data,canvas) {
        return new chartwork.BarGroupChart(ctx,titleDiv,data,canvas);
    }

    function BarGroupChart() {
        if(EXTEND) {
            EXTEND.apply(this,arguments);
        }
       this.drawChart();
    }

    BarGroupChart.prototype = {
        drawChart: function() {
            this.drawBackground();
            this.drawLegend();
            this.drawXAxisAndLabels();
            this.drawYAxisAndLabels();
            this.drawBarGroup();
        },
        drawBarGroup: function() {
            var self = this;
            var values = this._chartData["values"];
            values.sort(function(x, y) {
                var xLabel1 = self.getXLabel(x[0]);
                var xLabel2 = self.getXLabel(y[0]);
                return xLabel1.x - xLabel2.x;
            });
            for(var i = 0; i < values.length; i++) {
                var arr = values[i];
                var xLabel = this.getXLabel(arr[0]);
                if(xLabel) {
                    this.drawGroupBarItem(xLabel,arr);
                }                
            }
        },
        drawGroupBarItem: function(xLabel,arr) {
            if(!this._keys.length) {
                return;
            }
            var xPos = xLabel["xPos"];
            var text = xLabel["text"];
            var x = xPos - this._subXBarWidth;
            var w = this._subXBarWidth * 2;
            
            var eachWidth = w / this._keys.length;
            var maxY = this.getYPos(0);
            for(var i=0;i<this._keys.length;i++) {
                var key = this._keys[i];
                var colour = key["colour"];
                var name = key["text"] || key["label"];
                var theValue = parseFloat(arr[i + 1]);
                var y = this.getYPos(theValue) + 0.5;
//                var label = text + ":" + name + ":" + theValue;

                var h = maxY - y;
                this._ctx.fillStyle = colour;
                this._ctx.fillRect(x + 0.5,y,eachWidth - 1,h);
                x += eachWidth;
            }
        }
    };

    if(EXTEND) {
        chartwork.utils.inherits(BarGroupChart, EXTEND);
    }    
})();
(function() {
    chartwork.ChartJsonBuilder = {
        buildChartData: function(chartData,chartValue) {
            if(chartData._function) {
                if(chartwork.utils.isArray(chartData._function)) {
                    for(var i=0;i<chartData._function.length;i++) {
                        var func = chartData._function[i];
                        if(chartwork.ChartJsonBuilder[func]) {
                            chartwork.ChartJsonBuilder[func](chartData,chartValue);
                        }
                    }
                } else if(typeof chartData._function === 'string') {
                    if(chartwork.ChartJsonBuilder[chartData._function]) {
                        chartwork.ChartJsonBuilder[chartData._function](chartData,chartValue);
                    }
                }
            }
        },
        
        buildValues: function(chartData,chartValue) {
            if(chartValue) {
                var startTime = chartValue["startTime"];
                var isTimeField = false;//通过是否有startTime，判断是否为时间
                if(startTime) {
                    isTimeField = true;
                }
                var xFieldName = chartData["x-axis"]["field-name"];
                var xFieldType = chartData["x-axis"]["field-type"];
                
                if(chartValue.result) {
                    chartValue = chartValue.result;
                }
                chartData["values"] = [];
                var yFields = chartData["y-axis"]["field-name"];
                if(typeof yFields === 'string') {
                    yFields = yFields.split(",");
                }
                
                for(var i=0;i<chartValue.length;i++) {
                    var item = chartValue[i];
                    var arr = [];
                    arr[0] = item[xFieldName];
                    for(var j=0;j<yFields.length;j++) {
                        arr[j + 1] = item[yFields[j]];
                    }
                    chartData["values"].push(arr);
                }
            }
        },
        buildXAxis: function(chartData,chartValue) {
            if(!chartValue) {
                return;
            }
            var xFieldName = chartData["x-axis"]["field-name"];
            var xFieldType = chartData["x-axis"]["field-type"];
            
            var hasForecast = false;
            var meetEndTime = false;
            var resultArr = chartValue;
            if(chartValue["result"]) {
                resultArr = chartValue["result"];
            }
            var startTime = chartValue["startTime"];
            var endTime = chartValue["endTime"];
            if(resultArr) {
                for(var i=0;i<resultArr.length;i++) {   
                        var item = resultArr[i];
                        if(item) {
                                var xValue = item[xFieldName];
                                if(xFieldType === "time") {
                                    if("预测" === xValue) {
                                        hasForecast = true;
                                    }
                                    if(endTime === xValue) {
                                        meetEndTime = true;
                                    }
                                }
                        }
                }
            }


            
            var xAxis = {};
            var labels = [];
            xAxis["labels"] = labels;
            
            var showInterval = chartData["x-axis"]["show-interval"];
            if(!showInterval) {
                showInterval = 1;
            }
            
            if("time" === xFieldType) {//x轴是时间
                var timeStep = chartData["x-axis"].stepTime;//5 min
                if(!timeStep) {
                    timeStep = 5;
                }
                var startSeconds = chartwork.TimeUtil.getGlobalSecond(startTime);
                var endSeconds = chartwork.TimeUtil.getGlobalSecond(endTime);
                if(timeStep <= 0) {
                    timeStep = 5;
                }
                var stepTime = timeStep*60;
                var index = 0;
                if(meetEndTime) {
                    if(startSeconds <= endSeconds) {
                        for(var i=startSeconds;i<=endSeconds;i += stepTime) {
                            var theTime = chartwork.TimeUtil.getHourMinute(i);
                            var item = {"x":index,"text":theTime,"show":(index % showInterval === 0)};
                            if(item.show) {
                                item.label = chartwork.TimeUtil.formatTime(theTime);
                            }
                            labels.push(item);
                            index++;
                        }
                    } else {//跨天的情况
                        var lastTime = chartwork.TimeUtil.getGlobalSecond("23:55");
                        for(var i=startSeconds;i<=lastTime;i += stepTime) {
                                var theTime = chartwork.TimeUtil.getHourMinute(i);
                                var item = {"x":index,"text":theTime,"show":(index % showInterval === 0)};
                                if(item.show) {
                                    item.label = chartwork.TimeUtil.formatTime(theTime);
                                }
                                labels.push(item);
                                index++;
                        }
                        var firstTime = chartwork.TimeUtil.getGlobalSecond("00:00");
                        for(var i=firstTime;i<=endSeconds;i += stepTime) {
                                var theTime = chartwork.TimeUtil.getHourMinute(i);
                                
                                var item = {"x":index,"text":theTime,"show":(index % showInterval === 0)};
                                if(item.show) {
                                    item.label = chartwork.TimeUtil.formatTime(theTime);
                                }
                                labels.push(item);
                                index++;
                        }
                    }
                    if(hasForecast) {
                        labels.push({"x":index,"text":"预测","show":(index % showInterval === 0),"fill-style":"rgba(255,0,0,0.15)","stroke-style":"#F00000","line-dash":[4,2]});
                        index++;
                    }
                } else {
                    for(var i=startSeconds;i<=endSeconds;i += stepTime) {
                        var theTime;
                        if(timeStep === 60) {
                            theTime = chartwork.TimeUtil.getHour(i);
                        } else {
                            theTime = chartwork.TimeUtil.getHourMinute(i);
                        }

                        if(i === endSeconds) {
                            if(hasForecast) {
                                labels.push({"x":index,"text":"预测","show":(index % showInterval === 0),"fill-style":"rgba(255,0,0,0.15)","stroke-style":"#F00000","line-dash":[4,2]});
                            } else {
                                var item = {"x":index,"text":theTime,"show":(index % showInterval === 0)};
                                if(item.show) {
                                    item.label = chartwork.TimeUtil.formatTime(theTime);
                                }
                                labels.push(item);
                            }
                        } else {
                            var item = {"x":index,"text":theTime,"show":(index % showInterval === 0)};
                            if(item.show) {
                                item.label = chartwork.TimeUtil.formatTime(theTime);
                            }
                            labels.push(item);
                        }
                        index++;
                    }
                }
                if(chartData["x-axis"]["offset-sub-column"]) {
                    xAxis["min"] = 0 - 0.5;
                    xAxis["max"] = index - 0.5;
                } else {
                    xAxis["min"] = 0;
                    xAxis["max"] = index - 1;
                }
                if(chartData["x-axis"]["extands-width"]) {
                    xAxis["min"] -= chartData["x-axis"]["extands-width"];
                    xAxis["max"] += chartData["x-axis"]["extands-width"];
                }

            } else {
                for(var i=0;i<resultArr.length;i++) {   
                    var item = resultArr[i];
                    if(item) {
                        var xValue = item[xFieldName];
                        var xItem = {"x":i,"text":xValue,"show":(i % showInterval === 0)};
                        if(xItem.show) {
                            if("datetime" === xFieldType) {
                                xItem.label = chartwork.TimeUtil.formatDatetime(xValue);
                            } else if("date" === xFieldType) {
                                xItem.label = chartwork.TimeUtil.formatDate(xValue);
                            }
                        } 
                        labels.push(xItem);
                    }
                }
                if(chartData["x-axis"]["offset-sub-column"]) {
                    xAxis["min"] = 0 - 0.5;
                    xAxis["max"] = resultArr.length - 0.5;
                } else {
                    xAxis["min"] = 0;
                    xAxis["max"] = resultArr.length - 1;
                }
                if(chartData["x-axis"]["extands-width"]) {
                    xAxis["min"] -= chartData["x-axis"]["extands-width"];
                    xAxis["max"] += chartData["x-axis"]["extands-width"];
                }
            }

            if(xAxis) {
                $.extend(chartData["x-axis"], xAxis);
            }
        },
	buildYAxis: function(chartData,chartValue) {
            if(!chartValue) {
                return;
            }
            var values = chartData["y-axis"]["field-name"];
            if(typeof values === 'string') {
                values = values.split(",");
            }
            
            var max = 0;
            var min = 99999999;
            if((chartData["y-axis"]["minY"] || chartData["y-axis"]["minY"] === 0) && (chartData["y-axis"]["maxY"] || chartData["y-axis"]["maxY"] === 0)) {
                min = chartData["y-axis"]["minY"];
                max = chartData["y-axis"]["maxY"];
            } else {
                var resultArr = chartValue;
                if(chartValue["result"]) {
                    resultArr = chartValue["result"];
                }
                if(resultArr) {
                    for(var i=0;i<resultArr.length;i++) {
                            var item = resultArr[i];
                            if(item) {
                                for(var j=0;j<values.length;j++) {
                                    var ff = parseFloat(item[values[j]]);
                                    if(ff) {
                                        if(ff > max) {
                                            max = ff;
                                        }
                                        if(ff < min) {
                                            min = ff;
                                        }
                                    }
                                }
                            }
                    }
                }
                if(min > 0) {//正值得情况，设置为最小值为0，负值的情况，需要处理负值
                    min = 0;
                }
            }
            
            var yAxis = {};
            var labels = [];
            yAxis["labels"] = labels;
            var gridNum = chartData["y-axis"]["grid-number"];
            if(!gridNum) {
                gridNum = 10;
            }
            var yFieldType = chartData["y-axis"]["field-type"];
            if(yFieldType === 'int') {//单位类型为整数，必须取整
                var topValue = (max - min);
                var stepValue = Math.round(1.0 * topValue / gridNum);
                if(stepValue === 0) {
                    stepValue = 1;
                }
                for(var i=0;i<=gridNum;i++) {
                        var theValue = parseInt(min + stepValue * i);
                        if(i === 0) {
                            labels.push({"y":theValue,"text":theValue});
                        } else {
                            var column = {"y":theValue,"text":theValue};
                            if(chartData["y-axis"]["line-dash"]) {
                                column["line-dash"] = chartData["y-axis"]["line-dash"];
                            }
                            labels.push(column);
                        }
                        if(i === gridNum) {
                            yAxis["min"] = min;
                            yAxis["max"] = theValue;
                        }
                }                
            } else {
                if(max === 0) {//==0的情况，无法做stepValue
                    var stepValue = 1.0 / gridNum;
                    for(var i=0;i<=gridNum;i++) {
                        var theValue = Math.round(stepValue * i* 100) / 100;
                        if(i === 0) {
                            labels.push({"y":theValue,"text":theValue});
                        } else {
                            var column = {"y":theValue,"text":theValue};
                            if(chartData["y-axis"]["line-dash"]) {
                                column["line-dash"] = chartData["y-axis"]["line-dash"];
                            }
                            labels.push(column);
                        }
                        if(i === gridNum) {
                            yAxis["minY"] = 0;
                            yAxis["maxY"] = theValue;
                        }
                    }
                } else if(max <= 9.0) {
                    var topValue = (max - min);
                    var stepValue = 1.0 * topValue / gridNum;
                    for(var i=0;i<=gridNum;i++) {
                        var theValue = (min + stepValue * i).toFixed(2);
                        if(i === 0) {
                            labels.push({"y":theValue,"text":theValue});
                        } else {
                            var column = {"y":theValue,"text":theValue};
                            if(chartData["y-axis"]["line-dash"]) {
                                column["line-dash"] = chartData["y-axis"]["line-dash"];
                            }
                            labels.push(column);
                        }
                        if(i === gridNum) {
                            yAxis["minY"] = min;
                            yAxis["maxY"] = theValue;
                        }
                    }

                } else {
                    var topValue = (max - min);
                    var stepValue = Math.round(1.0 * topValue / gridNum);
                    if(stepValue === 0) {
                        stepValue = 1;
                    }
                    for(var i=0;i<=gridNum;i++) {
                            var theValue = parseInt(min + stepValue * i);
                            if(i === 0) {
                                labels.push({"y":theValue,"text":theValue});
                            } else {
                                var column = {"y":theValue,"text":theValue};
                                if(chartData["y-axis"]["line-dash"]) {
                                    column["line-dash"] = chartData["y-axis"]["line-dash"];
                                }
                                labels.push(column);
                            }
                            if(i === gridNum) {
                                yAxis["min"] = min;
                                yAxis["max"] = theValue;
                            }
                    }
                }
            }
            if(yAxis) {
                $.extend(chartData["y-axis"], yAxis);
            }
	},
        buildFullYAxis: function(chartData,chartValue) {
            if(!chartValue) {
                return;
            }
            var values = chartData["y-axis"]["field-name"];
            if(typeof values === 'string') {
                values = values.split(",");
            }
            var min = 0;
            var max = 0;
            if((chartData["y-axis"]["minY"] || chartData["y-axis"]["minY"] === 0) && (chartData["y-axis"]["maxY"] || chartData["y-axis"]["maxY"] === 0)) {
                min = chartData["y-axis"]["minY"];
                max = chartData["y-axis"]["maxY"];
            } else {
                var resultArr = chartValue;
                if(chartValue["result"]) {
                    resultArr = chartValue["result"];
                }
                if(resultArr) {
                    for(var i=0;i<resultArr.length;i++) {
                        var item = resultArr[i];
                        if(item) {
                            for(var j=0;j<values.length;j++) {
                                var ff = item[values[j]];
                                if(ff) {
                                    if(ff > max) {
                                        max = ff;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            var yAxis = {};
            var labels = [];
            yAxis["labels"] = labels;
            if(max === 0) {//==0的情况，无法做stepValue
                    var stepValue = 0.1;
                    var num = 10;
                    for(var i=0;i<=num;i++) {
                        var theValue = stepValue * i;
                        var theText = (10 * i) + "%";
                        if(i === 0) {
                            labels.push({"y":theValue,"text":theText});
                        } else {
                            labels.push({"y":theValue,"text":theText,"line-dash":[1]});
                        }
                        if(i === num) {
                            yAxis["minY"] = min;
                            yAxis["maxY"] = theValue;
                        }
                    }
            } else if(max <= 9.0) {
                    var topValue = max * 1.2;
                    var num = 10;
                    var stepValue = 0.1 * topValue;
                    var theText = (10 * i) + "%";
                    for(var i=0;i<=num;i++) {
                            var theValue = stepValue * i;
                            if(i === 0) {
                                labels.push({"y":theValue,"text":theText});
                            } else {
                                labels.push({"y":theValue,"text":theText,"line-dash":[1]});
                            }
                            if(i === num) {
                                yAxis["minY"] = min;
                                yAxis["maxY"] = theValue;
                            }
                    }
            } else {
                    var topValue = max * 1.2;
                    var stepValue = (0.1 * topValue);
                    if(stepValue === 0) {
                        stepValue = 1;
                    }
                    var num = 10;
                    for(var i=0;i<=num;i++) {
                            var theValue = parseInt(stepValue * i);
                            var theText = (10 * i) + "%";
                            if(i === 0) {
                                labels.push({"y":theValue,"text":theText});
                            } else {
                                labels.push({"y":theValue,"text":theText,"line-dash":[1]});
                            }
                            if(i === num) {
                                yAxis["min"] = min;
                                yAxis["max"] = theValue;
                            }
                    }
            }
            if(yAxis) {
                $.extend(chartData["y-axis"], yAxis);
            }
	},
        //剪切数据到当前小时
        cutValuesToCurrentHour: function(chartData,chartValue) {
            var values = chartData.values;
            var myDate = new Date(chartwork._loginTime);
            var hour = myDate.getHours();  
            if(hour >= 0) {
                var newValues = [];
                for(var i=0;i<values.length;i++) {
                    if(values[i][0] <= hour) {
                        newValues.push(values[i]);
                    }
                }
                chartData.values = newValues;
            }
        },
        appendDayHours : function(chartData,chartValue) {//补上后面缺少的小时数，如果原来的时间为0开始，去除0.
            var xFieldName = chartData["x-axis"]["field-name"];
            var result;
            if($.isArray(chartValue)) {
                result = chartValue;
            } else {
                result = chartValue.result;
            }
            for(var i=result.length;i<24;i++) {
                var data = {};
                data[xFieldName] = i;
                result.push(data);
            }
            for(var i=0;i<result.length;i++) {
                var data = result[i];
                if(typeof(data[xFieldName]) === "string") {
                    if(data[xFieldName].length === 2 && data[xFieldName].startsWith("0")) {
                        data[xFieldName] = data[xFieldName].substring(1);
                    }
                }
            }
        },
        appendDayHours2: function(chartData,chartValue) {
            var xFieldName = chartData["x-axis"]["field-name"];
            var result;
            if($.isArray(chartValue)) {
                result = chartValue;
            } else {
                result = chartValue.result;
            }          
            for(var i=result.length;i<24;i++) {
                var data = {};
                var hour = "" + i;
                if(hour.length === 1) {
                    hour = "0" + hour;
                }
                data[xFieldName] = hour;
                result.push(data);
            }
        }
        
        
    };
})();