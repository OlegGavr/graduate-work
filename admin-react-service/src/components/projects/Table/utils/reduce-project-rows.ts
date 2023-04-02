import type {CostProjectDto, CostProjectItemDto} from "gateway-service-api-react-client";
import type {RowItem} from "../../types";

const localAggregate = (index: number) => {
    switch (index) {
        case 0:
            return "Суммарные часы";
        case 1:
            return "Суммарные средства без НДС";
        case 2:
            return "Суммарные средства c НДС 20%";
        default:
            return "";
    }
};

const reduceCostItemRows = (projectCostItems: CostProjectItemDto[]): RowItem[] => {
    const [firstProjectCostItem, ...restProjectCostItems] = projectCostItems;
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const {parentId, ...restFields} = firstProjectCostItem;
    return [
        restFields,
        ...restProjectCostItems
    ];
};

export const reduceProjectRows = (project: CostProjectDto, prevProjects: RowItem[]): RowItem[] => {
    const projectItems = project.projectItems?.length ?
        reduceCostItemRows(project.projectItems) : [];

    return projectItems.map(projectItem => {
        const prevProject = prevProjects.find(item => projectItem.id === item.id);

        return {
            ...projectItem,
            isExpanded: prevProject?.isExpanded || true,
        };
    });
};

export const reduceProjectAggregateRows = (project: CostProjectDto): CostProjectItemDto[] => {
    const aggregate = ("aggregate" in project) ?
        Object.values(project.aggregate!)
            .map((item, index) => {
                return {
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-ignore
                    ...item,
                    id: `aggregate${index}`,
                    name: localAggregate(index)
                };
            }) : [];

    return aggregate.length ?
        reduceCostItemRows(aggregate) : [];
};
