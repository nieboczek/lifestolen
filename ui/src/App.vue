<script setup lang="ts">
import type { Module, SettingValue } from '@/types';
import Category from './components/Category.vue';
import { ref, watch } from 'vue';
import { useGrapheneBridge } from '@/composables/useGrapheneBridge';

const bridge = useGrapheneBridge();
const modules = ref<Module[]>([]);

interface ModuleInfo {
    id: string;
    category: string;
    enabled: boolean;
}

interface TogglePayload {
    id: string;
    enabled: boolean;
}

watch(bridge, (newBridge) => {
    if (!newBridge) {
        return;
    }

    newBridge.onReady(() => {
        const data = bridge.value?.request<null, { modules: ModuleInfo[] }>('ready', null);
        if (data?.modules) {
            modules.value = data.modules.map(m => ({
                id: m.id,
                category: m.category,
                enabled: m.enabled,
                settings: []
            }));
        }
    });

    newBridge.on<TogglePayload>('toggleModule', (payload) => {
        const m = modules.value.find(m => m.id === payload.id);
        if (m) {
            m.enabled = payload.enabled;
        }
    });
});

function toggleModule(id: string) {
    const m = modules.value.find(m => m.id === id);
    if (m) {
        m.enabled = !m.enabled;
        bridge.value?.emit<TogglePayload>('toggleModule', { id, enabled: m.enabled });
    }
}

function updateSetting(moduleId: string, settingName: string, value: SettingValue) {
    const m = modules.value.find(m => m.id === moduleId);
    const s = m?.settings.find(s => s.name === settingName);
    if (s) {
        s.value = value;
    }
}

</script>

<template>
    <Category name="Combat" :modules="modules.filter(m => m.category === 'Combat')" @toggle-module="toggleModule" @update-setting="updateSetting" />
    <Category name="Movement" :modules="modules.filter(m => m.category === 'Movement')" @toggle-module="toggleModule" @update-setting="updateSetting" />
</template>
