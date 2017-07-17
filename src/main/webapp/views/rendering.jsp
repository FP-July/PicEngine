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
    <title><%=session.getAttribute("username")%> - 进行中</title>

    <link rel="stylesheet" href="../vendor/bootstrap/css/bootstrap-responsive.min.css">
    <link rel="stylesheet" href="../vendor/bootstrap/css/bootstrap.min.css">
    <style>
        .progress {
            margin-bottom: 0px;
        }
    </style>
</head>

<body>


<div class="navbar">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a data-target=".navbar-responsive-collapse" data-toggle="collapse" class="btn btn-navbar"><span
                    class="icon-bar"></span><span class="icon-bar"></span><span class="icon-bar"></span></a> <a
                href="#" class="brand">Distributed Rendering Engine - <%=session.getAttribute("username")%></a>
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
            <h2>
                正在进行的任务
            </h2>
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>
                        编号
                    </th>
                    <th>
                        任务名称
                    </th>
                    <th>
                        提交时间
                    </th>
                    <th>
                        状态
                    </th>
                    <th>
                        已用时
                    </th>
                    <th>
                        操作
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr class="danger">
                    <td>
                        1
                    </td>
                    <td>
                        17/7终稿
                    </td>
                    <td>
                        2017/07/02
                    </td>
                    <td>
                        <div class="progress progress-striped active">
                            <div class="bar bar-danger" style="width: 0%;"></div>
                        </div>
                    </td>
                    <td>
                        1 时 0 分
                    </td>
                    <td>
                        <button class="btn btn-small btn-danger">删除</button>
                    </td>
                </tr>
                <tr class="success">
                    <td>
                        2
                    </td>
                    <td>
                        公司景观设计
                    </td>
                    <td>
                        2017/05/06
                    </td>
                    <td>
                        <div class="progress progress-striped active">
                            <div class="bar bar-success" style="width: 90%;"></div>
                        </div>
                    </td>
                    <td>
                        2 时 0 分
                    </td>
                    <td>
                        <button class="btn btn-small btn-danger">删除</button>
                    </td>
                </tr>
                <tr class="error">
                    <td>
                        3
                    </td>
                    <td>
                        图形学大作业
                    </td>
                    <td>
                        2017/05/25
                    </td>
                    <td>
                        <div class="progress progress-striped active">
                            <div class="bar bar-danger" style="width: 10%;"></div>
                        </div>
                    </td>
                    <td>
                        3 时 0 分
                    </td>
                    <td>
                        <button class="btn btn-small btn-danger">删除</button>
                    </td>
                </tr>
                <tr class="warning">
                    <td>
                        4
                    </td>
                    <td>
                        光线追踪测试
                    </td>
                    <td>
                        2017/06/60
                    </td>
                    <td>
                        <div class="progress progress-striped active">
                            <div class="bar bar-warning" style="width: 40%;"></div>
                        </div>
                    </td>
                    <td>
                        4 时 0 分
                    </td>
                    <td>
                        <button class="btn btn-small btn-danger">删除</button>
                    </td>
                </tr>
                <tr class="info">
                    <td>
                        5
                    </td>
                    <td>
                        宣传片初稿
                    </td>
                    <td>
                        2017/01/20
                    </td>
                    <td>
                        <div class="progress progress-striped active">
                            <div class="bar bar-info" style="width: 70%;"></div>
                        </div>
                    </td>
                    <td>
                        5 时 0 分
                    </td>
                    <td>
                        <button class="btn btn-small btn-danger">删除</button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="span2">
        </div>
    </div>
</div>

<script src="../vendor/jQuery/jquery-1.11.3.min.js"></script>
<script src="../vendor/bootstrap/js/bootstrap.min.js"></script>
<script src="../vendor/cookie/jquery.cookie.js"></script>
<script src="../js/navbar.js"></script>
</body>
</html>
