/**
 * Unit Test Suite for `background.js` Messaging Bridge
 *
 * This file contains standalone JavaScript tests utilizing Node.js's native
 * `node:test` runner. It is designed to verify the robust functionality of
 * the GeckoView messaging script without requiring a full Android or browser
 * environment.
 *
 * It achieves this by using the `vm` module to sandbox and mock the required
 * Browser WebExtension APIs (e.g., `browser.runtime.connectNative`) and DOM
 * elements (e.g., `document.dispatchEvent`, `CustomEvent`).
 */
const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');
const vm = require('vm');

test('background.js tests', async (t) => {
    const scriptPath = path.join(__dirname, 'background.js');
    const scriptContent = fs.readFileSync(scriptPath, 'utf8');

    // Creates an isolated sandbox environment mirroring the expected browser context
    function createMockEnvironment() {
        const env = {
            addListenerCb: null,
            postMessageCalled: [],
            dispatchedEvents: [],
            eventListeners: {},
            errorLogged: null,
            console: {
                log: () => {},
                error: (msg, err) => { env.errorLogged = { msg, err }; }
            },
            // Mocking the WebExtension API used for native messaging
            browser: {
                runtime: {
                    connectNative: (name) => {
                        assert.strictEqual(name, "browser");
                        return {
                            onMessage: {
                                addListener: (cb) => { env.addListenerCb = cb; }
                            },
                            postMessage: (msg) => { env.postMessageCalled.push(msg); }
                        };
                    }
                }
            },
            // Mocking the DOM environment to intercept events dispatched to/from the webpage
            document: {
                defaultView: { isDefaultView: true },
                dispatchEvent: (event) => { env.dispatchedEvents.push(event); },
                addEventListener: (name, cb) => {
                    if (!env.eventListeners[name]) env.eventListeners[name] = [];
                    env.eventListeners[name].push(cb);
                }
            },
            // Mocking Gecko's specific security function
            cloneInto: (obj, targetScope) => {
                assert.strictEqual(targetScope, env.document.defaultView);
                return { ...obj, cloned: true };
            },
            CustomEvent: class CustomEvent {
                constructor(name, options) {
                    this.name = name;
                    this.detail = options ? options.detail : null;
                }
            }
        };

        env.JSON = JSON;
        env.Error = Error;

        // Compile the context for the VM
        vm.createContext(env);
        return env;
    }

    // Test: Verifies that data originating from the native Android app is correctly
    // wrapped in a CustomEvent ('geckoview') and dispatched into the webpage's DOM.
    await t.test('receives message from app and dispatches to webpage', () => {
        const env = createMockEnvironment();
        vm.runInContext(scriptContent, env);

        const testResponse = { data: "test" };

        assert.ok(env.addListenerCb, 'onMessage listener should be registered');
        env.addListenerCb(testResponse);

        assert.strictEqual(env.dispatchedEvents.length, 1);
        const event = env.dispatchedEvents[0];
        assert.strictEqual(event.name, 'geckoview');
        assert.deepStrictEqual(event.detail, { ...testResponse, cloned: true });
    });

    // Test: Verifies that 'geckoview-event' CustomEvents fired by the webpage
    // are intercepted, serialized, and sent to the native Android app.
    await t.test('receives message from webpage and sends to app', () => {
        const env = createMockEnvironment();
        vm.runInContext(scriptContent, env);

        assert.ok(env.eventListeners['geckoview-event'], 'geckoview-event listener should be registered');
        const webEvent = new env.CustomEvent('geckoview-event', { detail: { info: "from web" } });
        env.eventListeners['geckoview-event'].forEach(cb => cb(webEvent));

        assert.strictEqual(env.postMessageCalled.length, 1);
        assert.strictEqual(env.postMessageCalled[0], JSON.stringify({ info: "from web" }));
    });

    // Test: Ensures that exceptions thrown during the cloning or dispatching process
    // (e.g., due to invalid data structures) are caught and logged without crashing.
    await t.test('handles geckoview emit errors gracefully', () => {
        const env = createMockEnvironment();
        env.cloneInto = () => { throw new env.Error("Mock error"); };
        vm.runInContext(scriptContent, env);

        env.addListenerCb({ test: "data" });

        assert.ok(env.errorLogged);
        assert.strictEqual(env.errorLogged.msg, "Geckoview emit error");
        assert.strictEqual(env.errorLogged.err.message, "Mock error");
    });
});