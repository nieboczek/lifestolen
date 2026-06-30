<script setup lang="ts">
import { computed, ref } from 'vue';
import type { Setting } from '@/types';

const props = defineProps<{
    setting: Setting;
}>();

const emit = defineEmits<{
    change: [name: string, value: number];
}>();

function intToARGB(value: number) {
    return {
        a: (value >>> 24) & 0xFF,
        r: (value >>> 16) & 0xFF,
        g: (value >>> 8) & 0xFF,
        b: value & 0xFF,
    };
}

function argbToInt(a: number, r: number, g: number, b: number) {
    return ((a << 24) | (r << 16) | (g << 8) | b) >>> 0;
}

function rgbToHsv(r: number, g: number, b: number) {
    r /= 255; g /= 255; b /= 255;
    const max = Math.max(r, g, b);
    const min = Math.min(r, g, b);
    const d = max - min;
    let h = 0;
    const s = max === 0 ? 0 : d / max;
    const v = max;
    if (d !== 0) {
        switch (max) {
            case r: h = ((g - b) / d + (g < b ? 6 : 0)) / 6; break;
            case g: h = ((b - r) / d + 2) / 6; break;
            case b: h = ((r - g) / d + 4) / 6; break;
        }
    }
    return { h, s, v };
}

function hsvToRgb(h: number, s: number, v: number) {
    h = ((h % 1) + 1) % 1;
    const i = Math.floor(h * 6);
    const f = h * 6 - i;
    const p = v * (1 - s);
    const q = v * (1 - f * s);
    const t = v * (1 - (1 - f) * s);
    let r: number, g: number, b: number;
    switch (i % 6) {
        case 0: r = v; g = t; b = p; break;
        case 1: r = q; g = v; b = p; break;
        case 2: r = p; g = v; b = t; break;
        case 3: r = p; g = q; b = v; break;
        case 4: r = t; g = p; b = v; break;
        default: r = v; g = p; b = q;
    }
    return { r: Math.round(r * 255), g: Math.round(g * 255), b: Math.round(b * 255) };
}

const colorValue = ref(props.setting.value as number);
const argb = computed(() => intToARGB(colorValue.value));
const hsv = computed(() => rgbToHsv(argb.value.r, argb.value.g, argb.value.b));

const svSquare = ref<HTMLElement | null>(null);
const hueSlider = ref<HTMLElement | null>(null);
const alphaSlider = ref<HTMLElement | null>(null);

function emitChange() {
    emit('change', props.setting.name, colorValue.value);
}

function onSVMouseDown(event: MouseEvent) {
    event.preventDefault();
    updateSV(event);
    const onMove = (e: MouseEvent) => updateSV(e);
    const onUp = () => {
        window.removeEventListener('mousemove', onMove);
        window.removeEventListener('mouseup', onUp);
    };
    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseup', onUp);
}

function updateSV(event: MouseEvent) {
    const rect = svSquare.value?.getBoundingClientRect();
    if (!rect) return;
    const s = Math.max(0, Math.min(1, (event.clientX - rect.left) / rect.width));
    const v = Math.max(0, Math.min(1, 1 - (event.clientY - rect.top) / rect.height));
    const rgb = hsvToRgb(hsv.value.h, s, v);
    colorValue.value = argbToInt(argb.value.a, rgb.r, rgb.g, rgb.b);
    emitChange();
}

function onHueMouseDown(event: MouseEvent) {
    event.preventDefault();
    updateHue(event);
    const onMove = (e: MouseEvent) => updateHue(e);
    const onUp = () => {
        window.removeEventListener('mousemove', onMove);
        window.removeEventListener('mouseup', onUp);
    };
    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseup', onUp);
}

function updateHue(event: MouseEvent) {
    const rect = hueSlider.value?.getBoundingClientRect();
    if (!rect) return;
    const h = Math.max(0, Math.min(1, (event.clientY - rect.top) / rect.height));
    const rgb = hsvToRgb(h, hsv.value.s, hsv.value.v);
    colorValue.value = argbToInt(argb.value.a, rgb.r, rgb.g, rgb.b);
    emitChange();
}

function onAlphaMouseDown(event: MouseEvent) {
    event.preventDefault();
    updateAlpha(event);
    const onMove = (e: MouseEvent) => updateAlpha(e);
    const onUp = () => {
        window.removeEventListener('mousemove', onMove);
        window.removeEventListener('mouseup', onUp);
    };
    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseup', onUp);
}

