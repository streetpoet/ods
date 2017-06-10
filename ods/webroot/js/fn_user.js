$(function() {

	loadSelectionData();

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
					loadData();
				},
				error : function(textStatus, errorThrown) {
					alert(errorThrown);
				}
			});
		}
	});

});

function loadSelectionData() {
	$.ajax({
		url : 'http://localhost:8090/api/labels'
	}).then(function(data) {
		$('#selectLabel > option').remove();
		$.each(JSON.parse(data), function(i, obj) {
			$('#selectLabel').append('<option>' 
					+ obj.labelName 
					+ '</option>');
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