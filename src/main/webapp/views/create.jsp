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
</head>

<body style="background-color: #f5f5f5;">


<div class="navbar">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a data-target=".navbar-responsive-collapse" data-toggle="collapse" class="btn btn-navbar"><span
                    class="icon-bar"></span><span class="icon-bar"></span><span class="icon-bar"></span></a> <a
                href="" class="brand">Distributed Rendering Engine - <%=session.getAttribute("username")%></a>
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
            <form class="form-horizontal" method="post" action="/taskcreate">
                <div class="control-group">
                    <label class="control-label" for="task_name">任务名称</label>
                    <div class="controls">
                        <input type="text" id="task_name" placeholder="请输入任务名称" class="input-xlarge">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="task_type">任务类型</label>
                    <div class="controls">
                        <select id="task_type" class="input-xlarge">
                            <option>图像渲染</option>
                            <option>视频渲染</option>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="task_file">任务文件</label>
                    <div class="controls">
                        <input type="file" id="task_file" class="filestyle"
                               data-input="false" data-icon="false">
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <button class="btn btn-primary btn-small" type="submit">提交任务</button>
                    </div>
                </div>
            </form>
        </div>
        <div class="span2">
        </div>
    </div>
</div>

<script src="../vendor/jQuery/jquery-1.11.3.min.js"></script>
<script src="../vendor/bootstrap/js/bootstrap.min.js"></script>
<script src="../vendor/cookie/jquery.cookie.js"></script>
<script src="../vendor/bootstrap-file/bootstrap-filestyle.min.js"></script>
<script src="../js/navbar.js"></script>
<script>
    $(document).ready(function() {
        $(":file").filestyle();
    });
</script>
</body>
</html>
