$(document).ready(function () {
    if ("WebSocket" in window) {
        console.log("WebSocket is supported by your Browser!");
    } else {
        console.log("WebSocket is unsupported by your Browser!");
        return;
    }

    var send = function () {
        var text = $message.val();
        $message.val("");
        connection.send(text);
    };


    var $messages = $("#messages"), $send = $("#send"), $message = $("#message");

    var connection = new WebSocket("ws://localhost:9000/socket");

    $send.prop("disabled", true);

    connection.onopen = function () {
        $send.prop("disabled", false);
        $messages.prepend($("<li class='bg-info' style='font-size: 1.5em'>Connected</li>"));
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

    console.log("chat app is running!");
});