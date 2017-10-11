$(document).ready(function () {
    if ("WebSocket" in window) {
        console.log("WebSocket is supported by your Browser!");
    } else {
        console.log("WebSocket is unsupported by your Browser!");
        return;
    }

    var getScriptParamUrl = function () {
        var scripts = document.getElementsByTagName('script');
        var lastScript = scripts[scripts.length - 1];
        return lastScript.getAttribute('data-url');
    };

    var send = function () {
        var text = $message.val();
        $message.val("");
        connection.send(text);
    };


    var $messages = $("#messages"), $send = $("#send"), $message = $("#message");

    var connection = new WebSocket(getScriptParamUrl());

    $send.prop("disabled", true);

    connection.onopen = function () {
        $send.prop("disabled", false);
        $send.on('click', send);
        $message.keypress(function (event) {
            var keycode = event.keyCode;
            var message = $message.val();
            if (keycode == '13' && message != '') {
                send()
            }
        });
    };

    connection.onerror = function (error) {
        console.log('WebSocket Error', error);
    };

    connection.onmessage = function (event) {
        console.log('receive a message', event.data);
        $messages.append($("<li style='font-size: 1.5em'>" + event.data + "</li>"))
    };

    var ping = function () {
        connection.send("ping-pong")
    };

    window.setInterval(ping, 1000 * 5);

    console.log("chat app is running!");
});