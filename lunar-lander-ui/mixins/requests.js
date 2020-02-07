export default {
  methods: {
    streamingRequest({url, params, success, error, method, readChunk, vueComponent, field} = {}) {
        var payload = {
            method: method,
            cache: "no-cache",
            mode: "cors",
            headers: {
                "accept": "*/*",
                "content-type": "application/json",
                "cache-control": "no-cache"
            },
        };

        if (params != null){
            payload["body"] = params;
        }
        var request = fetch( url, payload).then(function(response) {
          success(response, vueComponent, field);
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
    readChunk(reader, i,  field, vueComponent){
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

                    vueComponent.$data[field][chunkObject.index] = JSON.parse(chunkObject.msg);
                    console.log(chunkObject);

                }
            });

            if (result.done) {
                console.log("done")
                return;
            } else {
                return readChunk(reader, i, field, vueComponent);
            }
        });
    }
  }
}
