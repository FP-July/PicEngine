<%--
  Created by IntelliJ IDEA.
  User: THU73
  Date: 17/7/14
  Time: 23:30
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=session.getAttribute("username")%> - 创建新任务</title>

    <link rel="stylesheet" href="../vendor/bootstrap/css/bootstrap-responsive.min.css">
    <link rel="stylesheet" href="../vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="../css/fileinput.css">
</head>

<body style="background-color: #f5f5f5;">


<div class="navbar">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a data-target=".navbar-responsive-collapse" data-toggle="collapse" class="btn btn-navbar">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <a href="#myModal" role="button" data-toggle="modal" class="brand">
                Distributed Rendering Engine - <%=session.getAttribute("username")%>
            </a>
            <div class="nav-collapse collapse navbar-responsive-collapse">
                <ul class="nav">
                    <li>
                        <a href="javascript:void(0);" onclick="visit('info')">主页</a>
                    </li>
                    <li class="active">
                        <a href="javascript:void(0);" onclick="visit('create')">新建任务</a>
                    </li>
                    <li>
                        <a href="javascript:void(0);" onclick="visit('rendering')">正在进行</a>
                    </li>
                    <li>
                        <a href="javascript:void(0);" onclick="visit('finished')">已完成</a>
                    </li>
                </ul>
                <ul class="nav pull-right">
                    <li>
                        <a href="javascript:void(0);" onclick="visit('help')">帮助</a>
                    </li>
                    <li>
                        <a href="/">退出</a>
                    </li>
                </ul>
            </div>

        </div>
    </div>

</div>
<div class="container-fluid">
    <div class="row-fluid">
        <div class="span2">
        </div>
        <div class="span8">
            <h2 style="margin-bottom: 50px; padding-left: 110px;">创建新任务</h2>
            <form id="task_form" class="form-horizontal" method="post" action="/taskcreate"
                  enctype="multipart/form-data">
                <div class="control-group">
                    <label class="control-label" for="task_name">任务名称</label>
                    <div class="controls">
                        <input type="text" id="task_name" name="task_name"
                               placeholder="请输入任务名称" class="input-xlarge">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="task_type">任务类型</label>
                    <div class="controls">
                        <select id="task_type" name="task_type" class="input-xlarge">
                            <option value="图像渲染">图像渲染</option>
                            <option value="视频渲染">视频渲染</option>
                        </select>
                    </div>
                </div>
                <input name="username" value="<%=session.getAttribute("username")%>" type="hidden">
                <div class="control-group">
                    <label class="control-label" for="task_file">任务文件</label>
                    <div class="controls">
                        <input id="task_file" name="task_file" type="file" title="请选择文件"
                               multiple accept="text/plain" onchange="examine_size();">
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <button id="create_button" class="btn btn-primary btn-small" type="button">提交任务</button>
                    </div>
                </div>
            </form>
        </div>
        <div class="span2">
        </div>
    </div>
</div>

<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel">相关信息</h3>
    </div>
    <div class="modal-body">
        <h4>小组名单</h4>
        <p>计45 江天</p>
        <p>计45 王龙涛</p>
        <p>计45 张琛昱</p>
        <hr>
        <h4>Final Project for Distributed Data Processing</h4>
        <p>Distributed Image Rendering Engine</p>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
    </div>
</div>

<script src="../vendor/jQuery/jquery-1.11.3.min.js"></script>
<script src="../vendor/bootstrap/js/bootstrap.min.js"></script>
<script src="../vendor/cookie/jquery.cookie.js"></script>
<script src="../vendor/bootstrap-file/bootstrap-file.js"></script>
<script src="../js/navbar.js"></script>
<script src="../js/fileinput.min.js"></script>
<script>
    $(document).ready(function () {
        $('input[type=file]').fileinput({
        	uploadUrl : "/upload_img",//上传图片的url
        	overwriteInitial : false,
        	maxFileSize : 1000,//上传文件最大的尺寸
        	maxFilesNum : 10,//上传最大的文件数量
        	showPreview  : false,
        	showUpload : false
        });
    });

    examine_size = function () {
        var file = document.getElementById('task_file').files;
        var size = file[0].size;
        if (size / 1024 / 1024 > 10) {
            alert('最多支持大小为10MB的文件！');
            return;
        }
    }

    $('#create_button').click(function () {
        var task_name = $('#task_name').val();
        var task_type = $('#task_type').val();
        var file_name = $('#task_file').val();

        if (task_name === '') {
            alert('请输入任务名称！');
            return;
        }

        if (file_name === '') {
            alert('请上传要渲染的工程文件！');
            return;
        }

        $('#task_form').submit();
    });
</script>
</body>
</html>
