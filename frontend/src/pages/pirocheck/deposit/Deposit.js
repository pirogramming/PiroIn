import { useState, useEffect } from 'react';
import styles from './Deposit.module.css';

// 임시 데이터
const MOCK_DATA = {
    amount: 100000,
    descentAssignment: 10000,
    descentAttendance: 10000,
    ascentDefence: 10000,
};

const IS_MOCK = true; // 백엔드 연동 시 false로 변경

function Deposit() {
    const [deposit, setDeposit] = useState(null);

    useEffect(() => {
        if (IS_MOCK) {
            setDeposit(MOCK_DATA);
            return;
        }
        fetch('/api/deposit/me')
            .then(r => r.json())
            .then(data => setDeposit(data))
            .catch(() => setDeposit(MOCK_DATA));
    }, []);

    if (!deposit) return null;

    return (
        <div className={styles.container}>
            {IS_MOCK && (
                <div className={styles.mockBanner}>
                    ⚠️ 현재 임시 데이터로 표시 중입니다. 백엔드 연동 후 IS_MOCK을 false로 변경하세요.
                </div>
            )}

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