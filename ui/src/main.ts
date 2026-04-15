import "./assets/main.css";

import { createApp } from "vue";
import App from "./App.vue";

createApp(App).mount("#app");

// Disable context menu
document.addEventListener('contextmenu', event => event.preventDefault());
