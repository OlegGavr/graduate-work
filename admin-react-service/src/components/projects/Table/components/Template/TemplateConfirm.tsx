import React from "react";
import {ApplyCostProjectTemplateVariantsDto} from "gateway-service-api-react-client";
import {CustomConfirm} from "../../../../../common/components/confirm";

// enum ActionsEnum {
//     Replace = "0",
//     Append = "1",
// }

type TemplateConfirmProps = {
    onClose(): void;
    templateId: string;
    onLoadTemplate: (templateId: string, variant: ApplyCostProjectTemplateVariantsDto) => void;
}

export const TemplateConfirm = (props: TemplateConfirmProps) => {
    const {onClose, templateId, onLoadTemplate} = props;
    // const [action, setAction] = useState(ActionsEnum.Replace);

    const content = (
        <div>
            <h1>Все текущие работы в проекте будут удалены</h1>
            <h1>Хотите продолжить?</h1>
            {/*<RadioGroup*/}
            {/*    defaultValue={ActionsEnum.Replace}*/}
            {/*    className={styles["radio-group"]}*/}
            {/*    onChange={(e) => {*/}
            {/*        setAction(e.target.value as unknown as ActionsEnum);*/}
            {/*    }}*/}
            {/*>*/}
            {/*    <FormControlLabel value={ActionsEnum.Replace} label="Заменить работы" control={<Radio size="small"/>}/>*/}
            {/*    <FormControlLabel value={ActionsEnum.Append} label="Добавить работы (в конец списка)"*/}
            {/*                      control={<Radio size="small"/>}/>*/}
            {/*</RadioGroup>*/}
        </div>
    );

    const onClick = () => {
        onLoadTemplate(templateId, ApplyCostProjectTemplateVariantsDto.Replace);
        // switch (action) {
        //     case ActionsEnum.Append: {
        //         onLoadTemplate(templateId, ApplyCostProjectTemplateVariantsDto.Append);
        //         break;
        //     }
        //     case ActionsEnum.Replace: {
        //         onLoadTemplate(templateId, ApplyCostProjectTemplateVariantsDto.Replace);
        //         break;
        //     }
        // }
    };

    return (
        <CustomConfirm
            className="confirm-template"
            header="Загрузка шаблона"
            content={content}
            onClick={onClick}
            onClose={onClose}/>
    );
};
