import type {ChevronCell} from "@silevis/reactgrid/src/core";
import type {UpdateRow} from "../../../../common/types";

export const findChevronCell = (row?: UpdateRow | null) =>
    row ? row.cells.find((cell) => cell.type === "chevron") as ChevronCell | undefined : undefined;
