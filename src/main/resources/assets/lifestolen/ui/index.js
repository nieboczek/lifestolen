let bridge = null;
let modules = [];
let categories = [];

async function connectBridgeWhenAvailable() {
  const candidate = globalThis.grapheneBridge;
  if (!candidate || typeof candidate.request !== "function") {
    setTimeout(connectBridgeWhenAvailable, 50);
    return;
  }

  bridge = candidate;

  const response = await bridge.request("ready", "1");
  modules = response.modules;
  render();
}

function handleModuleToggle(moduleId, enabled) {
  const module = modules.find(m => m.id === moduleId);
  if (module) {
    module.enabled = enabled;
    renderModuleStates();
  }
}

function render() {
  categories = [...new Set(modules.map(m => m.category))];
}

document.addEventListener('DOMContentLoaded', connectBridgeWhenAvailable);
