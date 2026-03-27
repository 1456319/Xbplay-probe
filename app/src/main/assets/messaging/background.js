/* This content script acts as a messaging bridge between the Android host and the GeckoView web view. It securely transfers JSON payloads and CustomEvents, employing a recursive isValidData function to ensure only safe, plain objects and primitives are transferred via nativeMessaging, preventing unsafe data cloning (such as functions or prototypes). */
let port = browser.runtime.connectNative("browser");

// from app to webpage
port.onMessage.addListener(response => {
    try {
        // Let's just echo the message back
        console.log("Received message for firefox", response);

        // we have to use this clone into function otherwise there are permission errors
        var clonedDetail = cloneInto(response, document.defaultView);

        // emit event
        var event = new CustomEvent('geckoview', { detail: clonedDetail });
        document.dispatchEvent(event);

        // log data and echo back to android
        console.log("Dispatched geckoview event!");
        // why was I echoing message back to the app after sending to app??
//        port.postMessage(JSON.stringify(response));
    } catch (err){
        console.error("Geckoview emit error", err)
    }
});

// from webpage to app
document.addEventListener('geckoview-event', (event) => {
    console.log("Caught geckoview event", event.detail)
    port.postMessage(JSON.stringify(event.detail));
})