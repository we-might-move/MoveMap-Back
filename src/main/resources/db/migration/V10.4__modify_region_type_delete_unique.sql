ALTER TABLE region_type DROP CONSTRAINT IF EXISTS uk_region_type_name;
ALTER TABLE region_type DROP CONSTRAINT IF EXISTS region_type_name_key;

-- 시도 데이터 삽입 (parent_id = NULL)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      (NULL, '11', '서울특별시'),
                                                      (NULL, '26', '부산광역시'),
                                                      (NULL, '27', '대구광역시'),
                                                      (NULL, '28', '인천광역시'),
                                                      (NULL, '29', '광주광역시'),
                                                      (NULL, '30', '대전광역시'),
                                                      (NULL, '31', '울산광역시'),
                                                      (NULL, '36', '세종특별자치시'),
                                                      (NULL, '41', '경기도'),
                                                      (NULL, '42', '강원특별자치도'),
                                                      (NULL, '43', '충청북도'),
                                                      (NULL, '44', '충청남도'),
                                                      (NULL, '45', '전북특별자치도'),
                                                      (NULL, '46', '전라남도'),
                                                      (NULL, '47', '경상북도'),
                                                      (NULL, '48', '경상남도'),
                                                      (NULL, '50', '제주특별자치도');

-- 시군구 데이터 삽입 (parent_id는 시도의 ID 참조)
-- 서울특별시 (11)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11110', '종로구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11140', '중구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11170', '용산구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11200', '성동구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11215', '광진구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11230', '동대문구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11260', '중랑구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11290', '성북구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11305', '강북구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11320', '도봉구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11350', '노원구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11380', '은평구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11410', '서대문구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11440', '마포구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11470', '양천구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11500', '강서구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11530', '구로구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11545', '금천구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11560', '영등포구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11590', '동작구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11620', '관악구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11650', '서초구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11680', '강남구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11710', '송파구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '11'), '11740', '강동구');

-- 부산광역시 (26)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26110', '중구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26140', '서구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26170', '동구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26200', '영도구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26230', '부산진구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26260', '동래구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26290', '남구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26320', '북구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26350', '해운대구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26380', '사하구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26410', '금정구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26440', '강서구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26470', '연제구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26500', '수영구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26530', '사상구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '26'), '26710', '기장군');

-- 대구광역시 (27)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '27'), '27110', '중구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '27'), '27140', '동구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '27'), '27170', '서구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '27'), '27200', '남구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '27'), '27230', '북구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '27'), '27260', '수성구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '27'), '27290', '달서구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '27'), '27710', '달성군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '27'), '27720', '군위군');

-- 인천광역시 (28)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '28'), '28110', '중구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '28'), '28140', '동구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '28'), '28177', '미추홀구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '28'), '28185', '연수구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '28'), '28200', '남동구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '28'), '28237', '부평구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '28'), '28245', '계양구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '28'), '28260', '서구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '28'), '28710', '강화군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '28'), '28720', '옹진군');

-- 광주광역시 (29)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '29'), '29110', '동구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '29'), '29140', '서구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '29'), '29155', '남구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '29'), '29170', '북구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '29'), '29200', '광산구');

-- 대전광역시 (30)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '30'), '30110', '동구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '30'), '30140', '중구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '30'), '30170', '서구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '30'), '30200', '유성구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '30'), '30230', '대덕구');

-- 울산광역시 (31)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '31'), '31110', '중구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '31'), '31140', '남구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '31'), '31170', '동구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '31'), '31200', '북구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '31'), '31710', '울주군');

-- 세종특별자치시 (36) - 시군구 없음

