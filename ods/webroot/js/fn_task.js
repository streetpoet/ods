$(function() {
	loadLabelSelection();

	$('select[id=selectLabel]').on('change', function() {
		loadUserListByLabel($('select[id=selectLabel]').val());
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