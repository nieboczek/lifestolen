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
