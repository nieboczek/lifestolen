<script setup lang="ts">
import type { Module as ModuleType, SettingValue } from '@/types';
import Module from './Module.vue';
import { ref } from 'vue';

const props = defineProps<{
    name: string;
    modules: ModuleType[];
}>();

const emit = defineEmits<{
    toggleModule: [id: string];
    updateSetting: [moduleId: string, settingName: string, value: SettingValue];
}>();

const optionsShown = ref<string>();

function toggleModule(module: ModuleType) {
    emit("toggleModule", module.id);
}

function toggleShowSettings(module: ModuleType) {
    if (optionsShown.value === module.id) {
        optionsShown.value = undefined;
    } else {
        optionsShown.value = module.id;
    }
}

function updateSetting(module: ModuleType, settingName: string, value: SettingValue) {
    emit("updateSetting", module.id, settingName, value)
}
</script>

<template>
    <div class="category">
        <h1 class="category-header">{{ name }}</h1>
        <p class="separator"></p>
        <Module v-for="module in modules" :module="module" :showSettings="optionsShown === module.id"
            @toggle-module="toggleModule(module)" @toggle-show-settings="toggleShowSettings(module)"
            @update-setting="(n, v) => updateSetting(module, n, v)" />
    </div>
</template>

<style>
.category-header {
    margin: 0;
    padding: 4px;
}

.separator {
    width: 256px;
    height: 1px;
    margin: 0;
    background: #333;
}

.category {
    width: 256px;
    min-height: 512px;
    color: #fff;
    background: black;
    border-radius: 8px;
}
</style>
