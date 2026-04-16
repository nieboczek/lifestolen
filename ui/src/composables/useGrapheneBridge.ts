import type { Bridge, Module, Setting } from '@/types';
import { ref, onMounted } from 'vue';

function createDummyBridge(modules: Module[]): Bridge {
    const listeners: Record<string, Set<(payload: any) => void>> = {};
    const handlers: Record<string, (payload: any) => any> = {};

    return {
        on(channel: string, listener: (payload: any) => void) {
            if (!listeners[channel]) listeners[channel] = new Set();
            listeners[channel].add(listener);
            return () => listeners[channel]?.delete(listener);
        },
        off(channel: string, listener: (payload: any) => void) {
            listeners[channel]?.delete(listener);
        },
        handle(channel: string, handler: (payload: any) => any) {
            handlers[channel] = handler;
            return () => delete handlers[channel];
        },
        isReady() {
            return true;
        },
        onReady(callback: () => void) {
            callback();
            return () => {};
        },
        emit(channel: string, payload: any) {
            if (channel === 'toggleModule') {
                const mod = modules.find(m => m.id === payload.id);
                if (mod) mod.enabled = payload.enabled;
            } else if (channel === 'updateSetting') {
                const mod = modules.find(m => m.id === payload.moduleId);
                const setting = mod?.settings.find(s => s.name === payload.name);
                if (setting) setting.value = payload.value;
            }
        },
        request(channel: string, _payload: any): Promise<any> {
            return new Promise((resolve) => {
                if (channel === 'ready') {
                    setTimeout(() => {
                        resolve({ modules });
                    }, 100);
                } else if (handlers[channel]) {
                    setTimeout(() => {
                        resolve(handlers[channel]!(_payload));
                    }, 100);
                }
            });
        }
    };
}

const dummyModules: Module[] = [
    {
        id: 'FakeLag',
        category: 'Movement',
        enabled: false,
        settings: [
            { name: 'Delay', value: [300, 600], type: 'intRange', min: 0, max: 1000, unit: 'ms' },
            { name: 'Recoil Time', value: 200, type: 'int', min: 0, max: 1000, unit: 'ms' }
        ]
    },
    {
        id: 'KillAura',
        category: 'Combat',
        enabled: false,
        settings: [
            { name: 'Range', value: 3.0, type: 'float', min: 1.0, max: 4.0, unit: 'blocks' },
            { name: 'Attack Only Players', value: true, type: 'boolean' }
        ]
    },
    {
        id: 'Proximity',
        category: 'Combat',
        enabled: false,
        settings: [
            { name: 'Player Whitelist', value: [], type: 'boolean' }
        ]
    }
];

export function useGrapheneBridge() {
    const bridge = ref<Bridge | null>(null);

    onMounted(() => {
        console.log("Connecting to Graphene Bridge...");
        const poll = setInterval(() => {
            const candidate = (globalThis as any).grapheneBridge;
            if (candidate && typeof candidate.request === "function") {
                console.log("Graphene Bridge connected.");
                bridge.value = candidate;
                clearInterval(poll);
            }
        }, 50);

        setTimeout(() => {
            if (!bridge.value && document.getElementById('__vue-devtools-container__')) {
                console.log("Using dummy Graphene Bridge (devtools mode).");
                bridge.value = createDummyBridge(dummyModules);
            }
        }, 500);
    });

    return bridge;
}
