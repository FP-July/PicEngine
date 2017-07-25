/**
 * 
 */
function runTask(username, taskName) {
	var params = {
		username : username,
		taskName : taskName
	};
	$.post("/taskRun", params, function(data) {
		// TODO show this more friendly
		alert(data);
		window.location.reload();
	});
}

function deleteTask(username, taskName) {
	var params = {
		username : username,
		taskName : taskName
	};
	$.post("/taskDelete", params, function(data) {
		// TODO show this more friendly
		alert(data);
		window.location.reload();
	});
}

function getTaskLog(username, taskName) {
	var params = {
			username : username,
			taskName : taskName
		};
		$.post("/taskLog", params, function(data) {
			// TODO show this more friendly
			console.log(data);
		});
}