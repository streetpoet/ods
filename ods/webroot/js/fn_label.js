$(function() {

	loadData();

	// hang on event of form with id=myform
	$("#labelCreationForm").submit(function(e) {

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
				loadData();
			},
			error : function(textStatus, errorThrown) {
				alert(errorThrown);
			}
		});

	});

});

function loadData() {
	$.ajax({
		url : 'http://localhost:8090/api/labels'
	}).then(function(data) {
		$('#labelTable tbody > tr').remove();
		$.each(JSON.parse(data), function(i, obj) {
			$('#labelTable tbody').append('<tr><td><h5><span class="label label-primary">' 
					+ obj.labelName 
					+ '</span></h5></td><td><button type="button" class="btn btn-info">Update</button> <button type="button" class="btn btn-danger" onclick="deleteData(' + obj.id + ')">Delete</button></td></tr>');
		});
	});
}

function deleteData(id) {
	$.ajax({
	    url: 'http://localhost:8090/api/labels/' + id,
	    type: 'delete',
	    success: function(result) {
	        alert('delete success');
	        loadData();
	    }
	});
}