import type {DataProvider, LegacyDataProvider} from "react-admin";

export function createDataProvider(
    dataProvider?: Partial<DataProvider | LegacyDataProvider>
): DataProvider | LegacyDataProvider {
    return {
        getList:          () => new Promise(() => { return {data: [], total: 0}; }),
        getOne:           () => new Promise(() => { return {data: {}}; }),
        getMany:          () => new Promise(() => { return {data: []}; }),
        getManyReference: () => new Promise(() => { return {data: [], total: 0}; }),
        create:           () => new Promise(() => { return {data: {}}; }),
        update:           () => new Promise(() => { return {data: {}}; }),
        updateMany:       () => new Promise(() => { return {data: []}; }),
        delete:           () => new Promise(() => { return {data: {}}; }),
        deleteMany:       () => new Promise(() => { return {data: []}; }),
        ...dataProvider,
    };
}
