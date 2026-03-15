const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');
const vm = require('vm');

test('background.js tests', async (t) => {
    const scriptPath = path.join(__dirname, 'background.js');
    const scriptContent = fs.readFileSync(scriptPath, 'utf8');

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
            document: {
                defaultView: { isDefaultView: true },
                dispatchEvent: (event) => { env.dispatchedEvents.push(event); },
                addEventListener: (name, cb) => {
                    if (!env.eventListeners[name]) env.eventListeners[name] = [];
                    env.eventListeners[name].push(cb);
                }
            },
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

        vm.createContext(env);
        return env;
    }

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

    await t.test('receives message from webpage and sends to app', () => {
        const env = createMockEnvironment();
        vm.runInContext(scriptContent, env);

        assert.ok(env.eventListeners['geckoview-event'], 'geckoview-event listener should be registered');
        const webEvent = new env.CustomEvent('geckoview-event', { detail: { info: "from web" } });
        env.eventListeners['geckoview-event'].forEach(cb => cb(webEvent));

        assert.strictEqual(env.postMessageCalled.length, 1);
        assert.strictEqual(env.postMessageCalled[0], JSON.stringify({ info: "from web" }));
    });

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
