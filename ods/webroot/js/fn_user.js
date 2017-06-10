$(function() {

	loadSelectionData();
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
					
					loadSelectionData();
					loadUserData();
				},
				error : function(xhr, errorType, exception) {
					alert(exception);
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
					+ '<td><h5><span class="label label-primary">' + obj.labelName + '</span></h5></td>'
					+ '<td><button type="button" class="btn btn-info">Update</button> <button type="button" class="btn btn-danger" onclick="deleteData(' + obj.id + ')">Delete</button></td>'
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