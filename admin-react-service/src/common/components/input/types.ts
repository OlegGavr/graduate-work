export type CustomInputProps = {
    value?: string | number;
    label: string;
    name: string;
    placeholder?: string;
    className?: string;
    disabled?: boolean;
    maxLength?: number;
    onChange(e: any): void;
}
