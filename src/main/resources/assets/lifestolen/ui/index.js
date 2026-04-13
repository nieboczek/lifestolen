let bridge = null;

async function connectBridgeWhenAvailable() {
  const candidate = globalThis.grapheneBridge;
  if (!candidate || typeof candidate.request !== "function") {
    setTimeout(connectBridgeWhenAvailable, 50);
    return;
  }

  bridge = candidate;
  await bridge.emit("ready", "1");
  console.log("Bridge is ready");
  run();
}

function run() {
  const initUnsubscribe = bridge.on("init", (payload) => {
    console.log(payload.modules);
    initUnsubscribe();
  });
}

connectBridgeWhenAvailable();
