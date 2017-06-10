$(function() {

	loadSelectionData();

	// hang on event of form with id=myform
	$("#userCreationForm").submit(function(e) {
		if ($('input#inputLoginId').val() == ''
			|| $('input#inputNickname').val() == ''
			|| $('input#inputPassword').val() == ''
			|| $('input#inputEmail').val() == '') {
			alert('invalid user data')
			return false;
		}else{
			// prevent Default functionality
			e.preventDefault();
			// get the action-url of the form
			var actionurl = e.currentTarget.action;

			// do your own request an handle the results
			$.ajax({
				url : 'http://localhost:8090/api/users',
				type : 'post',
				dataType : 'json',
				data : JSON.stringify({
					'loginId' : $('input#inputLoginId').val(),
					'nickname' : $('input#inputNickname').val(),
					'password' : $('input#inputPassword').val(),
					'email' : $('input#inputEmail').val(),
					'labelId' : $('select[id=selectLabel]').val()
				}),
				success : function(data) {
					$('input#inputLoginId').val('');
					$('input#inputNickname').val('');
					$('input#inputPassword').val('');
					$('input#inputEmail').val('');
					loadSelectionData();
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
			$('#selectLabel').append('<option value="' + obj.id + '">' 
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