$(function() {

	loadLabelData();

	// hang on event of form with id=myform
	$("#labelCreationForm").submit(function(e) {

		if ($('input#labelName').val() == '') {
			alert('invalid label name');
			return false;
		}else{
			// prevent Default functionality
			e.preventDefault();
			// get the action-url of the form
			var actionurl = e.currentTarget.action;

			// do your own request an handle the results
			$.ajax({
				url : 'http://localhost:8090/api/labels',
				type : 'post',
				dataType : 'json',
				data : JSON.stringify({
					'labelName' : $('input#labelName').val()
				}),
				success : function(data) {
					$('input#labelName').val('');
					loadLabelData();
				},
				error : function(xhr, errorType, exception) {
					alert(exception);
				}
			});
		}
	});

});

function loadLabelData() {
	$.ajax({
		url : 'http://localhost:8090/api/labels'
	}).then(function(data) {
		$('#labelTable tbody > tr').remove();
		$.each(JSON.parse(data), function(i, obj) {
			$('#labelTable tbody').append('<tr><td><span class="label label-primary">' 
					+ obj.labelName 
					+ '</span></td><td><button type="button" class="btn btn-info btn-sm">Update</button> <button type="button" class="btn btn-danger btn-sm" onclick="deleteData(' + obj.id + ')">Delete</button></td></tr>');
		});
	});
}

function deleteData(id) {
	$.ajax({
	    url: 'http://localhost:8090/api/labels/' + id,
	    type: 'delete',
	    success: function(result) {
	        alert('delete success');
	        loadLabelData();
	    },error : function(xhr, errorType, exception) {
			alert(exception);
		}
	});
}