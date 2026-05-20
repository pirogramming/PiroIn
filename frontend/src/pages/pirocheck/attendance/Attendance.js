import { useState, useEffect } from 'react';
import styles from './Attendance.module.css';

function roleReturn(role){
    if (role == "ADMIN") {
        return (
            <>
            <div className={styles.title1}>ATEENDANCE CHECK</div>
            <div className={styles.codebox}>
                <div className={styles.code}></div>
                <div className={styles.code}></div>
                <div className={styles.code}></div>
                <div className={styles.code}></div>
            </div>
            <div className={styles.manage}>
                <button className={styles.create}></button>
                <a className={styles.manage_piro}></a>
            </div>
            </>
        )
    }


    if (role == "MEMBER") {
        return (
            <>
            <div className={styles.title2}>ATTENDANCE CHECK</div>
            <input className placeholder='출석 코드를 입력하세요.'>
                <button>출석</button>
            </input>
            </>
        )
    }
}


export default Attendance;