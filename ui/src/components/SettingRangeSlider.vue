<script setup lang="ts">
import { computed, ref } from 'vue';
import type { Setting } from '@/types';

const props = defineProps<{
    setting: Setting;
}>();

const emit = defineEmits<{
    change: [name: string, value: number[]];
}>();

const sliderMin = computed(() => Math.min(props.setting.min ?? 0, props.setting.max ?? 100));
const sliderMax = computed(() => Math.max(props.setting.min ?? 0, props.setting.max ?? 100));
const step = computed(() => props.setting.step ?? 1);
const precision = computed(() => {
    const stepStr = step.value.toString();
    if (stepStr.includes('.')) {
        return stepStr.split('.')[1]!.length;
    }
    return 0;
});

const sliderTrack = ref<HTMLElement | null>(null);

function clamp(value: number, min: number, max: number): number {
    return Math.min(max, Math.max(min, value));
}

const values = computed((): [number, number] => {
    const rawValues = Array.isArray(props.setting.value)
        ? props.setting.value
        : [sliderMin.value, sliderMax.value];

    let lower = clamp(rawValues[0] ?? sliderMin.value, sliderMin.value, sliderMax.value);
    let upper = clamp(rawValues[1] ?? sliderMax.value, sliderMin.value, sliderMax.value);

    if (lower > upper) {
        [lower, upper] = [upper, lower];
    }

    return [lower, upper];
});

const lowerValue = computed(() => values.value[0]);
const upperValue = computed(() => values.value[1]);

function toPercent(value: number): number {
    const span = sliderMax.value - sliderMin.value;
    if (span <= 0) {
        return 0;
    }

    return ((value - sliderMin.value) / span) * 100;
}

const lowerPercent = computed(() => toPercent(lowerValue.value));
const upperPercent = computed(() => toPercent(upperValue.value));
const selectedWidth = computed(() => Math.max(upperPercent.value - lowerPercent.value, 0));
const lowerThumbZIndex = computed(() => lowerValue.value >= upperValue.value ? 2 : 1);
const upperThumbZIndex = computed(() => lowerValue.value >= upperValue.value ? 1 : 2);

function snap(value: number): number {
    const snapped = sliderMin.value + Math.round((value - sliderMin.value) / step.value) * step.value;
    return clamp(snapped, sliderMin.value, sliderMax.value);
}

function emitRange(lower: number, upper: number) {
    emit('change', props.setting.name, [lower, upper]);
}

function valueFromClientX(clientX: number): number {
    const trackRect = sliderTrack.value?.getBoundingClientRect();
    if (!trackRect || trackRect.width <= 0) {
        return sliderMin.value;
    }

    const percent = clamp((clientX - trackRect.left) / trackRect.width, 0, 1);
    const rawValue = sliderMin.value + percent * (sliderMax.value - sliderMin.value);
    return snap(rawValue);
}

function updateThumb(index: 0 | 1, clientX: number) {
    const nextValue = valueFromClientX(clientX);

    if (index === 0) {
        emitRange(Math.min(nextValue, upperValue.value), upperValue.value);
        return;
    }

    emitRange(lowerValue.value, Math.max(nextValue, lowerValue.value));
}

function onMouseDown(index: 0 | 1, event: MouseEvent) {
    event.preventDefault();

    function onMouseMove(moveEvent: MouseEvent) {
        updateThumb(index, moveEvent.clientX);
    }

    function onMouseUp() {
        window.removeEventListener('mousemove', onMouseMove);
        window.removeEventListener('mouseup', onMouseUp);
    }

    updateThumb(index, event.clientX);
    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
}
</script>

<template>
    <div class="setting-oneline">
        <span class="setting-name">{{ setting.name }}</span>
        <span class="setting-slider-value">{{ lowerValue.toFixed(precision) }} - {{ upperValue.toFixed(precision) }} {{ setting.unit ?? '' }}</span>
    </div>
    <div class="range-slider-container">
        <div class="range-slider-track" ref="sliderTrack">
            <div class="range-slider-selection" :style="{ left: `${lowerPercent}%`, width: `${selectedWidth}%` }"></div>
            <div class="range-slider-thumb" :style="{ left: `${lowerPercent}%`, zIndex: lowerThumbZIndex }"
                @mousedown="onMouseDown(0, $event)"></div>
            <div class="range-slider-thumb" :style="{ left: `${upperPercent}%`, zIndex: upperThumbZIndex }"
                @mousedown="onMouseDown(1, $event)"></div>
        </div>
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

.range-slider-selection {
    position: absolute;
    top: 0;
    height: 8px;
    background: var(--brand);
    border-radius: 4px;
}

.range-slider-thumb {
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
