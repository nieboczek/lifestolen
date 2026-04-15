<script setup lang="ts">
import { computed, ref } from 'vue';
import type { Setting } from '@/types';

const props = defineProps<{
    setting: Setting;
}>();

const emit = defineEmits<{
    change: [name: string, value: number[]];
}>();

const values = computed(() => props.setting.value as number[]);
const min = computed(() => props.setting.min ?? 0);
const max = computed(() => props.setting.max ?? 100);

const leftPercent = computed(() => {
    const v = values.value[0] ?? 0;
    const range = max.value - min.value;
    const basePercent = range > 0 ? ((v - min.value) / range) * 100 : 0;
    const offset = getPixelOffset();
    return basePercent + offset;
});
const rightPercent = computed(() => {
    const v = values.value[1] ?? 100;
    const range = max.value - min.value;
    const basePercent = range > 0 ? ((v - min.value) / range) * 100 : 0;
    const offset = -getPixelOffset(true);
    return basePercent + offset;
});

function getPixelOffset(isSpecial: boolean = false): number {
    const container = sliderContainer.value;
    if (!container) return 0;
    const width = container.clientWidth;
    const thumbWidth = 20;
    const trackPadding = 2;
    const specialPadding = isSpecial ? -4.5 : 0; // this is a mess already, so why not add to it :)
    return width > 0 ? ((thumbWidth / 2 + trackPadding + specialPadding) / width) * 100 : 0;
}

const minZIndex = computed(() => leftPercent.value > rightPercent.value ? 2 : 1);
const maxZIndex = computed(() => leftPercent.value > rightPercent.value ? 1 : 2);

const sliderContainer = ref<HTMLElement | null>(null);

function onMouseDown(index: number, event: MouseEvent) {
    event.preventDefault();
    const startX = event.clientX;
    const startValues = [...values.value];
    const range = max.value - min.value;
    const minVal = startValues[0] ?? min.value;
    const maxVal = startValues[1] ?? max.value;
    const containerWidth = sliderContainer.value?.clientWidth ?? 256;

    function onMouseMove(e: MouseEvent) {
        const deltaX = e.clientX - startX;
        const deltaPercent = (deltaX / containerWidth) * 100;
        const deltaValue = (deltaPercent / 100) * range;

        let newValue: number;
        if (index === 0) {
            newValue = Math.round(Math.max(min.value, Math.min(maxVal, minVal + deltaValue)));
        } else {
            newValue = Math.round(Math.min(max.value, Math.max(minVal, maxVal + deltaValue)));
        }

        const newValues = [...startValues];
        newValues[index] = newValue;
        emit('change', props.setting.name, newValues);
    }

    function onMouseUp() {
        window.removeEventListener('mousemove', onMouseMove);
        window.removeEventListener('mouseup', onMouseUp);
    }

    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
}
</script>

<template>
    <div class="setting-oneline">
        <span class="setting-name">{{ setting.name }}</span>
        <span class="setting-slider-value">{{ values[0] ?? 0 }} - {{ values[1] ?? 100 }} {{ setting.unit }}</span>
    </div>
    <div class="range-slider-container" ref="sliderContainer">
        <div class="range-slider-track"></div>
        <div class="range-slider-range" :style="{ left: leftPercent + '%', right: (100 - rightPercent) + '%' }"></div>
        <div class="range-slider-thumb range-slider-thumb-min" :style="{ left: leftPercent + '%', zIndex: minZIndex }"
            @mousedown="onMouseDown(0, $event)"></div>
        <div class="range-slider-thumb range-slider-thumb-max" :style="{ left: rightPercent + '%', zIndex: maxZIndex }"
            @mousedown="onMouseDown(1, $event)"></div>
    </div>
</template>

<style>
.range-slider-container {
    position: relative;
    height: 20px;
    margin-top: 4px;
}

.range-slider-track {
    position: absolute;
    left: 2px;
    right: -2px;
    top: 6px;
    height: 8px;
    background: #444;
    border-radius: 4px;
}

.range-slider-range {
    position: absolute;
    top: 6px;
    height: 8px;
    background: var(--brand);
    border-radius: 4px;
}

.range-slider-thumb {
    position: absolute;
    top: 0;
    width: 16px;
    height: 16px;
    transform: translateX(-50%);
    border: 2px solid white;
    background: black;
    border-radius: 50%;
    cursor: pointer;
}

.setting-slider-value {
    font-size: 14px;
    color: #ddd;
}
</style>
