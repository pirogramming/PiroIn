import { useState, useEffect } from 'react';
import styles from './CurriculumPage.module.css';

const CurriculumCreate = () => {
    const [title, setTitle] = useState("")

    const onChangeTitle = (e) => {
        setTitle(e.target.value);
    }
    return (
        <div>
            <input placeholder='세션 제목'/>
        </div>
    );
};

export default CurriculumCreate;