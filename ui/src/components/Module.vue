<script setup lang="ts">
import type { Module, SettingValue } from '@/types';
import SettingSlider from './SettingSlider.vue';
import SettingRangeSlider from './SettingRangeSlider.vue';
import SettingToggle from './SettingToggle.vue';
import SettingKeybind from './SettingKeybind.vue';

const props = defineProps<{
    module: Module;
    showSettings: boolean;
}>();

const emit = defineEmits<{
    toggleModule: [];
    toggleShowSettings: [];
    updateSetting: [name: string, value: SettingValue];
}>();

function showOptions(event: MouseEvent) {
    if (event.button === 0) {
        emit("toggleModule");
    } else if (event.button === 2) {
        emit("toggleShowSettings");
    }
}

</script>

<template>
    <div class="module-container">
        <div class="module">
            <button :class="props.module.enabled ? 'module-id module-id-enabled' : 'module-id'"
                @mousedown="showOptions">{{ module.id }}</button>
            <svg class="expand-settings-svg" :class="{ rotated: props.showSettings }" width="10" height="6"
                viewBox="0 0 10 6" @mousedown="showOptions">
                <path d="M1,0.5,5,5,1,9.5" transform="translate(10 0) rotate(90)" fill="none" stroke="currentColor"
                    stroke-width="1.5" />
            </svg>
        </div>
        <Transition name="slide-fade">
            <div v-if="props.showSettings" class="settings-popup">
                <div v-for="setting in module.settings" :key="setting.name" class="setting-item">
                    <SettingSlider v-if="setting.type === 'float' || setting.type === 'int'" :setting="setting" @change="(n, v) => emit('updateSetting', n, v)" />
                    <SettingRangeSlider v-else-if="setting.type === 'intRange'" :setting="setting" @change="(n, v) => emit('updateSetting', n, v)" />
                    <SettingToggle v-else-if="setting.type === 'boolean'" :setting="setting" @change="(n, v) => emit('updateSetting', n, v)" />
                    <SettingKeybind v-else-if="setting.type === 'keybind'" :setting="setting" @change="(n, v) => emit('updateSetting', n, v)" />
                </div>
            </div>
        </Transition>
    </div>
</template>

<style>
.module-container {
    display: flex;
    align-items: center;
    width: 100%;
    position: relative;
}

.module {
    display: flex;
    align-items: center;
    width: 100%;
}

.module-id {
    height: 36px;
    color: #ddd;
    font-size: 20px;
    border: none;
    background: none;
    flex: 1;
    text-align: center;
    margin-left: 28px;
    transition: color 0.2s ease-out, text-shadow 0.2s ease-out;
}

.module-id-enabled {
    color: var(--brand);
    text-shadow: 0 0 8px var(--brand);
}

.module-id:hover {
    color: white;
}

.module-id-enabled:hover {
    color: var(--brand-light);
}

.expand-settings-svg {
    color: #666;
    width: 20px;
    height: 12px;
    margin-right: 8px;
    transition: transform 0.2s ease-out;
}

.expand-settings-svg.rotated {
    transform: rotate(-90deg);
}

.settings-popup {
    position: absolute;
    left: 100%;
    top: 0;
    background: black;
    padding: 8px 20px 8px 16px;
    border-radius: 0 4px 4px 0;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    z-index: 10;
    border-left: #333 1px solid;
    translate: -1px;
    width: 256px;
}

.setting-item {
    background: none;
    border: none;
    color: white;
    text-align: left;
    cursor: pointer;
    font-size: 14px;
    padding: 8px 0;
}

.slide-fade-enter-active {
    transition: all 0.2s ease-out;
    z-index: -10;
}

.slide-fade-leave-active {
    transition: all 0.15s ease-in;
    z-index: -10;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
    transform: translateX(-20px);
    opacity: 0;
}
</style>
