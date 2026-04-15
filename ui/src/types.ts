export type SettingValue = number | boolean;

export type Setting = {
    name: string;
    value: SettingValue;
    type: 'float' | 'boolean';
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
