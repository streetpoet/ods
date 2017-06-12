$(function() {
	loadLabelSelection();

	$('select[id=selectLabel]').on('change', function() {
		loadUserListByLabel($('select[id=selectLabel]').val());
	});
	
	// hang on event of form with id=myform
	$("#taskCreationForm").submit(function(e) {
		if ($('textarea#taskDetail').val() == ''
			|| $('select[id=selectLabel]').val() == 0) {
			alert('invalid task data')
			return false;
		}else{
			// prevent Default functionality
			e.preventDefault();
			// get the action-url of the form
			var actionurl = e.currentTarget.action;

			// do your own request an handle the results
			$.ajax({
				url : 'http://localhost:8090/api/tasks',
				type : 'post',
				dataType : 'json',
				data : JSON.stringify({
					'detail' : $('textarea#taskDetail').val(),
					'assignerId' : $('select[id=selectAssigner]').val()
				}),
				success : function(data) {
					alert('success created');
					$('textarea#taskDetail').val('');
					loadLabelSelection();
				},
				error : function(xhr, errorType, exception) {
					alert(exception);
				}
			});
		}
	});
});

function loadUserListByLabel(labelId) {
	$.ajax({
		url : 'http://localhost:8090/api/users/label/' + labelId
	}).then(function(data) {
		$('#selectAssigner > option').remove();
		$.each(JSON.parse(data), function(i, obj) {
			$('#selectAssigner').append('<option value="' + obj.id + '">' + obj.nickname + ' ( ' + obj.loginId + ' + ' + obj.email + ' )</option>');
		});
	});
}