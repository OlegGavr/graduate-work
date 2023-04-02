import type {UpdateCells, UpdateRow} from "../../../../common/types";

export function getCollapsedHeader(header: UpdateRow): string[] {
    return (header.cells as UpdateCells[]).reduce((prevValue: string[], currentValue) => {
        if (currentValue.type !== "chevron-header") {
            return prevValue;
        }

        if ("isExpanded" in currentValue && !currentValue.isExpanded) {
            return [
                ...prevValue,
                currentValue.chevronId as string,
            ];
        }

        return prevValue;
    }, []);
}
