(function() {
    works.EditPage = EditPage;

    var EXTEND = works.PageFrame;

    function EditPage(wrapDiv,reseizeCallback,options) {
        if (EXTEND) {
            EXTEND.apply(this, arguments);
        }
        this._reseizeCallback = reseizeCallback;
        this._options = options;
        this._wrapDiv = wrapDiv;
        this._editor = CodeMirror.fromTextArea(
            document.getElementById('code_area'),{
                lineNumbers: true,
                lineWrapping: true
            }
        );                
		
		var query = window.location.search.substring(1);
		var vars = query.split('&');
		for (var i = 0; i < vars.length; i++) {
		  var pair = vars[i].split('=');
		  if (decodeURIComponent(pair[0]) == "path") {
			  this._path = pair[1];
			  break;
		  }
		}
        
		let self = this;
		$('.fileUpload').fileupload({
			url: "../upload?name=image&path=../" + self._path,
			dataType: 'json',
			done: function (e, data) {
				if (data.result.retCode === 0) {
					let image = null;
					if(data.result.data) {
						image = data.result.data.image;
					}
					self.loadFiles(image);
				} else if(data.result.message) {
					alert(data.result.message)
				}
			},
			progressall: function (e, data) {
			}
		});
		$(".button_action_new").click(function() {
			self.newJsonFile();
		});
		$(".button_action_save").click(function() {
			self.saveContent();
		});
		$(".button_action_reload").click(function() {
			self.loadFiles();
		});
		$(".button_action_format").click(function() {
			self.formatJsonContent();
		});
		
		
        this.initFrame();
        this.loadFiles();
        this.bindSaveKey();
        
    }

    EditPage.prototype = {
        bindSaveKey: function() {
            var self = this;
            document.onkeydown=function(event){ 
                if(event && event.keyCode === 83 && event.ctrlKey) {
                    if(event.stopPropagation) {
                        event.stopPropagation();
                        event.preventDefault();
                    } else {//IE
                        event.cancelBubble = true;
                        event.returnValue = false;
                    }
                    self.saveContent();
                }
            };
        },
        resetSize: function () {
            var cOffsetTop = this._c.offsetTop;
            var marginLeft = 0;
            this._c.style["marginLeft"] = marginLeft + "px";
            
            var marginRight = 0;
            this._c.style["marginRight"] = marginRight + "px";
            var newHeight = works.utils.getWindowHeight() - cOffsetTop - this._s.offsetHeight;
            this._c.style.height = newHeight + "px";
            $("#ccenter").height(newHeight);
            $(".treepanel").height(newHeight - 26);
            if(this._editor) {
                this._editor.setSize("100%",newHeight);
            }
            this.onWindowResize();
        },
        loadFiles: function(focusName) {
            var self = this;
            $(".sidebar_tree").empty();
			self.loadContent("");
			
            $.ajax({
                type:"GET",
                url:"../service?name=detailfiles&path=" + self._path + "&v=" + new Date().getTime(),
                dataType:"json",
                cache:"false",
                success:function(data){
                    if(data.retCode === 0 && data.data) {
                        for(let item of data.data) {
                            let $item = $("<div title='" + item + "'>" + item + "</div>");
                            $(".sidebar_tree").append($item);
                            $item.click(function() {
								$item.addClass("focus_item").siblings().removeClass("focus_item")
                                self.loadFileContent(self._path,item);
                            });
							if(item === focusName) {
								$item.addClass("focus_item")
                                self.loadFileContent(self._path,item);
							}
                        }
                    } else if(data.retCode === -1) {//dir is not exist
                        var message = data.message;
                        if(message && message.endsWith("is not exist!")) {
                            new works.MyAlert().hiConfirm("目录不存在，创建此目录?","目录创建",function(r) {
                                if(r) {
                                    buildPath(pagePath);
                                } else {
                                    //close page?
                                }
                            });
                        }
                    }
                }
            });
            
            function buildPath(pagePath) {
                var allFilesUrl = pagePath + "buildPath";            
                $.ajax({
                    type:"GET",
                    url:allFilesUrl,
                    dataType:"json",
                    success:function(data){
                        if(data.retCode === 0) {
                            var pathNode = {
                                class: "dir",
                                "isDir":true,
                                file: pagePath
                            };
                            self._currentPath = pathNode;
                        }
                        if(data.message) {
                            works.alert(data.message);
                        }
                    }
                });
            }
            
        },
        loadFileContent: function(path,name) {
            this._currentPath = path + name;
            var self = this;
            document.title=name;
			
			var lowerName = name.toLowerCase();
            if(lowerName.endsWith(".json")) {
				$("#display_area").hide();
				$("#display_area").empty();
				$(".CodeMirror").show();
                $.ajax({
                    type:"GET",
                    url:"../" + this._currentPath + "?v=" + new Date().getTime(),
                    dataType:"json",
                    success:function(data){
                        let content = JSON.stringify(data, null, '\t');
                        self.loadContent(content);
					}
				});
            } else if(lowerName.endsWith(".mp3")) {
				$(".CodeMirror").hide();
				$("#display_area").show();
				$("#display_area").empty();
				$("#display_area").append("<audio controls><source type='audio/mpeg' src='" + "../" + this._currentPath + "'></audio>");
			} else if(lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp") || lowerName.endsWith(".gif")) {
				$(".CodeMirror").hide();
				$("#display_area").show();
				$("#display_area").empty();
				$("#display_area").append("<img src='" + "../" + this._currentPath + "'>");
			} else {
				$("#display_area").hide();
				$("#display_area").empty();
				$(".CodeMirror").show();
                $.ajax({
                    type:"GET",
                    url:"../" + this._currentPath + "?v=" + new Date().getTime(),
                    dataType: "text",
                    success:function(data){
                        self.loadContent(data);
					}
				});
			}
        },
        loadContent: function(content) {
            var doc = CodeMirror.Doc(content, "javascript");
            this._editor.swapDoc(doc);
            this.resetSize();
        },
        formatJsonContent: function() {
            if(!this._editor) {
                return;
            }
            var fileContent = this._editor.getValue();
            if(!fileContent) {
                return;
            }    
            var jsObject = eval("(" + fileContent + ")"); 
            var jsonContent = JSON.stringify(jsObject, null, '\t');
            var doc = CodeMirror.Doc(jsonContent, "javascript");
            this._editor.swapDoc(doc);
        },
        newJsonFile: function() {
            var self = this;
			
			$.ajax({
					type: "GET",
					url: "../service?name=detailjsonbuild&path=" + self._path,
					dataType: "json",
					contentType: 'text/plain; charset=UTF-8',
					cache: false,
					success: function (ret) {
						console.dir(ret)
						if(ret.retCode === 0) {
							self.loadFiles(ret.data.name);
						} else if(ret.message) {
							alert(ret.message);
						}
					}
			});
        },
        saveContent: function() {
            var self = this;
            if(!this._editor) {
                return;
            }
            var fileContent = this._editor.getValue();
            if(!fileContent) {
                return;
            }    
            if(!this._currentPath) {
                return;
            }
            save(this._currentPath,fileContent);
            function save(currentPath,fileContent,isReloadFiles) {
                console.log(fileContent)
                if(currentPath.endsWith(".json")) {
                    try {
                        JSON.parse(fileContent);
                    } catch(e) {
                        alert(e);
                        return;
                    }
                }        

                if (fileContent) {
                    $.ajax({
                            type: "POST",
                            url: "../service?name=filesave&path=" + currentPath,
                            dataType: "json",
                            data: fileContent,
                            contentType: 'text/plain; charset=UTF-8',
                            cache: false,
                            success: function (ret) {
                                if(ret.message) {
                                    $(".tipInfo").text(ret.message);
                                    $(".tipInfo").css( "opacity", 1);
                                    $(".tipInfo").show();
                                    if(ret.retCode === 0) {
                                        $(".tipInfo").fadeOut("slow");
                                    } else {
                                        $(".tipInfo").fadeTo("slow", 0.5);
                                    }
                                }
                                if(isReloadFiles) {
                                    self.loadFiles();
                                }
                            }
                    });
                }
            }
        }        
    };

    if (EXTEND) {
        works.utils.inherits(EditPage, EXTEND);
    }
})();

$(document).ready(function() {
    new works.EditPage();
});