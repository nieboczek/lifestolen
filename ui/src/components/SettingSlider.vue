<script setup lang="ts">
import { computed, ref } from 'vue';
import type { Setting } from '@/types';

const props = defineProps<{
    setting: Setting;
}>();

const emit = defineEmits<{
    change: [name: string, value: number];
}>();

const min = computed(() => props.setting.min ?? 0);
const max = computed(() => props.setting.max ?? 100);
const step = computed(() => props.setting.step ?? 1);
const value = computed(() => props.setting.value as number);
const precision = computed(() => {
    const stepStr = step.value.toString();
    if (stepStr.includes('.')) {
        return stepStr.split('.')[1]!.length;
    }
    return 0;
});

const sliderTrack = ref<HTMLElement | null>(null);

function snap(rawValue: number): number {
    const snapped = min.value + Math.round((rawValue - min.value) / step.value) * step.value;
    return Math.min(max.value, Math.max(min.value, snapped));
}

function valueFromClientX(clientX: number): number {
    const trackRect = sliderTrack.value?.getBoundingClientRect();
    if (!trackRect || trackRect.width <= 0) {
        return min.value;
    }

    const percent = Math.min(1, Math.max(0, (clientX - trackRect.left) / trackRect.width));
    const rawValue = min.value + percent * (max.value - min.value);
    return snap(rawValue);
}

function onMouseDown(event: MouseEvent) {
    event.preventDefault();
    const nextValue = valueFromClientX(event.clientX);
    emit('change', props.setting.name, nextValue);

    function onMouseMove(e: MouseEvent) {
        const newValue = valueFromClientX(e.clientX);
        emit('change', props.setting.name, newValue);
    }

    function onMouseUp() {
        window.removeEventListener('mousemove', onMouseMove);
        window.removeEventListener('mouseup', onMouseUp);
    }

    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
}

function toPercent(value: number): number {
    const span = max.value - min.value;
    if (span <= 0) return 0;
    return ((value - min.value) / span) * 100;
}

const fillPercent = computed(() => toPercent(value.value ?? min.value));
</script>

<template>
    <div class="setting-oneline">
        <span class="setting-name">{{ setting.name }}</span>
        <span class="setting-slider-value">{{ value.toFixed(precision) }} {{ setting.unit ?? '' }}</span>
    </div>
    <div class="slider-container">
        <div class="slider-track" ref="sliderTrack">
            <div class="slider-selection" :style="{ width: `${fillPercent}%` }"></div>
            <div class="slider-thumb" :style="{ left: `${fillPercent}%` }" @mousedown="onMouseDown"></div>
        </div>
    </div>
</template>

<style>
.slider-container {
    position: relative;
    height: 20px;
    margin-top: 4px;
}

.slider-track {
    position: absolute;
    left: 2px;
    right: -2px;
    top: 6px;
    height: 8px;
    background: #444;
    border-radius: 4px;
}

.slider-selection {
    position: absolute;
    left: 0;
    top: 0;
    height: 8px;
    background: var(--brand);
    border-radius: 4px;
}

.slider-thumb {
    position: absolute;
    top: -6px;
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
