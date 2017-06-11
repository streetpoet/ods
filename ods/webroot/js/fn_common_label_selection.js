function loadLabelSelection() {
	$.ajax({
		url : 'http://localhost:8090/api/labels'
	}).then(function(data) {
		$('#selectLabel > option:gt(1)').remove();
		$.each(JSON.parse(data), function(i, obj) {
			$('#selectLabel').append('<option value="' + obj.id + '">' + obj.labelName + '</option>');
		});
	});
}