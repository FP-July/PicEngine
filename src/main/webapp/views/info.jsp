<%--
  Created by IntelliJ IDEA.
  User: THU73
  Date: 17/7/14
  Time: 22:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dr. Who Login</title>

    <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap-responsive.min.css">
    <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap.min.css">
</head>

<body>


<div class="navbar">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a data-target=".navbar-responsive-collapse" data-toggle="collapse" class="btn btn-navbar"><span
                    class="icon-bar"></span><span class="icon-bar"></span><span class="icon-bar"></span></a> <a
                href="" class="brand">Distributed Rendering Engine - Dr. Who</a>
            <div class="nav-collapse collapse navbar-responsive-collapse">
                <ul class="nav">
                    <li>
                        <a href="javascript:void(0);" onclick="visit('info')">主页</a>
                    </li>
                    <li>
                        <a href="javascript:void(0);" onclick="visit('create')">新建任务</a>
                    </li>
                    <li class="active">
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
            <div class="alert alert-success">
                你已经完成了 5 个任务！
            </div>
            <div class="alert alert-info">
                你有 3 个任务正在运行！
            </div>
            <div class="alert alert-danger">
                你有 1 个任务运行失败！
            </div>
        </div>
        <div class="span2">
        </div>
    </div>
</div>
<username hidden><%=session.getAttribute("username")%></username>

<script src="vendor/jQuery/jquery-1.11.3.min.js"></script>
<script src="vendor/bootstrap/js/bootstrap.min.js"></script>
<script src="../vendor/cookie/jquery.cookie.js"></script>
</body>
<script>
    $(document).ready(function() {
        var username = $('username').html();
        $.cookie('username', username);
    })

    visit = function (target) {
        var form = document.createElement('form');
        form.action = '/' + target;

        form.target = '_self';
        form.method = 'post';

        var opt = document.createElement('input');
        opt.name = 'username';
        opt.value = $.cookie('username');
        form.appendChild(opt);

        document.body.appendChild(form);
        form.submit();
    }
</script>
</html>
