<script setup lang="ts">
import { onMounted, onUnmounted, ref, type MapHTMLAttributes } from 'vue';
import type { Setting } from '@/types';
import { useGrapheneBridge } from '@/composables/useGrapheneBridge';

const bridge = useGrapheneBridge();

interface KeydownPayload {
    code: number;
    displayed: string;
    isReserved: boolean;
}

interface KeynamePayload {
    code: number;
}

interface KeynameResult {
    displayed: string;
}

const props = defineProps<{
    setting: Setting;
}>();

const emit = defineEmits<{
    change: [name: string, value: number];
}>();

const isRecording = ref(false);
const displayed = ref("None");
let keydownHandle: () => void | undefined;

onMounted(() => {
    if (props.setting.value !== 0) {
        bridge.value!.request<KeynamePayload, KeynameResult>("keyname", { code: props.setting.value as number }).then(result => {
            displayed.value = result.displayed;
        })
    }

    keydownHandle = bridge.value!.on("keydown", (payload: KeydownPayload) => {
        if (isRecording.value) {
            displayed.value = payload.isReserved ? "None" : payload.displayed;
            isRecording.value = false;
            emit("change", props.setting.name, payload.isReserved ? 0 : payload.code);
        }
    });
});

onUnmounted(() => {
    keydownHandle ? keydownHandle() : {};
});
</script>

<template>
    <div class="setting-oneline">
        <span class="setting-name">{{ setting.name }}</span>
        <button 
            class="keybind-button" 
            :class="{ recording: isRecording }"
            @click="isRecording = true"
        >
            {{ displayed }}
        </button>
    </div>
</template>

<style>
.keybind-button {
    background: #333;
    color: #fff;
    border: 1px solid #555;
    border-radius: 4px;
    padding: 4px 12px;
    font-family: monospace;
    font-size: 14px;
    cursor: pointer;
    min-width: 80px;
    text-align: center;
    transition: background 0.2s, border-color 0.2s;
}

.keybind-button:hover {
    background: #444;
    border-color: #666;
}

.keybind-button.recording {
    background: var(--brand);
    border-color: var(--brand);
    color: black;
}
</style>
