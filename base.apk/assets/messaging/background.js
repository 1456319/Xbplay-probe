/**
 * Messaging Bridge Script (`background.js`)
 *
 * This script establishes a secure two-way communication channel between
 * the native Android host environment and the Mozilla GeckoView web view.
 * It utilizes WebExtensions nativeMessaging to send/receive JSON payloads
 * and CustomEvents to interface with the frontend web assets (e.g., old.html).
 */

// Establish the native connection with the Android app acting as the 'browser' port.
let port = browser.runtime.connectNative("browser");

// Listener for messages arriving from the Android native side to be passed to the webpage.
port.onMessage.addListener(response => {
    try {
        // Let's just echo the message back
        console.log("Received message for firefox", response);

        // The cloneInto function is a Gecko-specific security mechanism.
        // It securely clones the response object into the target scope (the webpage's defaultView)
        // to prevent unsafe data structures (like functions or prototypes) from breaching the sandbox.
        var clonedDetail = cloneInto(response, document.defaultView);

        // Emit the sanitized data to the webpage via a CustomEvent.
        var event = new CustomEvent('geckoview', { detail: clonedDetail });
        document.dispatchEvent(event);

        console.log("Dispatched geckoview event!");
    } catch (err){
        console.error("Geckoview emit error", err)
    }
});

// Listener for messages originating from the webpage to be sent to the Android app.
document.addEventListener('geckoview-event', (event) => {
    console.log("Caught geckoview event", event.detail)
    // Serialize the event detail into a JSON string (if not already) and post it through the native messaging port.
    const message = typeof event.detail === "string" ? event.detail : JSON.stringify(event.detail);
    port.postMessage(message);
})