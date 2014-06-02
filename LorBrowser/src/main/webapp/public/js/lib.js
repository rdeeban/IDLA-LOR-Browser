function init(user, bucket, key) {
    user['bucket'] = bucket;
    user['key'] = key;
    user['target'] = key;
}

function getRequests() {
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

function auth() {

}

function authenticate(key) {
    console.log("logging in with: " + key);
    var authKey = "";
    var authBucket = "";
    $.ajax ({
        type: 'GET',
        url: '/auth/login/'+key,
        async: false,
        beforeSend: function() {
            $('html, body').css("cursor", "wait");
        },
        success: function(data) {
            console.log(data);
            var statusMsg = data.status['status'];
            var token = data.response;
            authKey = token['result'].split(";")[1];
            authBucket = token['result'].split(";")[0];
            if (statusMsg == 'fail') {
                window.location = "/";
            }
            console.log(authBucket);
            console.log(authKey);
            $('html, body').css("cursor", "auto");
        }
    })
    console.log("verification: " + authKey);
    return [authBucket,authKey];
}

function mail(recipient, key) {
    console.log('GET /mail/' + recipient + '/' + key);
    $.ajax ({
        type: 'GET',
        url: '/mail/'+recipient+'/'+key,
        success: function(data) {
            console.log(data);
            $("#feedback").html("<div class='alert alert-success'><button type='button' class='close' data-dismiss='alert' aria-hidden='true'>×</button><strong>Mail </strong>"+data.status.status+"</div>");
        }
    })
}

function updateQueryStringParameter(uri, key, value) {
    var re = new RegExp("([?|&])" + key + "=.*?(&|#|$)", "i");
    if (uri.match(re)) {
        return uri.replace(re, '$1' + key + "=" + value + '$2');
    } else {
        var hash =  '';
        var separator = uri.indexOf('?') !== -1 ? "&" : "?";
        if( uri.indexOf('#') !== -1 ){
            hash = uri.replace(/.*#/, '#');
            uri = uri.replace(/#.*/, '');
        }
        return uri + separator + key + "=" + value + hash;
    }
}

function OpenInNewTab(url) {
    if (url.substr(url.length - 1, url.length) == '/') {
        url += "index.html";
    }
    var win=window.open('http://' + url, '_blank');
    win.focus();
}

function download(bucket,target) {
    var type = 'GET';
    var url = '/s3/download/'+getRequests()['key']+'/'+bucket+'/'+target;
    console.log(type + ' ' + url);
    $.ajax({
        url: url,
        type: type,
        context: document.body,
        beforeSend: function(){
            console.log("downloading . . .");
            $('html, body').css("cursor", "wait");
        },
        success: function(data){
            $("#feedback").html("<div class='alert alert-success'><button type='button' class='close' data-dismiss='alert' aria-hidden='true'>×</button><strong>Download </strong> successful. Sending data to your computer. . .</div>");
            console.log("sending . . .");
            var dlif = $('<iframe/>',{'src':url}).hide();
            $("#body").append(dlif);
            console.log("done");
            $('html, body').css("cursor", "auto");
        }
    });

}

function upload(bucket,target,checkOverwrites) {
    var metadata = localStorage.getItem('trace');
    var type = 'POST';
    var url = '/s3/upload/'+getRequests()['key']+'/'+bucket+'/'+target+'/'+checkOverwrites+'/'+metadata;
    var formData = new FormData($('form')[1]);
    console.log(formData);
    console.log(type + ' ' + url);
    $.ajax({
        url: url,
        type: type,
        beforeSend: function(){
            $('html, body').css("cursor", "wait");
            console.log("uploading . . .");
        },
        success: function(data){
            console.log("done");
            console.log(data);
            var status = data.status;
            console.log(status);
            if (status.status != "success") {
                var uploads = data.response;
                console.log(uploads);
                var toAppend = status.reason + "<br/><br/>";
                for (var i = 0; i < uploads.length; i ++) {
                    if (uploads[i].isOverwrite == true) {
                        console.log(uploads[i]);
                        console.log(uploads[i].name);
                        toAppend += "<p class='text-error'>"+uploads[i].name+"</p>";
                    }
                }
                $("#modal-body-p").html(toAppend);
                $("#myModalLauncher").click();
            }
            $("#feedback").html("<div class='alert alert-success'><button type='button' class='close' data-dismiss='alert' aria-hidden='true'>×</button><strong>Upload </strong>"+status.reason+"</div>");
            fresh(target);
            $('html, body').css("cursor", "auto");
        },
        data: formData,
        cache: false,
        contentType: false,
        processData: false
    });
}

function checkFile(files) {
    var file = files[0];
    var name = file.name;
    var size = file.size;
    var type = file.type;
    console.log(name + " " + size + " " + type);
    $("#feedback").html("<div class='alert alert-info'><button type='button' class='close' data-dismiss='alert' aria-hidden='true'>×</button><strong>File info </strong>" + name + " " + Math.round(size/1024/1024) + " MB</div>");
}

function replaceAll(find, replace, str) {
    return str.replace(new RegExp(find, 'g'), replace);
}

function fresh(target) {
    var Target = replaceAll('&','/', target);
    var d = search(root,Target);
    autorefresh(d);
    toggle(d);
    update(d);
}