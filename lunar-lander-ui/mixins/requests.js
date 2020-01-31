export default {
  methods: {
    streamingRequest({url, params, success, error, method} = {}) {
        var payload = {
            method: method,
            cache: "no-cache",
            mode: "cors",
            headers: {
                "accept": "application/octet-stream",
                "content-type": "application/json",
                "authorization": JSON.stringify({
                    sessionId: window.sessionStorage.getItem("sessionId"),
                    sessionKey: window.sessionStorage.getItem("sessionKey")
                }),
                "cache-control": "no-cache"
            },
        };

        if (params != null){
            payload["body"] = params;
        }
        var request = fetch(url, payload).then(function(response) {
          success(response);
        })
        .catch(error || function (error) {
            console.log(error);
            if(error.response.status === 504 || error.response.status == 401)
                logout();
                window.location.href = './';
        })
        .then(function(){
            console.log("done")
        })
        console.log(request)
    },
    readChunk(reader, i,  runWhenDone, args){
        reader.read().then(function(result){
            var decoder = new TextDecoder();
            var chunk = decoder.decode(result.value || new Uint8Array, {stream: !result.done});
            chunk.split("\n").forEach((chunkedLine) => {
                if (chunkedLine.trim().length != 0){
                    var chunkObject = {
                        "source" : id,
                        "index" : i,
                        "msg": chunkedLine,
                        "command": command
                    }
                    i = i + 1;

                    //console.log(chunkedLine);

                    if (chunkedLine.indexOf(STATUS_DELIMITER) != -1){
                        status = chunkedLine.substr(12);
                        chunkObject.msg = command + " exited with Status code " + status;
                        if (status == 0){
                            console.log(command + " Succeeded", "Success")
                        }
                        else if (status == 1){
                            console.log(command + " Not Found", "Error")
                        }
                        else {
                            console.log(command + " Failed", "Error")
                        }
                    }

                    console.log(chunkObject);

                }
            });

            if (result.done) {
                console.log("done")
                if (args == null){
                    runWhenDone()
                }else {
                    runWhenDone(args)
                }
                return;
            } else {
                return readChunk(reader, i, runWhenDone, args);
            }
        });
    }
  }
}