-- 경기도 (41)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41111', '수원시 장안구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41113', '수원시 권선구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41115', '수원시 팔달구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41117', '수원시 영통구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41131', '성남시 수정구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41133', '성남시 중원구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41135', '성남시 분당구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41150', '의정부시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41171', '안양시 만안구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41173', '안양시 동안구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41190', '부천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41210', '광명시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41220', '평택시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41250', '동두천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41271', '안산시 상록구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41273', '안산시 단원구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41281', '고양시 덕양구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41285', '고양시 일산동구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41287', '고양시 일산서구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41290', '과천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41310', '구리시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41360', '남양주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41370', '오산시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41390', '시흥시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41410', '군포시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41430', '의왕시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41450', '하남시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41461', '용인시 처인구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41463', '용인시 기흥구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41465', '용인시 수지구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41480', '파주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41500', '이천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41550', '안성시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41570', '김포시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41590', '화성시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41610', '광주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41630', '양주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41650', '포천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41670', '여주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41800', '연천군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41820', '가평군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '41'), '41830', '양평군');

-- 강원특별자치도 (42)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42110', '춘천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42130', '원주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42150', '강릉시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42170', '동해시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42190', '태백시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42210', '속초시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42230', '삼척시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42720', '홍천군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42730', '횡성군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42750', '영월군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42760', '평창군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42770', '정선군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42780', '철원군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42790', '화천군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42800', '양구군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42810', '인제군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42820', '고성군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '42'), '42830', '양양군');

-- 충청북도 (43)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43111', '청주시 상당구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43112', '청주시 서원구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43113', '청주시 흥덕구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43114', '청주시 청원구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43130', '충주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43150', '제천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43720', '보은군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43730', '옥천군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43740', '영동군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43745', '증평군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43750', '진천군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43760', '괴산군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43770', '음성군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '43'), '43800', '단양군');

-- 충청남도 (44)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44131', '천안시 동남구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44133', '천안시 서북구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44150', '공주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44180', '보령시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44200', '아산시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44210', '서산시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44230', '논산시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44250', '계룡시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44270', '당진시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44710', '금산군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44760', '부여군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44770', '서천군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44790', '청양군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44800', '홍성군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44810', '예산군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '44'), '44825', '태안군');

-- 전북특별자치도 (45)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45111', '전주시 완산구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45113', '전주시 덕진구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45130', '군산시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45140', '익산시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45180', '정읍시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45190', '남원시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45210', '김제시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45710', '완주군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45720', '진안군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45730', '무주군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45740', '장수군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45750', '임실군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45770', '순창군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45790', '고창군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '45'), '45800', '부안군');

-- 전라남도 (46)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46110', '목포시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46130', '여수시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46150', '순천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46170', '나주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46230', '광양시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46710', '담양군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46720', '곡성군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46730', '구례군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46770', '고흥군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46780', '보성군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46790', '화순군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46800', '장흥군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46810', '강진군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46820', '해남군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46830', '영암군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46840', '무안군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46860', '함평군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46870', '영광군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46880', '장성군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46890', '완도군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46900', '진도군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '46'), '46910', '신안군');

-- 경상북도 (47)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47111', '포항시 남구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47113', '포항시 북구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47130', '경주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47150', '김천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47170', '안동시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47190', '구미시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47210', '영주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47230', '영천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47250', '상주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47280', '문경시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47290', '경산시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47720', '의성군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47730', '청송군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47750', '영양군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47760', '영덕군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47770', '청도군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47820', '고령군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47830', '성주군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47840', '칠곡군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47850', '예천군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47900', '봉화군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47920', '울진군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '47'), '47930', '울릉군');

-- 경상남도 (48)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48121', '창원시 의창구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48123', '창원시 성산구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48125', '창원시 마산합포구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48127', '창원시 마산회원구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48129', '창원시 진해구'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48170', '진주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48220', '통영시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48240', '사천시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48250', '김해시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48270', '밀양시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48310', '거제시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48330', '양산시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48720', '의령군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48730', '함안군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48740', '창녕군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48820', '고성군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48840', '남해군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48850', '하동군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48860', '산청군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48870', '함양군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48880', '거창군'),
                                                      ((SELECT id FROM region_type WHERE prefix = '48'), '48890', '합천군');

-- 제주특별자치도 (50)
INSERT INTO region_type (parent_id, prefix, name) VALUES
                                                      ((SELECT id FROM region_type WHERE prefix = '50'), '50110', '제주시'),
                                                      ((SELECT id FROM region_type WHERE prefix = '50'), '50130', '서귀포시');