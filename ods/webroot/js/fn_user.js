$(function() {

	loadLabelSelection();
	loadUserData();

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
					
					loadLabelSelection();
					loadUserData();
				},
				error : function(xhr, errorType, exception) {
					alert(exception);
				}
			});
		}
	});

});

function loadUserData() {
	$.ajax({
		url : 'http://localhost:8090/api/users'
	}).then(function(data) {
		$('#userTable tbody > tr').remove();
		$.each(JSON.parse(data), function(i, obj) {
			$('#userTable tbody').append('<tr>'
					+ '<td>' + obj.username + '</td>'
					+ '<td>' + obj.nickname + '</td>'
					+ '<td>' + obj.email + '</td>'
					+ '<td><span class="label label-primary">' + obj.labelName + '</span></td>'
					+ '<td><button type="button" class="btn btn-info btn-sm">Update</button> <button type="button" class="btn btn-danger btn-sm" onclick="deleteData(' + obj.id + ')">Delete</button></td>'
					+ '</tr>');
		});
	});
}

function deleteData(id) {
	$.ajax({
	    url: 'http://localhost:8090/api/users/' + id,
	    type: 'delete',
	    success: function(result) {
	        alert('delete success');
	        loadUserData();
	    },
	    error : function(xhr, errorType, exception) {
			alert(exception);
		}
	});
}