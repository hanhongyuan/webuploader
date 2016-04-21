function isExist(md5){
	var exist = false;
	$.ajax({
	   type: "get",
	   async: false,
	   url: "webUploaderServlet.do",
	   data: {md5:md5, type:"md5"},
	   dataType: "json",
	   success: function(text){
		   exist = text;
	   }
	});
	console.log(md5+"是否存在:"+exist);
	return exist;
}

function getSubFile(parentMd5){
	var files = [];
	$.ajax({
	   type: "get",
	   async: false,
	   url: "webUploaderServlet.do",
	   data: {parentMd5:parentMd5, type:"parentMd5"},
	   dataType: "json",
	   success: function(text){
		 files = text;
	   }
	});
	
	console.log("getSubFile->使用parentMd5:"+parentMd5+"从服务器查询的数据:");
	console.log(files);
	return files;
}

function contains(files, md5){
//	if(md5){
//		return false;
//	}
	if(files){
		for(var i = 0; i < files.length; i++){
			if(md5 == files[i]){
				console.log("返回数据列表中包含:"+md5);
				return true;
			}
		}
	}
	return false;
}