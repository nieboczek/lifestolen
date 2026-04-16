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

const isDragging = ref(false);
const pos = ref({ x: 0, y: 0});
const dragOffset = ref({ x: 0, y: 0 });
const optionsShown = ref<string>();

function onMouseDown(e: MouseEvent) {
    const el = (e.currentTarget as HTMLElement).parentElement!;
    const rect = el.getBoundingClientRect();
    dragOffset.value = { x: e.clientX - rect.left, y: e.clientY - rect.top };
    if (pos.value === null) {
        pos.value = { x: rect.left, y: rect.top };
    }
    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
    isDragging.value = true;
}

function onMouseMove(e: MouseEvent) {
    if (!isDragging.value) return;
    pos.value = {
        x: e.clientX - dragOffset.value.x,
        y: e.clientY - dragOffset.value.y
    };
}

function onMouseUp() {
    isDragging.value = false;
    window.removeEventListener('mousemove', onMouseMove);
    window.removeEventListener('mouseup', onMouseUp);
}

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
    <div class="category" :class="{ dragging: isDragging }" :style="pos !== null ? { position: 'fixed', left: pos!.x + 'px', top: pos!.y + 'px' } : {}">
        <h1 class="category-header" @mousedown="onMouseDown">{{ name }}</h1>
        <p class="separator"></p>
        <Module v-for="module in modules" :module="module" :showSettings="optionsShown === module.id"
            @toggle-module="toggleModule(module)" @toggle-show-settings="toggleShowSettings(module)"
            @update-setting="(n, v) => updateSetting(module, n, v)" />
    </div>
</template>

<style>
.category-header {
    margin: 0;
    padding: 4px 8px;
    cursor: grab;
}

.category.dragging > .category-header {
    cursor: grabbing;
}

.separator {
    width: 256px;
    height: 1px;
    margin: 0;
    background: #333;
}

.category.dragging {
    z-index: 1000;
    position: fixed;
}

.category {
    width: 256px;
    min-height: 512px;
    color: #fff;
    background: black;
    border-radius: 8px;
}
</style>
