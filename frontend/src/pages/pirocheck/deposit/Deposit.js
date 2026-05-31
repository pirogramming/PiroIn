import { useState, useEffect } from 'react';
import { authFetch } from '../../../utils/Api';
import styles from './Deposit.module.css';

const IS_MOCK = false

function Deposit() {
    const [deposit, setDeposit] = useState(null);

    useEffect(() => {
        authFetch('/api/deposit/me')
            .then(r => r.json())
            .then(data => setDeposit(data))
            .catch(() => {}); 
    }, []);

    if (!deposit) return null;

    return (
        <div className={styles.container}>
            <div className={styles.title}>DEPOSIT CHECK</div>

            <div className={styles.amountBox}>
                <div className={styles.amountLabel}>잔여 보증금</div>
                <div className={styles.amountValue}>{deposit.amount.toLocaleString()}원</div>
            </div>

            <div className={styles.itemList}>
                <div className={styles.item}>
                    <span className={styles.itemLabel}>과제 차감</span>
                    <span className={styles.itemValue}>{deposit.descentAssignment.toLocaleString()}원</span>
                </div>
                <div className={styles.item}>
                    <span className={styles.itemLabel}>출석 차감</span>
                    <span className={styles.itemValue}>{deposit.descentAttendance.toLocaleString()}원</span>
                </div>
                <div className={styles.item}>
                    <span className={styles.itemLabel}>보증금 방어권</span>
                    <span className={styles.itemValue}>{deposit.ascentDefence.toLocaleString()}원</span>
                </div>
            </div>
        </div>
    );
}

export default Deposit;