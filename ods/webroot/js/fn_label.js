$(function() {
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
			},
			error : function(textStatus, errorThrown) {
				alert(errorThrown);
			}
		});

	});

});