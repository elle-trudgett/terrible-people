function sendHeartbeat() {
    var els = document.getElementsByClassName("tp-game-component");
    if (els.length > 0) {
        els[0].$server.heartbeat();
    }
    setTimeout(sendHeartbeat, 1000);
}

sendHeartbeat();