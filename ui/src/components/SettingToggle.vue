<script setup lang="ts">
import { ref } from 'vue';
import type { Setting } from '@/types';

const props = defineProps<{
    setting: Setting;
}>();

const emit = defineEmits<{
    change: [name: string, value: boolean];
}>();

const isChecked = ref(props.setting.value as boolean);

function toggle() {
    isChecked.value = !isChecked.value;
    emit('change', props.setting.name, isChecked.value);
}
</script>

<template>
    <div class="setting-oneline">
    <span class="setting-name">{{ setting.name }}</span>
    <button class="toggle-switch" :class="{ checked: isChecked }" @click="toggle">
        <span class="toggle-head" />
    </button>
    </div>
</template>

<style>
.toggle-switch {
    position: relative;
    width: 48px;
    height: 24px;
    background: #444;
    border-radius: 12px;
    border: none;
    cursor: pointer;
    margin-left: auto;
    transition: background 0.2s;
}

.toggle-switch.checked {
    background: var(--brand);
}

.toggle-head {
    position: absolute;
    top: 2px;
    left: 2px;
    width: 20px;
    height: 20px;
    background: black;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    transition: left 0.2s;
}

.toggle-switch.checked .toggle-head {
    left: 26px;
}
</style>