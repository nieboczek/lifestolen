/// Core variables
const app = document.getElementById("app");
let bridge = null;

/// State variables
let categories = [];
let modules = [];

function handleModuleToggle(moduleId, enabled) {
  const module = modules.find((m) => m.id === moduleId);
  if (module) {
    module.enabled = enabled;
    render();
  }
}

function Module(module) {
  const root = document.createElement("div");

  const p = document.createElement("p");
  p.textContent = module.id;
  root.appendChild(p);

  return root;
}

function Category(name) {
  const modulesInCategory = modules.filter((m) => m.category === name);
  const root = document.createElement("div");

  for (const module of modulesInCategory) {
    root.appendChild(Module(module));
  }

  return root;
}

function render() {
  console.log(modules);
  const root = document.createElement("div");

  for (const name of categories) {
    root.appendChild(Category(name));
  }

  if (app.children.length > 0) {
    app.removeChild(app.children[0]);
  }
  app.appendChild(root);
}

function setup() {
  categories = [...new Set(modules.map((m) => m.category))];
}

async function connectBridgeWhenAvailable() {
  const candidate = globalThis.grapheneBridge;
  if (!candidate || typeof candidate.request !== "function") {
    setTimeout(connectBridgeWhenAvailable, 50);
    return;
  }

  bridge = candidate;

  const response = await bridge.request("ready", "1");
  modules = response.modules;
  setup();
  render();
}

function devInjectBridge() {
  globalThis.grapheneBridge = {
    request: async function () {
      return {
        modules: [
          {
            category: "Combat",
            id: "KillAura",
          },
          {
            category: "Combat",
            id: "Proximity",
          },
          {
            category: "Movement",
            id: "FakeLag",
          },
        ],
      };
    },
  };
}

document.addEventListener("DOMContentLoaded", connectBridgeWhenAvailable);
