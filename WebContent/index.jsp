<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="webuploader/jquery-2.1.4.js"></script>
<script type="text/javascript" src="webuploader/webuploader.js"></script>
<link rel="stylesheet" type="text/css" href="webuploader/webuploader.css">
<link rel="stylesheet" type="text/css" href="webuploader/bootstrap.css">
<title>webuploader测试</title>
</head>
<body>
<div id="uploader" class="wu-example">
    <!--用来存放文件信息-->
    <div id="thelist" class="uploader-list"></div>
    <div class="btns">
        <div id="picker">选择文件</div>
    </div>
</div>
<button id="ctlBtn" class="btn btn-default">开始上传</button>
</body>
<script type="text/javascript">
$(function(){
	//钩子函数
	WebUploader.Uploader.register({
		'before-send-file':'beforeSendFile'
	},{
		beforeSendFile : function(file){
			console.log("发送之前:"+file.name);
			var me = this,
				owner = this.owner;
				server = me.options.server,
				deferred = WebUploader.Deferred();
			
			console.log(this);
			console.log(deferred);
			
			owner.md5File( file.source )
				// 如果读取出错了，则通过reject告诉webuploader文件上传出错。
				.fail(function(){
					deferred.reject();
				})
				// md5值计算完成
				.then(function( md5 ) {
					console.log("md5:"+md5);
					
					if(md5 == '285787bf9b7ee28d9761c5ec32e8fb9a2'){
						owner.skipFile( file );
                        console.log('文件重复，已跳过');
					}
					deferred.resolve();
					
				});
			
			return deferred.promise();
		}
		
	});
	
	
	WebUploader.Uploader.register({
		'before-send': 'checkchunk'
	},{
		checkchunk : function(block){
			var blob = block.blob.getSource(),
				owner = this.owner,
            	deferred = $.Deferred();
			
			console.log("------------------");
			console.log(block);
			
			owner.md5File( block.blob )
				// 如果读取出错了，则通过reject告诉webuploader文件上传出错。
				.fail(function(){
					deferred.reject();
				})
				// md5值计算完成
				.then(function( md5 ) {
					console.log("md5:"+md5);
					
					if(md5 == '285787bf9b7ee28d9761c5ec32e8fb9a'){
						//owner.skipFile( block.file );
	                    console.log('文件重复，已跳过');
	                    console.log(block.blob);
					}else{
						deferred.resolve();
					}
					
			});

	        return deferred.promise();
		}
		
	});
	
	var uploader = WebUploader.create({

	    // swf文件路径
	    swf: 'webuploader/Uploader.swf',

	    // 文件接收服务端。
	    server: 'webUploaderServlet.do',

	    // 选择文件的按钮。可选。
	    // 内部根据当前运行是创建，可能是input元素，也可能是flash.
	    pick: '#picker',

	    // 不压缩image, 默认如果是jpeg，文件上传前会压缩一把再上传！
	    resize: false,
	    
	    auto: false,
	    
	    chunked: true,
	    chunkSize: 5242880,
	    chunkRetry: 3,
	    threads: 1
	});

	
	
	
	
	//当有文件被添加进队列的时候
	uploader.on( 'fileQueued', function( file ) {
		console.log("文件:"+file.name+"被加入列队");
		console.log(uploader.md5File( file ));
		
		$list = $("#thelist");
	    $list.append( '<div id="' + file.id + '" class="item">' +
	        '<h4 class="info">' + file.name + '</h4>' +
	        '<p class="state">等待上传...</p>' +
	    '</div>' );
	});

	//文件上传过程中创建进度条实时显示。
	uploader.on( 'uploadProgress', function( file, percentage ) {
	    var $li = $( '#'+file.id ),
	        $percent = $li.find('.progress .progress-bar');

	    // 避免重复创建
	    if ( !$percent.length ) {
	        $percent = $('<div class="progress progress-striped active">' +
	          '<div class="progress-bar" role="progressbar" style="width: 0%">' +
	          '</div>' +
	        '</div>').appendTo( $li ).find('.progress-bar');
	    }

	    $li.find('p.state').text('上传中');

	    $percent.css( 'width', percentage * 100 + '%' );
	});

	uploader.on( 'uploadSuccess', function( file, response ) {
		console.log(response);
	    $( '#'+file.id ).find('p.state').text('已上传');
	});

	uploader.on( 'uploadError', function( file ) {
	    $( '#'+file.id ).find('p.state').text('上传出错');
	});

	uploader.on( 'uploadComplete', function( file ) {
	    $( '#'+file.id ).find('.progress').fadeOut();
	});

	$("#ctlBtn").click(function(){
		uploader.upload();
	});
	
});


</script>
</html>