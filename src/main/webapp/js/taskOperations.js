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

function downloadResult(username, taskID) {
	var options = {
		url : "/downloadResult",
		data : {
			username : username,
			taskID : taskID
		},
		method : 'post'
	};

	var config = $.extend(true, {
		method : 'post'
	}, options);
	var $iframe = $('<iframe id="down-file-iframe" />');
	var $form = $('<form target="down-file-iframe" method="' + config.method
			+ '" />');
	$form.attr('action', config.url);
	for ( var key in config.data) {
		$form.append('<input type="hidden" name="' + key + '" value="'
				+ config.data[key] + '" />');
	}
	$iframe.append($form);
	$(document.body).append($iframe);
	$form[0].submit();
	$iframe.remove();
}