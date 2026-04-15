<script setup lang="ts">
import type { Module, SettingValue } from '@/types';
import Category from './components/Category.vue';
import { ref } from 'vue';

const modules = ref<Module[]>([
    {
        id: "Kill Aura",
        category: "Combat",
        enabled: false,
        settings: [
            {
                name: "Range",
                value: 3.0,
                type: "float",
                min: 1.0,
                max: 4.0,
                step: 0.01,
                unit: "blocks",
            },
            {
                name: "Attack Only Players",
                value: true,
                type: "boolean"
            }
        ]
    },
    {
        id: "Proximity",
        category: "Combat",
        enabled: false,
        settings: []
    }
]);

function toggleModule(id: string) {
    const m = modules.value.find(m => m.id === id);
    m!.enabled = !(m?.enabled ?? true);
}

function updateSetting(moduleId: string, settingName: string, value: SettingValue) {
    const m = modules.value.find(m => m.id === moduleId);
    const s = m!.settings.find(s => s.name === settingName);
    s!.value = value;
}

</script>

<template>
    <Category name="Combat" :modules="modules" @toggle-module="toggleModule" @update-setting="updateSetting" />
</template>
