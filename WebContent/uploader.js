function isExist(md5){
	var exist = false;
	$.get('webUploaderServlet.do',{md5:md5, type:"md5"},function(text){
		console.log(text);
		exist = text;
	});
	return exist;
}

function getSubFile(parentMd5){
	var files = [];
	$.get('webUploaderServlet.do',{parentMd5:parentMd5, type:"parentMd5"},function(text){
		files = text;
		console.log(files[0]);
	});
	return files;
}

function contains(files, md5){
	if(files){
		for(var i = 0; i < files.length; i++){
			console.log(files[i]);
			if(md5 == files[i]){
				console.log("contains: true");
				return true;
			}
		}
	}
	console.log("contains: false");
	return false;
}