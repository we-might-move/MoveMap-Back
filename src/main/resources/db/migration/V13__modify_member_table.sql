/* email 칼럼과 password 칼럼의 NOT NULL 제약조건 삭제 */

ALTER TABLE member
    ALTER COLUMN email DROP NOT NULL;

ALTER TABLE member
    ALTER COLUMN password DROP NOT NULL;