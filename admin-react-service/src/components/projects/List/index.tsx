import React from "react";
import {
    Button,
    Datagrid,
    List,
    ListContextProvider,
    TextField,
    TopToolbar, useGetList,
} from "react-admin";
import type {
    ListProps
} from "react-admin";
import {CustomDeleteButtonConfirm} from "../../../common/components/delete-confirm";
import {Empty} from "./Empty";
import {useCreateProject} from "./hook";

type ProjectListProps = ListProps & {
    children: React.ReactNode;
}

export function ProjectList(props: ProjectListProps) {
    const onCreate = useCreateProject();

    const {data, total} = useGetList("projects");

    const actions = (
        <TopToolbar>
            <Button onClick={onCreate} label="Create Project"/>
        </TopToolbar>
    );

    // eslint-disable-next-line react/no-unstable-nested-components
    const PostBulkActionButtons = () => (
        <CustomDeleteButtonConfirm confirmTitle="Удаление проектов"
                                   confirmContent="Вы уверены, что хотите удалить проекты?"/>
    );

    return (
        <ListContextProvider value={{data, total}}>
            <List {...props}
                  empty={<Empty/>}
                  pagination={false}
                  actions={actions}
                  bulkActionButtons={<PostBulkActionButtons/>}
            >
                <Datagrid rowClick={() => "show"}>
                    <TextField source="name" sortable={false}/>
                    <TextField source="moneyPerHour" sortable={false}/>
                </Datagrid>
            </List>
        </ListContextProvider>
    );
}
