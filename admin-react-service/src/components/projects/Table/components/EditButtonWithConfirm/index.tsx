import {EditButton} from "react-admin";
import {confirmAlert} from "react-confirm-alert";
import {useLocation, useNavigate} from "react-router-dom";
import {CustomConfirm} from "../../../../../common/components/confirm";

export function EditButtonWithConfirm({className}: any) {
    const location = useLocation();
    const navigate = useNavigate();

    const submit = () => {
        confirmAlert({
            // eslint-disable-next-line react/no-unstable-nested-components
            customUI: ({onClose}: any) => {
                return (
                    <CustomConfirm header="Режим редактирования"
                                   content="Вы уверены, что хотите перейти в режим редактирования? Все значения будут пересчитаны, выделенные ячейки могут быть сброшены."
                                   onClick={() => navigate(location.pathname.split("/show")[0])}
                                   onClose={onClose}
                    />
                );
            }
        });
    };

    return (
        <div className={`${className}`}>
            <EditButton onClick={(e) => {
                e.preventDefault();
                submit();
            }}/>
        </div>
    );
}