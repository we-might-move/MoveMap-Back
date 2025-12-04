CREATE TABLE IF NOT EXISTS exercise_video (
                                              id                 BIGSERIAL       PRIMARY KEY,
                                              category_large     VARCHAR(50)  NOT NULL,
                                              category_middle    VARCHAR(100) NOT NULL,
                                              category_small     VARCHAR(100) NOT NULL,
                                              title              VARCHAR(500) NOT NULL,
                                              youtube_url        TEXT         NOT NULL,
                                              youtube_video_id   VARCHAR(50)  NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_exercise_video_youtube_video_id
    ON exercise_video (youtube_video_id);

INSERT INTO exercise_video
(category_large, category_middle, category_small, title, youtube_url, youtube_video_id)
VALUES
    ('국민체력100', '백세편살 운동처방', '집콕운동', '집중력 버프가 필요할 때? (ep1. 뇌를 자극시키는 집중력 향상 운동) 집콕운동', 'https://youtu.be/6AiSi3E3ifs', '6AiSi3E3ifs'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '운동 효과도 더블로가 (ep2. 수다잼 운동잼 짝 스트레칭)', 'https://youtu.be/2gHcoelhHw0', '2gHcoelhHw0'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '깨어나세요 용사여..! (ep3. 체력 +999 강화 운동)', 'https://youtu.be/CRMpWponGIc', 'CRMpWponGIc'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '꽃보다 근력! 꽃중년을 위한 의자운동 (ep4. 부상 없는 어르신 운동)', 'https://youtu.be/6ulvd_mw_uo', '6ulvd_mw_uo'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '[ENG] 뻣뻣한 몸이 10분만에 말랑말랑! (ep5. 10가지 스트레칭 루틴) 10 Stretching Routine', 'https://youtu.be/6ies7bJfYRs', '6ies7bJfYRs'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '키는 쑥쑥! 체형은 바르게 (ep.6 바른 성장을 위한 키즈 요가)', 'https://youtu.be/YevYZNaPgEw', 'YevYZNaPgEw'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '키는 쑥쑥! 체형은 바르게 (ep.6 바른 성장을 위한 키즈 요가)', 'https://youtu.be/4ZLaumFIAfk', '4ZLaumFIAfk'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '일상을 멈출 수 없다면 통증을 멈추세요! (ep.7 장시간 앉아있는 직장인을 위한 하체와 코어 요가)', 'https://youtu.be/clifZdPnX9E', 'clifZdPnX9E'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '일상을 멈출 수 없다면 통증을 멈추세요! (ep.7 장시간 앉아있는 직장인을 위한 하체와 코어 요가)', 'https://youtu.be/JokYLrqdPPo', 'JokYLrqdPPo'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '[ENG] 7번 보기만 해도 코어 찢어짐 ㅋㅋ (ep.8 7-Minute 11 Abs Workout)', 'https://youtu.be/PRG2dS-gYDs', 'PRG2dS-gYDs'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '[ENG] 7번 보기만 해도 코어 찢어짐 ㅋㅋ (ep.8 7-Minute 11 Abs Workout)', 'https://youtu.be/dyuCgeapIys', 'dyuCgeapIys'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '아이유도 인정한 운동효과! (ep.9 후광 효과 스트레칭)', 'https://youtu.be/fm6z-u95BIU', 'fm6z-u95BIU'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '출근길을 업그레이드해드립니다 (ep.10 왕복 가능한 출퇴근 스트레칭)', 'https://youtu.be/C0E5cDIeE7E', 'C0E5cDIeE7E'),
    ('국민체력100', '백세편살 운동처방', '집콕운동', '[ENG] 출근길을 업그레이드해드립니다 (ep.10 왕복 가능한 출퇴근 스트레칭)', 'https://youtu.be/z0XhV6nOdl0', 'z0XhV6nOdl0'),

    ('국민체력100', '백세편살 운동처방', '코어', '플랭크 루틴 1탄', 'https://youtu.be/Oj3FkJZ9h8Y', 'Oj3FkJZ9h8Y'),
    ('국민체력100', '백세편살 운동처방', '코어', '플랭크 루틴 2탄', 'https://youtu.be/f1mbRHNg9RQ', 'f1mbRHNg9RQ'),

    ('국민체력100', '백세편살 운동처방', '둔근', '전신 올인원! 둔근 운동 A 루틴', 'https://youtu.be/6mkFehGUEpA', '6mkFehGUEpA'),
    ('국민체력100', '백세편살 운동처방', '둔근', '전신 올인원! 둔근 운동 B 루틴', 'https://youtu.be/i5XvSoWybBk', 'i5XvSoWybBk'),

    ('국민체력100', '백세편살 운동처방', '전신', '전신운동 A 루틴', 'https://youtu.be/WghfJOLwdeg', 'WghfJOLwdeg'),
    ('국민체력100', '백세편살 운동처방', '전신', '전신운동 B 루틴', 'https://youtu.be/PKsFCeF5HzQ', 'PKsFCeF5HzQ'),

    ('국민체력100', '백세편살 운동처방', '유산소', '버피테스트', 'https://youtu.be/wm8RfE1BYHg', 'wm8RfE1BYHg'),
    ('국민체력100', '백세편살 운동처방', '유산소', '점핑잭', 'https://youtu.be/VCVgc6RfUOE', 'VCVgc6RfUOE'),
    ('국민체력100', '백세편살 운동처방', '유산소', '마운틴클라이머', 'https://youtu.be/n_XQ8wR06vE', 'n_XQ8wR06vE'),
    ('국민체력100', '백세편살 운동처방', '유산소', '발구르기', 'https://youtu.be/kf1nF4yPt_8', 'kf1nF4yPt_8'),

    ('국민체력100', '백세편살 운동처방', '상체', '푸쉬업 1탄', 'https://youtu.be/XpbdTiWENgw', 'XpbdTiWENgw'),
    ('국민체력100', '백세편살 운동처방', '상체', '푸쉬업 2탄', 'https://youtu.be/HHpCIC8gKdg', 'HHpCIC8gKdg'),
    ('국민체력100', '백세편살 운동처방', '상체', '숄더프레스', 'https://youtu.be/2pJz8nZ1fHw', '2pJz8nZ1fHw'),
    ('국민체력100', '백세편살 운동처방', '상체', '백익스텐션', 'https://youtu.be/5dYyezvlRXE', '5dYyezvlRXE'),

    ('국민체력100', '백세편살 운동처방', '하체', '스쿼트 1탄', 'https://youtu.be/hxW2E1A0VvM', 'hxW2E1A0VvM'),
    ('국민체력100', '백세편살 운동처방', '하체', '스쿼트 2탄', 'https://youtu.be/8xZdo92uWUE', '8xZdo92uWUE'),
    ('국민체력100', '백세편살 운동처방', '하체', '런지', 'https://youtu.be/Pq5W8n_U7D0', 'Pq5W8n_U7D0'),
    ('국민체력100', '백세편살 운동처방', '하체', '히프브릿지', 'https://youtu.be/VYcVnT0EhlM', 'VYcVnT0EhlM'),

    ('국민체력100', '백세편살 운동처방', '스트레칭', '전신 스트레칭', 'https://youtu.be/yyIu9C_HhMo', 'yyIu9C_HhMo'),
    ('국민체력100', '백세편살 운동처방', '스트레칭', '어깨 스트레칭', 'https://youtu.be/LTJ2eVH54xk', 'LTJ2eVH54xk'),
    ('국민체력100', '백세편살 운동처방', '스트레칭', '허리 스트레칭', 'https://youtu.be/Hw3h1PxsMho', 'Hw3h1PxsMho'),
    ('국민체력100', '백세편살 운동처방', '스트레칭', '하체 스트레칭', 'https://youtu.be/i4BV6qx5CCE', 'i4BV6qx5CCE'),

    ('국민체력100', '노화예방', '전신', '전신운동 1탄', 'https://youtu.be/yYv84UfBzSI', 'yYv84UfBzSI'),
    ('국민체력100', '노화예방', '전신', '전신운동 2탄', 'https://youtu.be/tN5_pZGwE9E', 'tN5_pZGwE9E'),

    ('국민체력100', '노화예방', '균형', '균형운동 1탄', 'https://youtu.be/0FoZaRKRbhM', '0FoZaRKRbhM'),
    ('국민체력100', '노화예방', '균형', '균형운동 2탄', 'https://youtu.be/LlwU4N43X8Y', 'LlwU4N43X8Y'),

    ('국민체력100', '노화예방', '근력', '근력운동 1탄', 'https://youtu.be/E2x3WAcfSuo', 'E2x3WAcfSuo'),
    ('국민체력100', '노화예방', '근력', '근력운동 2탄', 'https://youtu.be/nVh9Yx8RJWk', 'nVh9Yx8RJWk'),

    ('국민체력100', '노화예방', '스트레칭', '스트레칭 1탄', 'https://youtu.be/vIKtVYk8zFg', 'vIKtVYk8zFg'),
    ('국민체력100', '노화예방', '스트레칭', '스트레칭 2탄', 'https://youtu.be/7pGpOKDtul4', '7pGpOKDtul4');

INSERT INTO exercise_video
(category_large, category_middle, category_small, title, youtube_url, youtube_video_id)
VALUES
    ('국민체력100', '운동처방', '전신', '전신운동 1탄', 'https://youtu.be/CdUlWDOJk2E', 'CdUlWDOJk2E'),
    ('국민체력100', '운동처방', '전신', '전신운동 2탄', 'https://youtu.be/0NwzEoa_Jz4', '0NwzEoa_Jz4'),
    ('국민체력100', '운동처방', '전신', '전신운동 3탄', 'https://youtu.be/CVYH8KnzHho', 'CVYH8KnzHho'),

    ('국민체력100', '운동처방', '유산소', '유산소운동 1탄', 'https://youtu.be/ZtL-DVnPiZc', 'ZtL-DVnPiZc'),
    ('국민체력100', '운동처방', '유산소', '유산소운동 2탄', 'https://youtu.be/a1fTu2Lx7xk', 'a1fTu2Lx7xk'),
    ('국민체력100', '운동처방', '유산소', '유산소운동 3탄', 'https://youtu.be/Rxvj5QPWGeM', 'Rxvj5QPWGeM'),

    ('국민체력100', '운동처방', '상체', '상체운동 1탄', 'https://youtu.be/2EAf9IM7x-Y', '2EAf9IM7x-Y'),
    ('국민체력100', '운동처방', '상체', '상체운동 2탄', 'https://youtu.be/FqR_iFqqXmg', 'FqR_iFqqXmg'),

    ('국민체력100', '운동처방', '하체', '하체운동 1탄', 'https://youtu.be/mYxuxpxHGiQ', 'mYxuxpxHGiQ'),
    ('국민체력100', '운동처방', '하체', '하체운동 2탄', 'https://youtu.be/FqhzOHijRD4', 'FqhzOHijRD4'),
    ('국민체력100', '운동처방', '하체', '하체운동 3탄', 'https://youtu.be/Dndq7HeBghw', 'Dndq7HeBghw'),

    ('국민체력100', '운동처방', '코어', '코어운동 1탄', 'https://youtu.be/6CjdKc7Cync', '6CjdKc7Cync'),
    ('국민체력100', '운동처방', '코어', '코어운동 2탄', 'https://youtu.be/fdwbhohxtAU', 'fdwbhohxtAU'),
    ('국민체력100', '운동처방', '코어', '코어운동 3탄', 'https://youtu.be/gD1oxu9UXuE', 'gD1oxu9UXuE'),

    ('국민체력100', '운동처방', '스트레칭', '스트레칭 1탄', 'https://youtu.be/mG9OXJXnFo4', 'mG9OXJXnFo4'),
    ('국민체력100', '운동처방', '스트레칭', '스트레칭 2탄', 'https://youtu.be/5zYV2pA_IkY', '5zYV2pA_IkY'),
    ('국민체력100', '운동처방', '스트레칭', '스트레칭 3탄', 'https://youtu.be/cou8aCzoNLw', 'cou8aCzoNLw'),

    ('국민체력100', '체력증진', '전신', '전신강화 운동 1탄', 'https://youtu.be/Dl7l4mWo3VA', 'Dl7l4mWo3VA'),
    ('국민체력100', '체력증진', '전신', '전신강화 운동 2탄', 'https://youtu.be/K2pkLIO6kqk', 'K2pkLIO6kqk'),

    ('국민체력100', '체력증진', '유산소', '유산소 1탄', 'https://youtu.be/VcdgqxWlatI', 'VcdgqxWlatI'),
    ('국민체력100', '체력증진', '유산소', '유산소 2탄', 'https://youtu.be/Alc8u-dVjEI', 'Alc8u-dVjEI'),

    ('국민체력100', '체력증진', '상체', '상체 강화운동', 'https://youtu.be/h1M25PcGRaA', 'h1M25PcGRaA'),

    ('국민체력100', '체력증진', '하체', '하체 강화운동 1탄', 'https://youtu.be/2kEjV4o6Gvs', '2kEjV4o6Gvs'),
    ('국민체력100', '체력증진', '하체', '하체 강화운동 2탄', 'https://youtu.be/OZQ0XcMI0HY', 'OZQ0XcMI0HY'),

    ('국민체력100', '체력증진', '근력', '근력 강화운동 1탄', 'https://youtu.be/cgGV8mgK3BM', 'cgGV8mgK3BM'),
    ('국민체력100', '체력증진', '근력', '근력 강화운동 2탄', 'https://youtu.be/2O3dzj7Iz8Q', '2O3dzj7Iz8Q'),

    ('국민체력100', '체력증진', '코어', '코어 강화운동 1탄', 'https://youtu.be/fuhw9Opng6M', 'fuhw9Opng6M'),
    ('국민체력100', '체력증진', '코어', '코어 강화운동 2탄', 'https://youtu.be/3L2UkjJ8e1M', '3L2UkjJ8e1M'),

    ('국민체력100', '체력증진', '스트레칭', '스트레칭 루틴', 'https://youtu.be/jM6_zGq_Ybo', 'jM6_zGq_Ybo'),

    ('국민체력100', '아동운동', '놀이', '아동 신체 놀이 1탄', 'https://youtu.be/vwn2X2gYdQs', 'vwn2X2gYdQs'),
    ('국민체력100', '아동운동', '놀이', '아동 신체 놀이 2탄', 'https://youtu.be/o4iRciMghig', 'o4iRciMghig'),
    ('국민체력100', '아동운동', '놀이', '아동 신체 놀이 3탄', 'https://youtu.be/lS9N6bykTI4', 'lS9N6bykTI4'),

    ('국민체력100', '아동운동', '유산소', '아동 유산소 1탄', 'https://youtu.be/f0dRjRzRk2Q', 'f0dRjRzRk2Q'),
    ('국민체력100', '아동운동', '유산소', '아동 유산소 2탄', 'https://youtu.be/Yl3C9BvXcME', 'Yl3C9BvXcME'),

    ('국민체력100', '아동운동', '기본기', '기본 체력운동 1탄', 'https://youtu.be/Y3OGi8JesgE', 'Y3OGi8JesgE'),
    ('국민체력100', '아동운동', '기본기', '기본 체력운동 2탄', 'https://youtu.be/QMCL0ycLBAA', 'QMCL0ycLBAA'),
    ('국민체력100', '아동운동', '기본기', '기본 체력운동 3탄', 'https://youtu.be/U-ACbz2K0rY', 'U-ACbz2K0rY'),

    ('국민체력100', '아동운동', '근력', '아동 근력운동', 'https://youtu.be/1pN3kLBXjgk', '1pN3kLBXjgk'),

    ('국민체력100', '아동운동', '스트레칭', '아동 스트레칭', 'https://youtu.be/9B8xqe8Vr9w', '9B8xqe8Vr9w'),
    ('국민체력100', '아동운동', '균형', '균형잡기 놀이', 'https://youtu.be/5PfzL2zGEP4', '5PfzL2zGEP4'),
    ('국민체력100', '아동운동', '협응', '협응력 강화운동', 'https://youtu.be/7opmgWphlQ8', '7opmgWphlQ8');

INSERT INTO exercise_video
(category_large, category_middle, category_small, title, youtube_url, youtube_video_id)
VALUES
    ('국민체력100', '청소년 체력증진', '전신', '청소년 전신운동 1탄', 'https://youtu.be/lhXlDM_rNn0', 'lhXlDM_rNn0'),
    ('국민체력100', '청소년 체력증진', '전신', '청소년 전신운동 2탄', 'https://youtu.be/g6pKEzNTziY', 'g6pKEzNTziY'),

    ('국민체력100', '청소년 체력증진', '유산소', '청소년 유산소 1탄', 'https://youtu.be/jICbCNWmvVc', 'jICbCNWmvVc'),
    ('국민체력100', '청소년 체력증진', '유산소', '청소년 유산소 2탄', 'https://youtu.be/BV0Hi2pv8KM', 'BV0Hi2pv8KM'),

    ('국민체력100', '청소년 체력증진', '상체', '청소년 상체운동', 'https://youtu.be/4kh_3gnTfhI', '4kh_3gnTfhI'),

    ('국민체력100', '청소년 체력증진', '하체', '청소년 하체운동 1탄', 'https://youtu.be/fA2D0h3mb9A', 'fA2D0h3mb9A'),
    ('국민체력100', '청소년 체력증진', '하체', '청소년 하체운동 2탄', 'https://youtu.be/wzptBF3-0m8', 'wzptBF3-0m8'),

    ('국민체력100', '청소년 체력증진', '근력', '청소년 근력 강화를 위한 운동', 'https://youtu.be/XJb1KbdJp3U', 'XJb1KbdJp3U'),

    ('국민체력100', '청소년 체력증진', '코어', '청소년 코어운동', 'https://youtu.be/S2PnZqD2cRE', 'S2PnZqD2cRE'),

    ('국민체력100', '직장인 체력증진', '전신', '직장인 전신운동 1탄', 'https://youtu.be/Hj3fZJQnZy0', 'Hj3fZJQnZy0'),
    ('국민체력100', '직장인 체력증진', '전신', '직장인 전신운동 2탄', 'https://youtu.be/8xpxB0ZgVhE', '8xpxB0ZgVhE'),

    ('국민체력100', '직장인 체력증진', '유산소', '직장인 유산소 운동', 'https://youtu.be/3gI3lYvyh3c', '3gI3lYvyh3c'),

    ('국민체력100', '직장인 체력증진', '상체', '직장인 상체운동', 'https://youtu.be/t9_lGZo4PMw', 't9_lGZo4PMw'),

    ('국민체력100', '직장인 체력증진', '하체', '직장인 하체운동 1탄', 'https://youtu.be/2x4O06UohG8', '2x4O06UohG8'),
    ('국민체력100', '직장인 체력증진', '하체', '직장인 하체운동 2탄', 'https://youtu.be/I1I9hbkWT4A', 'I1I9hbkWT4A'),

    ('국민체력100', '직장인 체력증진', '근력', '직장인을 위한 근력 강화 운동', 'https://youtu.be/eqUC2osFVzE', 'eqUC2osFVzE'),

    ('국민체력100', '직장인 체력증진', '스트레칭', '직장인 스트레칭 루틴', 'https://youtu.be/lrGguqXc-mk', 'lrGguqXc-mk'),

    ('국민체력100', '고령자 체력증진', '전신', '고령자 전신운동 1탄', 'https://youtu.be/o6uWkPBwTqI', 'o6uWkPBwTqI'),
    ('국민체력100', '고령자 체력증진', '전신', '고령자 전신운동 2탄', 'https://youtu.be/dldQz_7l1lA', 'dldQz_7l1lA'),

    ('국민체력100', '고령자 체력증진', '유산소', '고령자 유산소운동', 'https://youtu.be/K8BrLkPWA5c', 'K8BrLkPWA5c'),

    ('국민체력100', '고령자 체력증진', '상체', '고령자 상체운동', 'https://youtu.be/ORuK5L_QCjE', 'ORuK5L_QCjE'),

    ('국민체력100', '고령자 체력증진', '하체', '고령자 하체운동', 'https://youtu.be/fb3p_C7DkIo', 'fb3p_C7DkIo'),

    ('국민체력100', '고령자 체력증진', '균형', '균형운동 1탄', 'https://youtu.be/pW0sGFY2zZk', 'pW0sGFY2zZk'),
    ('국민체력100', '고령자 체력증진', '균형', '균형운동 2탄', 'https://youtu.be/M6uGJPD7WzA', 'M6uGJPD7WzA'),

    ('국민체력100', '고령자 체력증진', '근력', '근력운동 1탄', 'https://youtu.be/NHjk2RKRVy4', 'NHjk2RKRVy4'),
    ('국민체력100', '고령자 체력증진', '근력', '근력운동 2탄', 'https://youtu.be/f8JtOdocOCY', 'f8JtOdocOCY'),

    ('국민체력100', '고령자 체력증진', '유연성', '유연성 향상운동', 'https://youtu.be/IFqydMUax7c', 'IFqydMUax7c'),

    ('국민체력100', '고령자 체력증진', '치매예방', '치매 예방 운동 1탄', 'https://youtu.be/TYQjVFuZKxM', 'TYQjVFuZKxM'),
    ('국민체력100', '고령자 체력증진', '치매예방', '치매 예방 운동 2탄', 'https://youtu.be/EWm5HGt5x3Y', 'EWm5HGt5x3Y'),

    ('국민체력100', '고령자 체력증진', '스트레칭', '고령자 스트레칭', 'https://youtu.be/XnxQ5uUQ0J4', 'XnxQ5uUQ0J4'),

    ('국민체력100', '특수체육', '전신', '장애인을 위한 전신운동', 'https://youtu.be/_o3AqoDIhZ4', '_o3AqoDIhZ4'),
    ('국민체력100', '특수체육', '근력', '장애인을 위한 근력 강화운동', 'https://youtu.be/7y2r6mvzWo0', '7y2r6mvzWo0'),
    ('국민체력100', '특수체육', '균형', '장애인을 위한 균형운동', 'https://youtu.be/NmSjLi3olic', 'NmSjLi3olic'),
    ('국민체력100', '특수체육', '스트레칭', '장애인을 위한 스트레칭', 'https://youtu.be/a_6Gw2WQr-Q', 'a_6Gw2WQr-Q'),

    ('국민체력100', '가정운동', '전신', '가정용 전신운동 루틴', 'https://youtu.be/e4wwL0_D8bA', 'e4wwL0_D8bA'),
    ('국민체력100', '가정운동', '전신', '홈트 전신 루틴', 'https://youtu.be/IwclylFnLAk', 'IwclylFnLAk'),

    ('국민체력100', '가정운동', '상체', '홈트 상체 루틴', 'https://youtu.be/0vzPfGZODdg', '0vzPfGZODdg'),
    ('국민체력100', '가정운동', '하체', '홈트 하체 루틴', 'https://youtu.be/sByVPiT73HI', 'sByVPiT73HI'),
    ('국민체력100', '가정운동', '스트레칭', '홈트 스트레칭', 'https://youtu.be/gmXNu_RqY5A', 'gmXNu_RqY5A');

INSERT INTO exercise_video
(category_large, category_middle, category_small, title, youtube_url, youtube_video_id)
VALUES
    ('국민체력100', '비만예방', '전신', '비만예방 전신운동 1탄', 'https://youtu.be/FXvX8xZ2r7k', 'FXvX8xZ2r7k'),
    ('국민체력100', '비만예방', '전신', '비만예방 전신운동 2탄', 'https://youtu.be/NBi_QyPYv9k', 'NBi_QyPYv9k'),

    ('국민체력100', '비만예방', '유산소', '비만예방 유산소운동', 'https://youtu.be/4YV8DRGdXqk', '4YV8DRGdXqk'),

    ('국민체력100', '비만예방', '근력', '비만예방 근력운동', 'https://youtu.be/jM7QaOqc8JU', 'jM7QaOqc8JU'),

    ('국민체력100', '비만예방', '코어', '비만예방 코어운동', 'https://youtu.be/rAzp4I7R_bU', 'rAzp4I7R_bU'),

    ('국민체력100', '비만예방', '하체', '비만예방 하체운동', 'https://youtu.be/3U-8A4HgvD0', '3U-8A4HgvD0'),

    ('국민체력100', '비만예방', '상체', '비만예방 상체운동', 'https://youtu.be/uqjltqjPV50', 'uqjltqjPV50'),

    ('국민체력100', '비만예방', '스트레칭', '비만예방 스트레칭', 'https://youtu.be/ztp8ZZrVSu0', 'ztp8ZZrVSu0'),

    ('국민체력100', '우울증 예방', '전신', '우울증 예방 전신운동', 'https://youtu.be/wv0-evuSrx4', 'wv0-evuSrx4'),

    ('국민체력100', '우울증 예방', '스트레칭', '우울증 예방 스트레칭', 'https://youtu.be/BFxIN8xZho0', 'BFxIN8xZho0'),

    ('국민체력100', '우울증 예방', '근력', '우울증 예방 근력운동', 'https://youtu.be/rM8I2qcplxk', 'rM8I2qcplxk'),

    ('국민체력100', '우울증 예방', '유산소', '우울증 예방 유산소', 'https://youtu.be/qzTmkV1RQuM', 'qzTmkV1RQuM'),

    ('국민체력100', '발달장애 아동', '전신', '발달장애 아동 전신운동', 'https://youtu.be/lqRk8bA8MyY', 'lqRk8bA8MyY'),

    ('국민체력100', '발달장애 아동', '스트레칭', '발달장애 아동 스트레칭', 'https://youtu.be/V6u2OSxw-BE', 'V6u2OSxw-BE'),

    ('국민체력100', '발달장애 아동', '근력', '발달장애 아동 근력운동', 'https://youtu.be/_wBk5IKt5Jw', '_wBk5IKt5Jw'),

    ('국민체력100', '발달장애 아동', '균형', '발달장애 아동 균형운동', 'https://youtu.be/zY8QkL3jw_M', 'zY8QkL3jw_M'),

    ('국민체력100', '어르신 낙상예방', '전신', '낙상 예방 전신운동', 'https://youtu.be/SZV9wLE-hyI', 'SZV9wLE-hyI'),

    ('국민체력100', '어르신 낙상예방', '균형', '낙상 예방 균형운동 1탄', 'https://youtu.be/fTZPIYOvToA', 'fTZPIYOvToA'),
    ('국민체력100', '어르신 낙상예방', '균형', '낙상 예방 균형운동 2탄', 'https://youtu.be/2X8JZ0pRm5U', '2X8JZ0pRm5U'),

    ('국민체력100', '어르신 낙상예방', '근력', '낙상 예방 근력운동', 'https://youtu.be/eR5Qcm-tjtA', 'eR5Qcm-tjtA'),

    ('국민체력100', '어르신 낙상예방', '하체', '낙상 예방 하체운동', 'https://youtu.be/3JkIhyjXnZc', '3JkIhyjXnZc'),

    ('국민체력100', '어르신 낙상예방', '스트레칭', '낙상 예방 스트레칭', 'https://youtu.be/3h70DB6UObI', '3h70DB6UObI'),

    ('국민체력100', '임산부 운동', '전신', '임산부 전신운동', 'https://youtu.be/BELPk-Zo7YU', 'BELPk-Zo7YU'),

    ('국민체력100', '임산부 운동', '스트레칭', '임산부 스트레칭', 'https://youtu.be/7TJt3G8vFBo', '7TJt3G8vFBo'),

    ('국민체력100', '임산부 운동', '근력', '임산부 근력운동', 'https://youtu.be/fCQ1U1Fcs3A', 'fCQ1U1Fcs3A'),

    ('국민체력100', '임산부 운동', '유산소', '임산부 유산소운동', 'https://youtu.be/PQmFQJvczhU', 'PQmFQJvczhU'),

    ('국민체력100', '어린이 성장', '전신', '어린이 성장 전신운동', 'https://youtu.be/jFBJDUc3Nn8', 'jFBJDUc3Nn8'),

    ('국민체력100', '어린이 성장', '스트레칭', '어린이 스트레칭', 'https://youtu.be/X6HyQ5PV2Ho', 'X6HyQ5PV2Ho'),

    ('국민체력100', '어린이 성장', '근력', '어린이 근력운동', 'https://youtu.be/2lxZ5v3MIwQ', '2lxZ5v3MIwQ'),

    ('국민체력100', '어린이 성장', '협응', '어린이 협응운동', 'https://youtu.be/G1FlRN-ryls', 'G1FlRN-ryls'),

    ('국민체력100', '어린이 성장', '균형', '어린이 균형운동', 'https://youtu.be/mN24jU5vbl4', 'mN24jU5vbl4'),

    ('국민체력100', '체력향상', '전신', '체력향상 전신 루틴', 'https://youtu.be/0H73cEEzois', '0H73cEEzois'),

    ('국민체력100', '체력향상', '상체', '체력향상 상체 루틴', 'https://youtu.be/sHjTlch5RgA', 'sHjTlch5RgA'),

    ('국민체력100', '체력향상', '하체', '체력향상 하체 루틴', 'https://youtu.be/TkLPpJJqWlE', 'TkLPpJJqWlE'),

    ('국민체력100', '체력향상', '코어', '체력향상 코어 루틴', 'https://youtu.be/U-kHmnzWl94', 'U-kHmnzWl94'),

    ('국민체력100', '체력향상', '유산소', '체력향상 유산소 루틴', 'https://youtu.be/haVi-UdbQFo', 'haVi-UdbQFo'),
    ('국민체력100', '체력향상', '근력', '체력향상 근력 루틴', 'https://youtu.be/_fx0u3MCnCc', '_fx0u3MCnCc'),

    ('국민체력100', '회사 건강증진', '전신', '직장인을 위한 전신 루틴', 'https://youtu.be/uDZ2C5q7fZg', 'uDZ2C5q7fZg'),
    ('국민체력100', '회사 건강증진', '스트레칭', '직장인 스트레칭', 'https://youtu.be/p3LNjZ5KJc4', 'p3LNjZ5KJc4'),
    ('국민체력100', '회사 건강증진', '근력', '직장인 근력 루틴', 'https://youtu.be/FyBLeQ1f0r4', 'FyBLeQ1f0r4'),
    ('국민체력100', '회사 건강증진', '유산소', '직장인 유산소 루틴', 'https://youtu.be/jr2d7DaRaoQ', 'jr2d7DaRaoQ');

INSERT INTO exercise_video
(category_large, category_middle, category_small, title, youtube_url, youtube_video_id)
VALUES
    ('국민체력100', '힐링운동', '스트레칭', '힐링 스트레칭 1탄', 'https://youtu.be/zG_PcRMTtS4', 'zG_PcRMTtS4'),
    ('국민체력100', '힐링운동', '스트레칭', '힐링 스트레칭 2탄', 'https://youtu.be/H3WB-gy_pj0', 'H3WB-gy_pj0'),

    ('국민체력100', '힐링운동', '호흡', '힐링 호흡운동', 'https://youtu.be/kdyVZZ_-9dI', 'kdyVZZ_-9dI'),

    ('국민체력100', '힐링운동', '명상', '힐링 명상 1탄', 'https://youtu.be/vEffy-Z2sRM', 'vEffy-Z2sRM'),
    ('국민체력100', '힐링운동', '명상', '힐링 명상 2탄', 'https://youtu.be/F7aJW5MGN24', 'F7aJW5MGN24'),

    ('국민체력100', '힐링운동', '전신', '이완 스트레칭', 'https://youtu.be/v1iXDb8X5Zg', 'v1iXDb8X5Zg'),

    ('국민체력100', '힐링운동', '요가', '힐링 요가', 'https://youtu.be/4l9x509Y6bI', '4l9x509Y6bI'),

    ('국민체력100', '힐링운동', '근력', '힐링 근력운동', 'https://youtu.be/Zzt3VvNL8Vc', 'Zzt3VvNL8Vc'),

    ('국민체력100', '힐링운동', '유산소', '힐링 유산소운동', 'https://youtu.be/8JuP7FsZN78', '8JuP7FsZN78'),

    ('국민체력100', '재활운동', '전신', '재활 전신운동', 'https://youtu.be/fmV3wY3A2HM', 'fmV3wY3A2HM'),

    ('국민체력100', '재활운동', '하체', '재활 하체운동', 'https://youtu.be/0_0jG6mmS4M', '0_0jG6mmS4M'),

    ('국민체력100', '재활운동', '상체', '재활 상체운동', 'https://youtu.be/x_gdcKz_EME', 'x_gdcKz_EME'),

    ('국민체력100', '재활운동', '스트레칭', '재활 스트레칭', 'https://youtu.be/4qE8Noqn22o', '4qE8Noqn22o'),

    ('국민체력100', '재활운동', '균형', '재활 균형운동', 'https://youtu.be/D9Od5Y8sJAk', 'D9Od5Y8sJAk'),

    ('국민체력100', '재활운동', '유산소', '재활 유산소운동', 'https://youtu.be/re9sjPiWZq4', 're9sjPiWZq4'),

    ('국민체력100', '재활운동', '코어', '재활 코어운동', 'https://youtu.be/9hG3djYypZY', '9hG3djYypZY'),

    ('국민체력100', '스포츠 기초체력', '전신', '기초 체력운동 1탄', 'https://youtu.be/5FcuFtqo8Ws', '5FcuFtqo8Ws'),
    ('국민체력100', '스포츠 기초체력', '전신', '기초 체력운동 2탄', 'https://youtu.be/fjrEX8fFqwk', 'fjrEX8fFqwk'),

    ('국민체력100', '스포츠 기초체력', '코어', '기초 체력 코어운동', 'https://youtu.be/r0yPoK7nhNg', 'r0yPoK7nhNg'),

    ('국민체력100', '스포츠 기초체력', '하체', '기초 체력 하체운동', 'https://youtu.be/F0O0MtZ6oGg', 'F0O0MtZ6oGg'),

    ('국민체력100', '스포츠 기초체력', '상체', '기초 체력 상체운동', 'https://youtu.be/J_l9kV8iG2g', 'J_l9kV8iG2g'),

    ('국민체력100', '스포츠 기초체력', '스트레칭', '기초 체력 스트레칭', 'https://youtu.be/XfH5AvzkdxE', 'XfH5AvzkdxE'),

    ('국민체력100', '체형교정', '전신', '체형교정 전신운동', 'https://youtu.be/mcXk-bOZGQ4', 'mcXk-bOZGQ4'),

    ('국민체력100', '체형교정', '스트레칭', '체형교정 스트레칭', 'https://youtu.be/Mr5M-0WLvRc', 'Mr5M-0WLvRc'),

    ('국민체력100', '체형교정', '하체', '체형교정 하체운동', 'https://youtu.be/sZQmtgwPSRQ', 'sZQmtgwPSRQ'),

    ('국민체력100', '체형교정', '코어', '체형교정 코어운동', 'https://youtu.be/VUtqjCwXTWE', 'VUtqjCwXTWE'),

    ('국민체력100', '체형교정', '상체', '체형교정 상체운동', 'https://youtu.be/3t1B7YVpR6Q', '3t1B7YVpR6Q'),

    ('국민체력100', '홈트레이닝', '전신', '홈트 전신 운동', 'https://youtu.be/vmtx5GeuLog', 'vmtx5GeuLog'),

    ('국민체력100', '홈트레이닝', '하체', '홈트 하체 운동', 'https://youtu.be/Y79bVjGsG2U', 'Y79bVjGsG2U'),

    ('국민체력100', '홈트레이닝', '상체', '홈트 상체 운동', 'https://youtu.be/_gFzI0GooyU', '_gFzI0GooyU'),

    ('국민체력100', '홈트레이닝', '스트레칭', '홈트 스트레칭 루틴', 'https://youtu.be/g9SBsKp2oGY', 'g9SBsKp2oGY'),

    ('국민체력100', '홈트레이닝', '코어', '홈트 코어 운동', 'https://youtu.be/UMxVWQn2ykg', 'UMxVWQn2ykg'),

    ('국민체력100', '다이어트', '전신', '다이어트 전신운동 루틴', 'https://youtu.be/_sXkP7FpCZI', '_sXkP7FpCZI'),

    ('국민체력100', '다이어트', '하체', '다이어트 하체 루틴', 'https://youtu.be/c8mS8Rxg8Ho', 'c8mS8Rxg8Ho'),

    ('국민체력100', '다이어트', '상체', '다이어트 상체 루틴', 'https://youtu.be/S0dxEGgkp5Y', 'S0dxEGgkp5Y'),

    ('국민체력100', '다이어트', '코어', '다이어트 코어 루틴', 'https://youtu.be/x9N4dw8XJuQ', 'x9N4dw8XJuQ'),

    ('국민체력100', '다이어트', '유산소', '다이어트 유산소 루틴', 'https://youtu.be/lngmXhXcFx4', 'lngmXhXcFx4'),

    ('국민체력100', '다이어트', '근력', '다이어트 근력 루틴', 'https://youtu.be/CiGvA-j2wh8', 'CiGvA-j2wh8');