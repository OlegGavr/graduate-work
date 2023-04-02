import type {CostProjectItemDetailsDto, CostProjectItemDto} from "gateway-service-api-react-client";
import type {KeyCostProjectItemType} from "../../../../common/types";

type KeyCostProjectItemDetailsType = keyof CostProjectItemDetailsDto;

export function getCostItemInfo(item: CostProjectItemDto, columnId: string) {
    const parsedColumnId = (columnId as string).split(".");
    const lastInfoColumn = parsedColumnId.length - 1;
    const preLastInfoColumn = parsedColumnId.length - 2;
    const itemKey = parsedColumnId[preLastInfoColumn] as KeyCostProjectItemType;
    const infoKey = parsedColumnId[lastInfoColumn] as KeyCostProjectItemDetailsType;
    return item[itemKey]![infoKey];
}
