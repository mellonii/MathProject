function answerWithSSML(param, ssml) {
    if (typeof param === 'object') {
        var text = param.text;
        text = resolveInlineDictionary(resolveVariables(resolveInlineDictionary(text)));
        var ssml = param.ssml;
    } else if (ssml) {
        var text = param;
        var ssml = ssml;
    } else {
        var text = param;
        var ssml = text;
    }
    var reply = {
        "type": "text",
        "text": text,
        "ssml": ssml
    };
    $jsapi.context().response.replies = $jsapi.context().response.replies || [];
    $jsapi.context().response.replies.push(reply);
}