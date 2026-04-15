import type { Bridge } from '@/types';
import { ref, onMounted } from 'vue';

export function useGrapheneBridge() {
    const bridge = ref<Bridge | null>(null);

    onMounted(() => {
        const poll = setInterval(() => {
            const candidate = (globalThis as any).grapheneBridge;
            if (candidate && typeof candidate.request === "function") {
                bridge.value = candidate;
                clearInterval(poll);
            }
        }, 50);
    });

    return bridge;
}