function updateAlpha(event: MouseEvent) {
    const rect = alphaSlider.value?.getBoundingClientRect();
    if (!rect) return;
    const a = Math.max(0, Math.min(1, 1 - (event.clientY - rect.top) / rect.height));
    colorValue.value = argbToInt(Math.round(a * 255), argb.value.r, argb.value.g, argb.value.b);
    emitChange();
}

const hueColor = computed(() => {
    const { r, g, b } = hsvToRgb(hsv.value.h, 1, 1);
    return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
});

const rgbColor = computed(() => {
    const { r, g, b } = hsvToRgb(hsv.value.h, hsv.value.s, hsv.value.v);
    return { r, g, b };
});

const alphaGradient = computed(() => {
    const { r, g, b } = rgbColor.value;
    return `linear-gradient(to top, rgba(${r},${g},${b},0), rgba(${r},${g},${b},1))`;
});

const svThumbX = computed(() => hsv.value.s * 100);
const svThumbY = computed(() => (1 - hsv.value.v) * 100);
const hueThumbY = computed(() => hsv.value.h * 100);
const alphaThumbY = computed(() => (1 - argb.value.a / 255) * 100);
</script>

<template>
    <div class="setting-color">
        <div class="setting-oneline">
            <span class="setting-name">{{ setting.name }}</span>
        </div>
        <div class="color-picker">
            <div class="sv-square" ref="svSquare" :style="{ backgroundColor: hueColor }" @mousedown="onSVMouseDown">
                <div class="sv-white" />
                <div class="sv-black" />
                <div class="sv-thumb" :style="{ left: `${svThumbX}%`, top: `${svThumbY}%` }" />
            </div>
            <div class="hue-slider" ref="hueSlider" @mousedown="onHueMouseDown">
                <div class="hue-thumb" :style="{ top: `${hueThumbY}%` }" />
            </div>
            <div class="alpha-slider" ref="alphaSlider" @mousedown="onAlphaMouseDown">
                <div class="alpha-checker" />
                <div class="alpha-overlay" :style="{ background: alphaGradient }" />
                <div class="alpha-thumb" :style="{ top: `${alphaThumbY}%` }" />
            </div>
        </div>
    </div>
</template>

<style>
.setting-color {
    padding: 2px 0;
}

.color-picker {
    display: flex;
    gap: 8px;
    margin-top: 6px;
}

.sv-square {
    position: relative;
    width: 140px;
    height: 140px;
    border-radius: 4px;
    cursor: crosshair;
    flex-shrink: 0;
}

.sv-white {
    position: absolute;
    inset: 0;
    border-radius: 4px;
    background: linear-gradient(to right, #fff, transparent);
}

.sv-black {
    position: absolute;
    inset: 0;
    border-radius: 4px;
    background: linear-gradient(to top, #000, transparent);
}

.sv-thumb {
    position: absolute;
    width: 12px;
    height: 12px;
    border: 2px solid white;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    pointer-events: none;
    box-shadow: 0 0 2px rgba(0,0,0,0.5);
}

.hue-slider {
    position: relative;
    width: 14px;
    height: 140px;
    border-radius: 4px;
    background: linear-gradient(to bottom, #f00 0%, #ff0 17%, #0f0 33%, #0ff 50%, #00f 67%, #f0f 83%, #f00 100%);
    cursor: pointer;
    flex-shrink: 0;
}

.hue-thumb {
    position: absolute;
    left: -3px;
    right: -3px;
    height: 4px;
    background: white;
    border: 1px solid #555;
    border-radius: 2px;
    transform: translateY(-50%);
    pointer-events: none;
}

.alpha-slider {
    position: relative;
    width: 14px;
    height: 140px;
    border-radius: 4px;
    cursor: pointer;
    flex-shrink: 0;
}

.alpha-checker {
    position: absolute;
    inset: 0;
    border-radius: 4px;
    background-image:
        linear-gradient(45deg, #888 25%, transparent 25%),
        linear-gradient(-45deg, #888 25%, transparent 25%),
        linear-gradient(45deg, transparent 75%, #888 75%),
        linear-gradient(-45deg, transparent 75%, #888 75%);
    background-size: 8px 8px;
    background-position: 0 0, 0 4px, 4px -4px, -4px 0px;
}

.alpha-overlay {
    position: absolute;
    inset: 0;
    border-radius: 4px;
}

.alpha-thumb {
    position: absolute;
    left: -3px;
    right: -3px;
    height: 4px;
    background: white;
    border: 1px solid #555;
    border-radius: 2px;
    transform: translateY(-50%);
    pointer-events: none;
}
</style>
