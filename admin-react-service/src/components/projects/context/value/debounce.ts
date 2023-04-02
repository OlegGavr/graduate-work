import {useEffect, useState} from "react";

export function useDebounce<T>(
    value: T,
    delay: number,
    action: (value: T, prevValue?: T) => void,
) {
    const [, setDebouncedValue] = useState<T>(value);

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedValue(prevValue => {
                action(value, prevValue);
                return value;
            });
        }, delay);

        return () => {
            clearTimeout(handler);
        };
        }, [value, delay]
    );
}
