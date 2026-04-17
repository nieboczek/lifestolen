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
    settings: SettingInfo[];
}

interface SettingInfo {
    name: string;
    value: SettingValue;
    type: 'float' | 'boolean' | 'int' | 'intRange';
    min?: number;
    max?: number;
    step?: number;
    unit?: string;
}

interface UpdateSettingPayload {
    moduleId: string;
    name: string;
    value: SettingValue;
}

watch(bridge, (newBridge) => {
    if (!newBridge) {
        return;
    }

    newBridge.onReady(() => {
        console.log("Requesting ready response from bridge");
        bridge.value!.request<null, { modules: ModuleInfo[] }>('ready', null).then((data) => {
            console.log("Bridge responded with data payload");
            modules.value = data.modules.map(m => ({
                id: m.id,
                category: m.category,
                enabled: m.enabled,
                settings: m.settings
            }));
        });
    });

    newBridge.on<UpdateSettingPayload>('updateSetting', (payload) => {
        const m = modules.value.find(m => m.id === payload.moduleId);
        if (!m) return;

        if (payload.name === "Enabled") {
            m.enabled = payload.value as boolean;
        } else {
            const s = m.settings.find(s => s.name === payload.name);
            s!.value = payload.value;
        }
    });
});

function toggleModule(moduleId: string) {
    const m = modules.value.find(m => m.id === moduleId);
    if (m) {
        m.enabled = !m.enabled;
        bridge.value!.emit<UpdateSettingPayload>('updateSetting', { moduleId, name: "Enabled", value: m.enabled });
    }
}

function updateSetting(moduleId: string, settingName: string, value: SettingValue) {
    const m = modules.value.find(m => m.id === moduleId);
    const s = m?.settings.find(s => s.name === settingName);
    if (s) {
        s.value = value;
        bridge.value?.emit<UpdateSettingPayload>('updateSetting', { moduleId, name: settingName, value });
    }
}

</script>

<template>
    <Category name="Combat" :modules="modules.filter(m => m.category === 'Combat')" @toggle-module="toggleModule" @update-setting="updateSetting" />
    <Category name="Movement" :modules="modules.filter(m => m.category === 'Movement')" @toggle-module="toggleModule" @update-setting="updateSetting" />
</template>
