(function () {
    works.EditPage = EditPage;

    var EXTEND = works.PageFrame;

    function EditPage(wrapDiv, reseizeCallback, options) {
        if (EXTEND) {
            EXTEND.apply(this, arguments);
        }
        let self = this;
        this._reseizeCallback = reseizeCallback;
        this._options = options;
        this._wrapDiv = wrapDiv;
        this._editor = CodeMirror.fromTextArea(
            document.getElementById('code_area'), {
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
        $('.fileUpload').click(function() {
            $('.fileUpload').fileupload({
                url: "../upload?reduce=image&path=" + self._path,
                dataType: 'json',
                done: function (e, data) {
                    if (data.result.retCode === 0) {
                        let image = null;
                        if (data.result.data) {
                            image = data.result.data.image;
                        }
                        self.loadFiles(image);
                    } else if (data.result.message) {
                        alert(data.result.message)
                    }
                },
                progressall: function (e, data) {
                }
            });
            })
        $(".button_action_save").click(function () {
            self.saveContent();
        });
        $(".button_action_reload").click(function () {
            self.loadFiles();
        });
        $(".button_action_format").click(function () {
            self.formatJsonContent();
        });

        let focusFileName;
        if(this._path && this._path.startsWith("datas/p/")) {
            let pos = this._path.indexOf("/",8);
            if(pos > 0) {
                focusFileName = this._path.substring(8,pos) + ".json";
            }
        }
        this.loadFiles(focusFileName);
        this.bindSaveKey();

        this.loadAppDatas();

        var resetWindow = function() {
            self.resetSize();
        }
        window.onload = resetWindow;
        window.onresize = resetWindow;
    }

    EditPage.prototype = {
        loadAppDatas: function () {
            self = this;
            $.ajax({
                type: "GET",
                url: "../service?name=datas&v=" + new Date().getTime(),
                dataType: "json",
                cache: "false",
                success: function (data) {
                    if (data.retCode === 0) {
                        self.initAppDatas(data.data);
                    }
                }
            });
        },
        initAppDatas: function (data) {
            let self = this;
            data.text = data.name;
            let detailPath = data.introPage;
            if(detailPath) {
                let pos = detailPath.lastIndexOf("/");
                if(pos != -1) {
                    detailPath = detailPath.substring(0,pos + 1);
                }
            }
            for (let group of data.groups) {
                group.text = group.name;
                group.color = "orange"
                group.icon = 'glyphicon glyphicon-flag';
                if (group.pois) {
                    group.nodes = group.pois;
                    for (let poi of group.pois) {
                        poi.text = poi.name;
                        poi.icon = 'glyphicon glyphicon-map-marker';
                    }
                }
            }
            data.groups.unshift({ 
                "text": "简介",
                "detailPath":detailPath,
                "detailJson":"intro.json",
                "icon" : "glyphicon glyphicon-home"
            });

            data.groups.unshift({ 
                "text": "模板",
                "detailPath":"datas/model/",
                "detailJson":"model.json",
                "icon" : "glyphicon glyphicon-home"
            });

            let $appTree = $('#app_tree');
            $appTree.treeview({
                color: "#428bca",
                enableLinks: true,
                data: data.groups
            });
            $appTree.on('nodeSelected', function (event, data) {
                self._path = data.detailPath;
                if(!self._path) {
                    if(data.key && data.key.startsWith("P")) {
                        self._path = "datas/p/" + data.key + "/";
                    }
                }
                self.loadFiles(data.detailJson);
            });

        },
        bindSaveKey: function () {
            var self = this;
            document.onkeydown = function (event) {
                if (event && event.keyCode === 83 && event.ctrlKey) {
                    if (event.stopPropagation) {
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
            let pageHeight = $("#container").height() - 2;
            $("#file_tree").height((pageHeight - 25 ) / 2);
            $("#app_tree").height((pageHeight - 25 ) / 2);
            $("#c").height(pageHeight)
            $("#ccenter").height(pageHeight);
            if (this._editor) {
                this._editor.setSize("100%", pageHeight - 25);
            }
        },
        loadFiles: function (focusName) {
            var self = this;
            let $fileTree = $('#file_tree');
            $fileTree.empty();
            self.loadContent("");
            $("#file-name").val("");
            if(!self._path) {
                return;
            }
            $.ajax({
                type: "GET",
                url: "../service?name=detailfiles&path=" + self._path + "&v=" + new Date().getTime(),
                dataType: "json",
                cache: "false",
                success: function (data) {
                    if (data.retCode === 0 && data.data) {
                        for (let item of data.data) {
                            let $item;
                            if(item.endsWith(".json")) {
                                $item = $("<div style='display:block;height:30px;line-height:30px;' >" + item + "</div>");
                            } else if(item.endsWith(".png")) {
                                $item = $("<div  style='display:block;width:100%;height:30px;line-height:30px;'><img style='display:block;width:30px;height:30px;' src='../" + self._path + item + "'></div>");
                            } else if(item.endsWith(".jpg")) {
                                $item = $("<div  style='display:block;width:100%;height:30px;line-height:30px;'><img style='display:block;width:30px;height:30px;' src='../" + self._path + item + "'></div>");
                            } else if(item.endsWith(".jpeg")) {
                                $item = $("<div  style='display:block;width:100%;height:30px;line-height:30px;'><img style='display:block;width:30px;height:30px;' src='../" + self._path + item + "'></div>");
                            } else if(item.endsWith(".webp")) {
                                $item = $("<div  style='display:block;width:100%;height:30px;line-height:30px;'><img style='display:block;width:30px;height:30px;' src='../" + self._path + item + "'></div>");
                            } else if(item.endsWith(".gif")) {
                                $item = $("<div  style='display:block;width:100%;height:30px;line-height:30px;'><img style='display:block;width:30px;height:30px;' src='../" + self._path + item + "'></div>");
                            } else if(item.endsWith(".mp3")) {
                                $item = $("<div  style='display:block;width:100%;line-height:60px;'><audio style='vertical-align: middle;width: 150px;' controls><source  type='audio/mpeg' src='../" + self._path + item + "'></audio></div>");
                            } else {
                                $item = $("<div style='display:block;height:30px;line-height:30px;' >" + item + "</div>");
                            }
                            $("#file_tree").append($item);
                            $item.click(function() {
                                $item.addClass("item-focused").siblings().removeClass("item-focused");
                                self.loadFileContent(self._path, item);
                            })
                            if(focusName == item) {
                                $item.addClass("item-focused").siblings().removeClass("item-focused");
                                self.loadFileContent(self._path, item);
                            }
                        }
                    } else if (data.retCode === -1) {
                        if(data.message) {
                            alert(data.message);
                        }
                    }
                }
            });
        },
        loadFileContent: function (path, name) {
            this._currentPath = path + name;
            var self = this;
            document.title = name;
            $("#file-name").val(name);

            var lowerName = name.toLowerCase();
            if (lowerName.endsWith(".json")) {
                $("#display_area").hide();
                $("#display_area").empty();
                $(".CodeMirror").show();
                $.ajax({
                    type: "GET",
                    url: "../" + this._currentPath + "?v=" + new Date().getTime(),
                    dataType: "json",
                    success: function (data) {
                        let content = JSON.stringify(data, null, '\t');
                        self.loadContent(content);
                    }
                });
            } else if (lowerName.endsWith(".mp3")) {
                $(".CodeMirror").hide();
                $("#display_area").show();
                $("#display_area").empty();
                $("#display_area").append("<audio controls><source type='audio/mpeg' src='" + "../" + this._currentPath + "'></audio>");
            } else if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp") || lowerName.endsWith(".gif")) {
                $(".CodeMirror").hide();
                $("#display_area").show();
                $("#display_area").empty();
                $("#display_area").append("<img src='" + "../" + this._currentPath + "'>");
            } else {
                $("#display_area").hide();
                $("#display_area").empty();
                $(".CodeMirror").show();
                $.ajax({
                    type: "GET",
                    url: "../" + this._currentPath + "?v=" + new Date().getTime(),
                    dataType: "text",
                    success: function (data) {
                        self.loadContent(data);
                    }
                });
            }
        },
        loadContent: function (content) {
            var doc = CodeMirror.Doc(content, "javascript");
            this._editor.swapDoc(doc);
            this.resetSize();
        },
        formatJsonContent: function () {
            if (!this._editor) {
                return;
            }
            var fileContent = this._editor.getValue();
            if (!fileContent) {
                return;
            }
            var jsObject = eval("(" + fileContent + ")");
            var jsonContent = JSON.stringify(jsObject, null, '\t');
            var doc = CodeMirror.Doc(jsonContent, "javascript");
            this._editor.swapDoc(doc);
        },
        saveContent: function () {
            var self = this;
            if (!this._editor) {
                return;
            }
            var fileContent = this._editor.getValue();
            if (!fileContent) {
                return;
            }
            if (!this._currentPath) {
                return;
            }
            save(this._currentPath, fileContent);
            function save(currentPath, fileContent, isReloadFiles) {
                if (currentPath.endsWith(".json")) {
                    try {
                        JSON.parse(fileContent);
                    } catch (e) {
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
                            if (ret.message) {
                                $(".tipInfo").text(ret.message);
                                $(".tipInfo").css("opacity", 1);
                                $(".tipInfo").show();
                                if (ret.retCode === 0) {
                                    $(".tipInfo").fadeOut("slow");
                                } else {
                                    $(".tipInfo").fadeTo("slow", 0.5);
                                }
                            }
                            if (isReloadFiles) {
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

$(document).ready(function () {
    new works.EditPage();
});