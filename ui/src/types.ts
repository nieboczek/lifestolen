export type SettingValue = number | boolean | number[];

export type Setting = {
    name: string;
    value: SettingValue;
    type: 'float' | 'boolean' | 'int' | 'intRange';
    min?: number;
    max?: number;
    step?: number;
    unit?: string;
};

export type Module = {
    id: string;
    category: string;
    enabled: boolean;
    settings: Setting[];
};

export interface Bridge {
    /**
     * Handles events from Java without returning anything.
     * Returns function that can be used to unsubscribe from the events.
     */
    on<P>(channel: string, listener: (payload: P) => void): () => void;
    /** Unsubscribe a listener from events or requests. */
    off(channel: string, listener: (payload: unknown) => void): void;
    /**
     * Handles requests from Java and returns a result.
     * Returns function that can be used to unsubscribe from the requests.
     */
    handle<P, R>(channel: string, handler: (payload: P) => R): () => void;
    /** Returns true when Java side is ready. */
    isReady(): boolean;
    /**
     * Executes callback when Java side is ready.
     * Returns function that can be used to unsubscribe.
     */
    onReady(callback: () => void): () => void;
    /** Emits an event to Java */
    emit<P>(channel: string, payload: P): void;
    /** Requests a result from Java */
    request<P, R>(channel: string, payload: P): Promise<R>;
}
