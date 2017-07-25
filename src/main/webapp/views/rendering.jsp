<%@ page import="java.util.ArrayList" %>
<%@ page import="bean.Task" %>
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
            <%
                ArrayList<Task> taskList = (ArrayList<Task>) session.getAttribute("taskList");
                if (taskList.size() == 0) {
            %>
            <h4>
                当前没有正在进行的任务。
            </h4>
            <%
            } else {
            %>
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
                        任务种类
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
                <%
                    for (int i = 0; i < taskList.size(); ++i) {
                        Task task = taskList.get(i);
                        String color = null;
                        int percent = task.getPercent();
                        if (percent < 10) {
                            color = "error";
                        } else if (percent < 40) {
                            color = "warning";
                        } else if (percent < 70) {
                            color = "info";
                        } else {
                            color = "success";
                        }
                        long minutes = task.getMinutes();
                        long hours = minutes / 60;
                        minutes = minutes % 60;
                %>
                <tr class="<%=color%>">
                    <td>
                        <%=task.getId()%>
                    </td>
                    <td>
                        <%=task.getName()%>
                    </td>
                    <td>
                        <%=task.getType()%>
                    </td>
                    <td>
                        <%=task.getDate()%>
                    </td>
                    <td>
	                    <% if(task.getState().equals(Task.ongoing)){
	                    %>
                        <div class="progress progress-striped active">
                            <div class="bar bar-<%=color%>" style="width: <%=task.getPercent()%>%;"></div>
                        </div>
                        <%
	                    	} else {
                        %>
                        	未启动
                        <%
	                    	}
                        %>
                    </td>
                    <td>
                     <% if(task.getState().equals(Task.ongoing)){
	                    %>
                        <%=hours%> 时 <%=minutes%> 分
                        <%
	                    	} else {
                        %>
                        	未启动
                        <%
	                    	}
                        %>
                    </td>
                    <td>
                    <button class="btn btn-small btn-danger" onclick="deleteTask('<%=task.getUsername()%>','<%=task.getName()%>')">删除</button>
                   		 <%
                   		 if(task.getState().equals(Task.init)) {
                   		 %>
						 <button class="btn btn-small btn-primary" onclick="runTask('<%=task.getUsername()%>','<%=task.getName()%>')">运行</button>
						 <%
                   		 	}
						 %>
                    </td>
                </tr>
                <%

                        }
                    }
                %>
                </tbody>
            </table>
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
<script src="../js/navbar.js"></script>
<script src="../js/taskOperations.js"></script>
</body>
</html>
