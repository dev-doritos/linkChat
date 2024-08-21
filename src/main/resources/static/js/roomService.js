'use strict';

function RoomService(sse) {
    this.sse = sse;
}

RoomService.prototype = {
    subscribe: function (callback) {
        const eventSource = new EventSource(this.sse);

        eventSource.onmessage = function(event) {
            const data = event.data;
            if (typeof callback === 'function') {
                callback(data);
            }
        };

        eventSource.onerror = function(error) {
            console.error("Error: ", error);
        };
    },
};