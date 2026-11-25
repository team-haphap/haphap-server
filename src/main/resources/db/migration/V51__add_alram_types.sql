ALTER TABLE alram DROP CONSTRAINT alram_type_check;
ALTER TABLE alram ADD CONSTRAINT alram_type_check
    CHECK (type IN (
                    'NEW_STAGE_REGISTRATION',
                    'STAGE_REGISTERED',
                    'STAGE_PASSED',
                    'PASS_CARD',
                    'POSTING_CLOSED',
                    'DEADLINE',
                    'ANNOUNCEMENT'
        ));