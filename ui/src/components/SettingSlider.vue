<script setup lang="ts">
import { computed } from 'vue';
import type { Setting } from '@/types';

const props = defineProps<{
    setting: Setting;
}>();

const emit = defineEmits<{
    change: [name: string, value: number];
}>();

const fillPercent = computed(() => {
    return ((props.setting.value as number - props.setting.min!) / (props.setting.max! - props.setting.min!)) * 100;
});
</script>

<template>
    <div class="setting-oneline">
        <span class="setting-name">{{ setting.name }}</span>
        <span class="setting-slider-value">{{ setting.value }} {{ setting.unit }}</span>
    </div>
    <input type="range" class="setting-slider" :min="setting.min" :max="setting.max" :step="setting.step ?? 1" :value="setting.value"
        :style="{ '--value': fillPercent + '%' }"
        @input="emit('change', setting.name, Number(($event.target as HTMLInputElement).value))" />
</template>

<style>
.setting-slider {
    width: 100%;
    background: transparent;
    accent-color: var(--brand);
}

.setting-slider::-webkit-slider-runnable-track {
    height: 8px;
    background: linear-gradient(to right, var(--brand) 0%, var(--brand) var(--value, 50%), #444 var(--value, 50%), #444 100%);
    border-radius: 4px;
}

.setting-slider::-moz-range-track {
    height: 8px;
    background: linear-gradient(to right, var(--brand) 0%, var(--brand) var(--value, 50%), #444 var(--value, 50%), #444 100%);
    border-radius: 4px;
}

.setting-slider::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 16px;
    height: 16px;
    border: 2px solid white;
    background: black;
    border-radius: 50%;
    cursor: pointer;
}

.setting-slider::-moz-range-thumb {
    width: 16px;
    height: 16px;
    border: 2px solid white;
    background: black;
    border-radius: 50%;
    cursor: pointer;
}

.setting-slider-value {
    font-size: 16px;
    color: #ddd;
}
</style>
