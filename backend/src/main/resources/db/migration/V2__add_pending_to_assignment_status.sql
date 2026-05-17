ALTER TABLE assignment_item
    DROP CONSTRAINT chk_assignment_item_submitted;

ALTER TABLE assignment_item
    ADD CONSTRAINT chk_assignment_item_submitted
        CHECK (submitted IN (
                             'SUCCESS',
                             'INSUFFICIENT',
                             'FAILURE',
                             'PENDING'
            ));