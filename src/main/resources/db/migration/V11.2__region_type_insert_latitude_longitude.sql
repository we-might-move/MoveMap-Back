-- ============================================
-- 시/도 중심 좌표 업데이트
-- ============================================

-- 서울특별시
UPDATE region_type
SET center_latitude = 37.5665,
    center_longitude = 126.9780,
    center_location = ST_SetSRID(ST_MakePoint(126.9780, 37.5665), 4326)
WHERE prefix = '11';

-- 부산광역시
UPDATE region_type
SET center_latitude = 35.1796,
    center_longitude = 129.0756,
    center_location = ST_SetSRID(ST_MakePoint(129.0756, 35.1796), 4326)
WHERE prefix = '26';

-- 대구광역시
UPDATE region_type
SET center_latitude = 35.8714,
    center_longitude = 128.6014,
    center_location = ST_SetSRID(ST_MakePoint(128.6014, 35.8714), 4326)
WHERE prefix = '27';

-- 인천광역시
UPDATE region_type
SET center_latitude = 37.4563,
    center_longitude = 126.7052,
    center_location = ST_SetSRID(ST_MakePoint(126.7052, 37.4563), 4326)
WHERE prefix = '28';

-- 광주광역시
UPDATE region_type
SET center_latitude = 35.1595,
    center_longitude = 126.8526,
    center_location = ST_SetSRID(ST_MakePoint(126.8526, 35.1595), 4326)
WHERE prefix = '29';

-- 대전광역시
UPDATE region_type
SET center_latitude = 36.3504,
    center_longitude = 127.3845,
    center_location = ST_SetSRID(ST_MakePoint(127.3845, 36.3504), 4326)
WHERE prefix = '30';

-- 울산광역시
UPDATE region_type
SET center_latitude = 35.5384,
    center_longitude = 129.3114,
    center_location = ST_SetSRID(ST_MakePoint(129.3114, 35.5384), 4326)
WHERE prefix = '31';

-- 세종특별자치시
UPDATE region_type
SET center_latitude = 36.4800,
    center_longitude = 127.2890,
    center_location = ST_SetSRID(ST_MakePoint(127.2890, 36.4800), 4326)
WHERE prefix = '36';

-- 경기도
UPDATE region_type
SET center_latitude = 37.4138,
    center_longitude = 127.5183,
    center_location = ST_SetSRID(ST_MakePoint(127.5183, 37.4138), 4326)
WHERE prefix = '41';

-- 강원특별자치도
UPDATE region_type
SET center_latitude = 37.8228,
    center_longitude = 128.1555,
    center_location = ST_SetSRID(ST_MakePoint(128.1555, 37.8228), 4326)
WHERE prefix = '42';

-- 충청북도
UPDATE region_type
SET center_latitude = 36.8,
    center_longitude = 127.7,
    center_location = ST_SetSRID(ST_MakePoint(127.7, 36.8), 4326)
WHERE prefix = '43';

-- 충청남도
UPDATE region_type
SET center_latitude = 36.5184,
    center_longitude = 126.8,
    center_location = ST_SetSRID(ST_MakePoint(126.8, 36.5184), 4326)
WHERE prefix = '44';

-- 전북특별자치도
UPDATE region_type
SET center_latitude = 35.7175,
    center_longitude = 127.153,
    center_location = ST_SetSRID(ST_MakePoint(127.153, 35.7175), 4326)
WHERE prefix = '45';

-- 전라남도
UPDATE region_type
SET center_latitude = 34.8679,
    center_longitude = 126.991,
    center_location = ST_SetSRID(ST_MakePoint(126.991, 34.8679), 4326)
WHERE prefix = '46';

-- 경상북도
UPDATE region_type
SET center_latitude = 36.4919,
    center_longitude = 128.8889,
    center_location = ST_SetSRID(ST_MakePoint(128.8889, 36.4919), 4326)
WHERE prefix = '47';

-- 경상남도
UPDATE region_type
SET center_latitude = 35.4606,
    center_longitude = 128.2132,
    center_location = ST_SetSRID(ST_MakePoint(128.2132, 35.4606), 4326)
WHERE prefix = '48';

-- 제주특별자치도
UPDATE region_type
SET center_latitude = 33.4890,
    center_longitude = 126.4983,
    center_location = ST_SetSRID(ST_MakePoint(126.4983, 33.4890), 4326)
WHERE prefix = '50';


-- ============================================
-- 시/군/구 중심 좌표 업데이트
-- ============================================

-- 서울특별시 (11)
UPDATE region_type SET center_latitude = 37.5730, center_longitude = 126.9794, center_location = ST_SetSRID(ST_MakePoint(126.9794, 37.5730), 4326) WHERE prefix = '11110'; -- 종로구
UPDATE region_type SET center_latitude = 37.5636, center_longitude = 126.9976, center_location = ST_SetSRID(ST_MakePoint(126.9976, 37.5636), 4326) WHERE prefix = '11140'; -- 중구
UPDATE region_type SET center_latitude = 37.5384, center_longitude = 126.9654, center_location = ST_SetSRID(ST_MakePoint(126.9654, 37.5384), 4326) WHERE prefix = '11170'; -- 용산구
UPDATE region_type SET center_latitude = 37.5633, center_longitude = 127.0367, center_location = ST_SetSRID(ST_MakePoint(127.0367, 37.5633), 4326) WHERE prefix = '11200'; -- 성동구
UPDATE region_type SET center_latitude = 37.5388, center_longitude = 127.0819, center_location = ST_SetSRID(ST_MakePoint(127.0819, 37.5388), 4326) WHERE prefix = '11215'; -- 광진구
UPDATE region_type SET center_latitude = 37.5744, center_longitude = 127.0397, center_location = ST_SetSRID(ST_MakePoint(127.0397, 37.5744), 4326) WHERE prefix = '11230'; -- 동대문구
UPDATE region_type SET center_latitude = 37.6063, center_longitude = 127.0926, center_location = ST_SetSRID(ST_MakePoint(127.0926, 37.6063), 4326) WHERE prefix = '11260'; -- 중랑구
UPDATE region_type SET center_latitude = 37.5894, center_longitude = 127.0165, center_location = ST_SetSRID(ST_MakePoint(127.0165, 37.5894), 4326) WHERE prefix = '11290'; -- 성북구
UPDATE region_type SET center_latitude = 37.6397, center_longitude = 127.0256, center_location = ST_SetSRID(ST_MakePoint(127.0256, 37.6397), 4326) WHERE prefix = '11305'; -- 강북구
UPDATE region_type SET center_latitude = 37.6688, center_longitude = 127.0471, center_location = ST_SetSRID(ST_MakePoint(127.0471, 37.6688), 4326) WHERE prefix = '11320'; -- 도봉구
UPDATE region_type SET center_latitude = 37.6542, center_longitude = 127.0568, center_location = ST_SetSRID(ST_MakePoint(127.0568, 37.6542), 4326) WHERE prefix = '11350'; -- 노원구
UPDATE region_type SET center_latitude = 37.6027, center_longitude = 126.9289, center_location = ST_SetSRID(ST_MakePoint(126.9289, 37.6027), 4326) WHERE prefix = '11380'; -- 은평구
UPDATE region_type SET center_latitude = 37.5791, center_longitude = 126.9368, center_location = ST_SetSRID(ST_MakePoint(126.9368, 37.5791), 4326) WHERE prefix = '11410'; -- 서대문구
UPDATE region_type SET center_latitude = 37.5663, center_longitude = 126.9015, center_location = ST_SetSRID(ST_MakePoint(126.9015, 37.5663), 4326) WHERE prefix = '11440'; -- 마포구
UPDATE region_type SET center_latitude = 37.5169, center_longitude = 126.8664, center_location = ST_SetSRID(ST_MakePoint(126.8664, 37.5169), 4326) WHERE prefix = '11470'; -- 양천구
UPDATE region_type SET center_latitude = 37.5509, center_longitude = 126.8495, center_location = ST_SetSRID(ST_MakePoint(126.8495, 37.5509), 4326) WHERE prefix = '11500'; -- 강서구
UPDATE region_type SET center_latitude = 37.4955, center_longitude = 126.8869, center_location = ST_SetSRID(ST_MakePoint(126.8869, 37.4955), 4326) WHERE prefix = '11530'; -- 구로구
UPDATE region_type SET center_latitude = 37.4569, center_longitude = 126.8955, center_location = ST_SetSRID(ST_MakePoint(126.8955, 37.4569), 4326) WHERE prefix = '11545'; -- 금천구
UPDATE region_type SET center_latitude = 37.5264, center_longitude = 126.8963, center_location = ST_SetSRID(ST_MakePoint(126.8963, 37.5264), 4326) WHERE prefix = '11560'; -- 영등포구
UPDATE region_type SET center_latitude = 37.5124, center_longitude = 126.9393, center_location = ST_SetSRID(ST_MakePoint(126.9393, 37.5124), 4326) WHERE prefix = '11590'; -- 동작구
UPDATE region_type SET center_latitude = 37.4784, center_longitude = 126.9516, center_location = ST_SetSRID(ST_MakePoint(126.9516, 37.4784), 4326) WHERE prefix = '11620'; -- 관악구
UPDATE region_type SET center_latitude = 37.4837, center_longitude = 127.0324, center_location = ST_SetSRID(ST_MakePoint(127.0324, 37.4837), 4326) WHERE prefix = '11650'; -- 서초구
UPDATE region_type SET center_latitude = 37.5172, center_longitude = 127.0473, center_location = ST_SetSRID(ST_MakePoint(127.0473, 37.5172), 4326) WHERE prefix = '11680'; -- 강남구
UPDATE region_type SET center_latitude = 37.5145, center_longitude = 127.1059, center_location = ST_SetSRID(ST_MakePoint(127.1059, 37.5145), 4326) WHERE prefix = '11710'; -- 송파구
UPDATE region_type SET center_latitude = 37.5301, center_longitude = 127.1238, center_location = ST_SetSRID(ST_MakePoint(127.1238, 37.5301), 4326) WHERE prefix = '11740'; -- 강동구

-- 부산광역시 (26)
UPDATE region_type SET center_latitude = 35.1040, center_longitude = 129.0326, center_location = ST_SetSRID(ST_MakePoint(129.0326, 35.1040), 4326) WHERE prefix = '26110'; -- 중구
UPDATE region_type SET center_latitude = 35.0978, center_longitude = 129.0231, center_location = ST_SetSRID(ST_MakePoint(129.0231, 35.0978), 4326) WHERE prefix = '26140'; -- 서구
UPDATE region_type SET center_latitude = 35.1295, center_longitude = 129.0450, center_location = ST_SetSRID(ST_MakePoint(129.0450, 35.1295), 4326) WHERE prefix = '26170'; -- 동구
UPDATE region_type SET center_latitude = 35.0913, center_longitude = 129.0675, center_location = ST_SetSRID(ST_MakePoint(129.0675, 35.0913), 4326) WHERE prefix = '26200'; -- 영도구
UPDATE region_type SET center_latitude = 35.1628, center_longitude = 129.0531, center_location = ST_SetSRID(ST_MakePoint(129.0531, 35.1628), 4326) WHERE prefix = '26230'; -- 부산진구
UPDATE region_type SET center_latitude = 35.2046, center_longitude = 129.0823, center_location = ST_SetSRID(ST_MakePoint(129.0823, 35.2046), 4326) WHERE prefix = '26260'; -- 동래구
UPDATE region_type SET center_latitude = 35.1361, center_longitude = 129.0845, center_location = ST_SetSRID(ST_MakePoint(129.0845, 35.1361), 4326) WHERE prefix = '26290'; -- 남구
UPDATE region_type SET center_latitude = 35.1957, center_longitude = 128.9897, center_location = ST_SetSRID(ST_MakePoint(128.9897, 35.1957), 4326) WHERE prefix = '26320'; -- 북구
UPDATE region_type SET center_latitude = 35.1631, center_longitude = 129.1635, center_location = ST_SetSRID(ST_MakePoint(129.1635, 35.1631), 4326) WHERE prefix = '26350'; -- 해운대구
UPDATE region_type SET center_latitude = 35.1042, center_longitude = 128.9747, center_location = ST_SetSRID(ST_MakePoint(128.9747, 35.1042), 4326) WHERE prefix = '26380'; -- 사하구
UPDATE region_type SET center_latitude = 35.2429, center_longitude = 129.0918, center_location = ST_SetSRID(ST_MakePoint(129.0918, 35.2429), 4326) WHERE prefix = '26410'; -- 금정구
UPDATE region_type SET center_latitude = 35.2122, center_longitude = 128.9808, center_location = ST_SetSRID(ST_MakePoint(128.9808, 35.2122), 4326) WHERE prefix = '26440'; -- 강서구
UPDATE region_type SET center_latitude = 35.1852, center_longitude = 129.0792, center_location = ST_SetSRID(ST_MakePoint(129.0792, 35.1852), 4326) WHERE prefix = '26470'; -- 연제구
UPDATE region_type SET center_latitude = 35.1450, center_longitude = 129.1127, center_location = ST_SetSRID(ST_MakePoint(129.1127, 35.1450), 4326) WHERE prefix = '26500'; -- 수영구
UPDATE region_type SET center_latitude = 35.1522, center_longitude = 128.9919, center_location = ST_SetSRID(ST_MakePoint(128.9919, 35.1522), 4326) WHERE prefix = '26530'; -- 사상구
UPDATE region_type SET center_latitude = 35.2447, center_longitude = 129.2223, center_location = ST_SetSRID(ST_MakePoint(129.2223, 35.2447), 4326) WHERE prefix = '26710'; -- 기장군

-- 대구광역시 (27)
UPDATE region_type SET center_latitude = 35.8694, center_longitude = 128.6069, center_location = ST_SetSRID(ST_MakePoint(128.6069, 35.8694), 4326) WHERE prefix = '27110'; -- 중구
UPDATE region_type SET center_latitude = 35.8869, center_longitude = 128.6359, center_location = ST_SetSRID(ST_MakePoint(128.6359, 35.8869), 4326) WHERE prefix = '27140'; -- 동구
UPDATE region_type SET center_latitude = 35.8719, center_longitude = 128.5591, center_location = ST_SetSRID(ST_MakePoint(128.5591, 35.8719), 4326) WHERE prefix = '27170'; -- 서구
UPDATE region_type SET center_latitude = 35.8463, center_longitude = 128.5974, center_location = ST_SetSRID(ST_MakePoint(128.5974, 35.8463), 4326) WHERE prefix = '27200'; -- 남구
UPDATE region_type SET center_latitude = 35.8858, center_longitude = 128.5822, center_location = ST_SetSRID(ST_MakePoint(128.5822, 35.8858), 4326) WHERE prefix = '27230'; -- 북구
UPDATE region_type SET center_latitude = 35.8581, center_longitude = 128.6306, center_location = ST_SetSRID(ST_MakePoint(128.6306, 35.8581), 4326) WHERE prefix = '27260'; -- 수성구
UPDATE region_type SET center_latitude = 35.8299, center_longitude = 128.5326, center_location = ST_SetSRID(ST_MakePoint(128.5326, 35.8299), 4326) WHERE prefix = '27290'; -- 달서구
UPDATE region_type SET center_latitude = 35.7742, center_longitude = 128.4312, center_location = ST_SetSRID(ST_MakePoint(128.4312, 35.7742), 4326) WHERE prefix = '27710'; -- 달성군
UPDATE region_type SET center_latitude = 36.2431, center_longitude = 128.5686, center_location = ST_SetSRID(ST_MakePoint(128.5686, 36.2431), 4326) WHERE prefix = '27720'; -- 군위군

-- 인천광역시 (28)
UPDATE region_type SET center_latitude = 37.4738, center_longitude = 126.6216, center_location = ST_SetSRID(ST_MakePoint(126.6216, 37.4738), 4326) WHERE prefix = '28110'; -- 중구
UPDATE region_type SET center_latitude = 37.4740, center_longitude = 126.6433, center_location = ST_SetSRID(ST_MakePoint(126.6433, 37.4740), 4326) WHERE prefix = '28140'; -- 동구
UPDATE region_type SET center_latitude = 37.4635, center_longitude = 126.6507, center_location = ST_SetSRID(ST_MakePoint(126.6507, 37.4635), 4326) WHERE prefix = '28177'; -- 미추홀구
UPDATE region_type SET center_latitude = 37.4105, center_longitude = 126.6785, center_location = ST_SetSRID(ST_MakePoint(126.6785, 37.4105), 4326) WHERE prefix = '28185'; -- 연수구
UPDATE region_type SET center_latitude = 37.4487, center_longitude = 126.7314, center_location = ST_SetSRID(ST_MakePoint(126.7314, 37.4487), 4326) WHERE prefix = '28200'; -- 남동구
UPDATE region_type SET center_latitude = 37.5088, center_longitude = 126.7217, center_location = ST_SetSRID(ST_MakePoint(126.7217, 37.5088), 4326) WHERE prefix = '28237'; -- 부평구
UPDATE region_type SET center_latitude = 37.5378, center_longitude = 126.7377, center_location = ST_SetSRID(ST_MakePoint(126.7377, 37.5378), 4326) WHERE prefix = '28245'; -- 계양구
UPDATE region_type SET center_latitude = 37.5453, center_longitude = 126.6759, center_location = ST_SetSRID(ST_MakePoint(126.6759, 37.5453), 4326) WHERE prefix = '28260'; -- 서구
UPDATE region_type SET center_latitude = 37.7466, center_longitude = 126.4876, center_location = ST_SetSRID(ST_MakePoint(126.4876, 37.7466), 4326) WHERE prefix = '28710'; -- 강화군
UPDATE region_type SET center_latitude = 37.4461, center_longitude = 126.6368, center_location = ST_SetSRID(ST_MakePoint(126.6368, 37.4461), 4326) WHERE prefix = '28720'; -- 옹진군

-- 광주광역시 (29)
UPDATE region_type SET center_latitude = 35.1460, center_longitude = 126.9220, center_location = ST_SetSRID(ST_MakePoint(126.9220, 35.1460), 4326) WHERE prefix = '29110'; -- 동구
UPDATE region_type SET center_latitude = 35.1520, center_longitude = 126.8894, center_location = ST_SetSRID(ST_MakePoint(126.8894, 35.1520), 4326) WHERE prefix = '29140'; -- 서구
UPDATE region_type SET center_latitude = 35.1328, center_longitude = 126.9026, center_location = ST_SetSRID(ST_MakePoint(126.9026, 35.1328), 4326) WHERE prefix = '29155'; -- 남구
UPDATE region_type SET center_latitude = 35.1741, center_longitude = 126.9118, center_location = ST_SetSRID(ST_MakePoint(126.9118, 35.1741), 4326) WHERE prefix = '29170'; -- 북구
UPDATE region_type SET center_latitude = 35.1395, center_longitude = 126.7933, center_location = ST_SetSRID(ST_MakePoint(126.7933, 35.1395), 4326) WHERE prefix = '29200'; -- 광산구

-- 대전광역시 (30)
UPDATE region_type SET center_latitude = 36.3313, center_longitude = 127.4252, center_location = ST_SetSRID(ST_MakePoint(127.4252, 36.3313), 4326) WHERE prefix = '30110'; -- 동구
UPDATE region_type SET center_latitude = 36.3255, center_longitude = 127.4210, center_location = ST_SetSRID(ST_MakePoint(127.4210, 36.3255), 4326) WHERE prefix = '30140'; -- 중구
UPDATE region_type SET center_latitude = 36.3557, center_longitude = 127.3833, center_location = ST_SetSRID(ST_MakePoint(127.3833, 36.3557), 4326) WHERE prefix = '30170'; -- 서구
UPDATE region_type SET center_latitude = 36.3623, center_longitude = 127.3567, center_location = ST_SetSRID(ST_MakePoint(127.3567, 36.3623), 4326) WHERE prefix = '30200'; -- 유성구
UPDATE region_type SET center_latitude = 36.3467, center_longitude = 127.4167, center_location = ST_SetSRID(ST_MakePoint(127.4167, 36.3467), 4326) WHERE prefix = '30230'; -- 대덕구

-- 울산광역시 (31)
UPDATE region_type SET center_latitude = 35.5694, center_longitude = 129.3318, center_location = ST_SetSRID(ST_MakePoint(129.3318, 35.5694), 4326) WHERE prefix = '31110'; -- 중구
UPDATE region_type SET center_latitude = 35.5439, center_longitude = 129.3300, center_location = ST_SetSRID(ST_MakePoint(129.3300, 35.5439), 4326) WHERE prefix = '31140'; -- 남구
UPDATE region_type SET center_latitude = 35.5046, center_longitude = 129.4164, center_location = ST_SetSRID(ST_MakePoint(129.4164, 35.5046), 4326) WHERE prefix = '31170'; -- 동구
UPDATE region_type SET center_latitude = 35.5826, center_longitude = 129.3614, center_location = ST_SetSRID(ST_MakePoint(129.3614, 35.5826), 4326) WHERE prefix = '31200'; -- 북구
UPDATE region_type SET center_latitude = 35.5822, center_longitude = 129.1553, center_location = ST_SetSRID(ST_MakePoint(129.1553, 35.5822), 4326) WHERE prefix = '31710'; -- 울주군

-- 경기도 (41)
UPDATE region_type SET center_latitude = 37.2636, center_longitude = 127.0286, center_location = ST_SetSRID(ST_MakePoint(127.0286, 37.2636), 4326) WHERE prefix = '41111'; -- 수원시 장안구
UPDATE region_type SET center_latitude = 37.2636, center_longitude = 126.9728, center_location = ST_SetSRID(ST_MakePoint(126.9728, 37.2636), 4326) WHERE prefix = '41113'; -- 수원시 권선구
UPDATE region_type SET center_latitude = 37.2793, center_longitude = 127.0289, center_location = ST_SetSRID(ST_MakePoint(127.0289, 37.2793), 4326) WHERE prefix = '41115'; -- 수원시 팔달구
UPDATE region_type SET center_latitude = 37.2574, center_longitude = 127.0454, center_location = ST_SetSRID(ST_MakePoint(127.0454, 37.2574), 4326) WHERE prefix = '41117'; -- 수원시 영통구
UPDATE region_type SET center_latitude = 37.4505, center_longitude = 127.1466, center_location = ST_SetSRID(ST_MakePoint(127.1466, 37.4505), 4326) WHERE prefix = '41131'; -- 성남시 수정구
UPDATE region_type SET center_latitude = 37.4348, center_longitude = 127.1466, center_location = ST_SetSRID(ST_MakePoint(127.1466, 37.4348), 4326) WHERE prefix = '41133'; -- 성남시 중원구
UPDATE region_type SET center_latitude = 37.3826, center_longitude = 127.1210, center_location = ST_SetSRID(ST_MakePoint(127.1210, 37.3826), 4326) WHERE prefix = '41135'; -- 성남시 분당구
UPDATE region_type SET center_latitude = 37.7382, center_longitude = 127.0338, center_location = ST_SetSRID(ST_MakePoint(127.0338, 37.7382), 4326) WHERE prefix = '41150'; -- 의정부시
UPDATE region_type SET center_latitude = 37.3897, center_longitude = 126.9268, center_location = ST_SetSRID(ST_MakePoint(126.9268, 37.3897), 4326) WHERE prefix = '41171'; -- 안양시 만안구
UPDATE region_type SET center_latitude = 37.3926, center_longitude = 126.9512, center_location = ST_SetSRID(ST_MakePoint(126.9512, 37.3926), 4326) WHERE prefix = '41173'; -- 안양시 동안구
UPDATE region_type SET center_latitude = 37.5034, center_longitude = 126.7660, center_location = ST_SetSRID(ST_MakePoint(126.7660, 37.5034), 4326) WHERE prefix = '41190'; -- 부천시
UPDATE region_type SET center_latitude = 37.4785, center_longitude = 126.8653, center_location = ST_SetSRID(ST_MakePoint(126.8653, 37.4785), 4326) WHERE prefix = '41210'; -- 광명시
UPDATE region_type SET center_latitude = 36.9922, center_longitude = 127.1127, center_location = ST_SetSRID(ST_MakePoint(127.1127, 36.9922), 4326) WHERE prefix = '41220'; -- 평택시
UPDATE region_type SET center_latitude = 37.9035, center_longitude = 127.0605, center_location = ST_SetSRID(ST_MakePoint(127.0605, 37.9035), 4326) WHERE prefix = '41250'; -- 동두천시
UPDATE region_type SET center_latitude = 37.3207, center_longitude = 126.8314, center_location = ST_SetSRID(ST_MakePoint(126.8314, 37.3207), 4326) WHERE prefix = '41271'; -- 안산시 상록구
UPDATE region_type SET center_latitude = 37.3207, center_longitude = 126.7925, center_location = ST_SetSRID(ST_MakePoint(126.7925, 37.3207), 4326) WHERE prefix = '41273'; -- 안산시 단원구
UPDATE region_type SET center_latitude = 37.6584, center_longitude = 126.8321, center_location = ST_SetSRID(ST_MakePoint(126.8321, 37.6584), 4326) WHERE prefix = '41281'; -- 고양시 덕양구
UPDATE region_type SET center_latitude = 37.6584, center_longitude = 126.7733, center_location = ST_SetSRID(ST_MakePoint(126.7733, 37.6584), 4326) WHERE prefix = '41285'; -- 고양시 일산동구
UPDATE region_type SET center_latitude = 37.6778, center_longitude = 126.7733, center_location = ST_SetSRID(ST_MakePoint(126.7733, 37.6778), 4326) WHERE prefix = '41287'; -- 고양시 일산서구
UPDATE region_type SET center_latitude = 37.4290, center_longitude = 126.9879, center_location = ST_SetSRID(ST_MakePoint(126.9879, 37.4290), 4326) WHERE prefix = '41290'; -- 과천시
UPDATE region_type SET center_latitude = 37.5943, center_longitude = 127.1296, center_location = ST_SetSRID(ST_MakePoint(127.1296, 37.5943), 4326) WHERE prefix = '41310'; -- 구리시
UPDATE region_type SET center_latitude = 37.6360, center_longitude = 127.2164, center_location = ST_SetSRID(ST_MakePoint(127.2164, 37.6360), 4326) WHERE prefix = '41360'; -- 남양주시
UPDATE region_type SET center_latitude = 37.1495, center_longitude = 127.0770, center_location = ST_SetSRID(ST_MakePoint(127.0770, 37.1495), 4326) WHERE prefix = '41370'; -- 오산시
UPDATE region_type SET center_latitude = 37.3800, center_longitude = 126.8029, center_location = ST_SetSRID(ST_MakePoint(126.8029, 37.3800), 4326) WHERE prefix = '41390'; -- 시흥시
UPDATE region_type SET center_latitude = 37.3615, center_longitude = 126.9355, center_location = ST_SetSRID(ST_MakePoint(126.9355, 37.3615), 4326) WHERE prefix = '41410'; -- 군포시
UPDATE region_type SET center_latitude = 37.3449, center_longitude = 126.9682, center_location = ST_SetSRID(ST_MakePoint(126.9682, 37.3449), 4326) WHERE prefix = '41430'; -- 의왕시
UPDATE region_type SET center_latitude = 37.5393, center_longitude = 127.2145, center_location = ST_SetSRID(ST_MakePoint(127.2145, 37.5393), 4326) WHERE prefix = '41450'; -- 하남시
UPDATE region_type SET center_latitude = 37.2339, center_longitude = 127.2017, center_location = ST_SetSRID(ST_MakePoint(127.2017, 37.2339), 4326) WHERE prefix = '41461'; -- 용인시 처인구
UPDATE region_type SET center_latitude = 37.2761, center_longitude = 127.1156, center_location = ST_SetSRID(ST_MakePoint(127.1156, 37.2761), 4326) WHERE prefix = '41463'; -- 용인시 기흥구
UPDATE region_type SET center_latitude = 37.3217, center_longitude = 127.0969, center_location = ST_SetSRID(ST_MakePoint(127.0969, 37.3217), 4326) WHERE prefix = '41465'; -- 용인시 수지구
UPDATE region_type SET center_latitude = 37.7599, center_longitude = 126.7800, center_location = ST_SetSRID(ST_MakePoint(126.7800, 37.7599), 4326) WHERE prefix = '41480'; -- 파주시
UPDATE region_type SET center_latitude = 37.2722, center_longitude = 127.4351, center_location = ST_SetSRID(ST_MakePoint(127.4351, 37.2722), 4326) WHERE prefix = '41500'; -- 이천시
UPDATE region_type SET center_latitude = 37.0079, center_longitude = 127.2797, center_location = ST_SetSRID(ST_MakePoint(127.2797, 37.0079), 4326) WHERE prefix = '41550'; -- 안성시
UPDATE region_type SET center_latitude = 37.6152, center_longitude = 126.7157, center_location = ST_SetSRID(ST_MakePoint(126.7157, 37.6152), 4326) WHERE prefix = '41570'; -- 김포시
UPDATE region_type SET center_latitude = 37.1992, center_longitude = 126.8311, center_location = ST_SetSRID(ST_MakePoint(126.8311, 37.1992), 4326) WHERE prefix = '41590'; -- 화성시
UPDATE region_type SET center_latitude = 37.4294, center_longitude = 127.2558, center_location = ST_SetSRID(ST_MakePoint(127.2558, 37.4294), 4326) WHERE prefix = '41610'; -- 광주시
UPDATE region_type SET center_latitude = 37.7854, center_longitude = 127.0456, center_location = ST_SetSRID(ST_MakePoint(127.0456, 37.7854), 4326) WHERE prefix = '41630'; -- 양주시
UPDATE region_type SET center_latitude = 38.1129, center_longitude = 127.2002, center_location = ST_SetSRID(ST_MakePoint(127.2002, 38.1129), 4326) WHERE prefix = '41650'; -- 포천시
UPDATE region_type SET center_latitude = 37.2978, center_longitude = 127.6374, center_location = ST_SetSRID(ST_MakePoint(127.6374, 37.2978), 4326) WHERE prefix = '41670'; -- 여주시
UPDATE region_type SET center_latitude = 38.0960, center_longitude = 127.0752, center_location = ST_SetSRID(ST_MakePoint(127.0752, 38.0960), 4326) WHERE prefix = '41800'; -- 연천군
UPDATE region_type SET center_latitude = 37.8316, center_longitude = 127.5097, center_location = ST_SetSRID(ST_MakePoint(127.5097, 37.8316), 4326) WHERE prefix = '41820'; -- 가평군
UPDATE region_type SET center_latitude = 37.4918, center_longitude = 127.4947, center_location = ST_SetSRID(ST_MakePoint(127.4947, 37.4918), 4326) WHERE prefix = '41830'; -- 양평군

-- 강원특별자치도 (42)
UPDATE region_type SET center_latitude = 37.8813, center_longitude = 127.7300, center_location = ST_SetSRID(ST_MakePoint(127.7300, 37.8813), 4326) WHERE prefix = '42110'; -- 춘천시
UPDATE region_type SET center_latitude = 37.3422, center_longitude = 127.9202, center_location = ST_SetSRID(ST_MakePoint(127.9202, 37.3422), 4326) WHERE prefix = '42130'; -- 원주시
UPDATE region_type SET center_latitude = 37.7519, center_longitude = 128.8761, center_location = ST_SetSRID(ST_MakePoint(128.8761, 37.7519), 4326) WHERE prefix = '42150'; -- 강릉시
UPDATE region_type SET center_latitude = 37.5247, center_longitude = 129.1144, center_location = ST_SetSRID(ST_MakePoint(129.1144, 37.5247), 4326) WHERE prefix = '42170'; -- 동해시
UPDATE region_type SET center_latitude = 37.1641, center_longitude = 128.9856, center_location = ST_SetSRID(ST_MakePoint(128.9856, 37.1641), 4326) WHERE prefix = '42190'; -- 태백시
UPDATE region_type SET center_latitude = 38.2070, center_longitude = 128.5918, center_location = ST_SetSRID(ST_MakePoint(128.5918, 38.2070), 4326) WHERE prefix = '42210'; -- 속초시
UPDATE region_type SET center_latitude = 37.4500, center_longitude = 129.1656, center_location = ST_SetSRID(ST_MakePoint(129.1656, 37.4500), 4326) WHERE prefix = '42230'; -- 삼척시
UPDATE region_type SET center_latitude = 37.6974, center_longitude = 127.8890, center_location = ST_SetSRID(ST_MakePoint(127.8890, 37.6974), 4326) WHERE prefix = '42720'; -- 홍천군
UPDATE region_type SET center_latitude = 37.4827, center_longitude = 127.9858, center_location = ST_SetSRID(ST_MakePoint(127.9858, 37.4827), 4326) WHERE prefix = '42730'; -- 횡성군
UPDATE region_type SET center_latitude = 37.1837, center_longitude = 128.4615, center_location = ST_SetSRID(ST_MakePoint(128.4615, 37.1837), 4326) WHERE prefix = '42750'; -- 영월군
UPDATE region_type SET center_latitude = 37.3708, center_longitude = 128.3901, center_location = ST_SetSRID(ST_MakePoint(128.3901, 37.3708), 4326) WHERE prefix = '42760'; -- 평창군
UPDATE region_type SET center_latitude = 37.3806, center_longitude = 128.6686, center_location = ST_SetSRID(ST_MakePoint(128.6686, 37.3806), 4326) WHERE prefix = '42770'; -- 정선군
UPDATE region_type SET center_latitude = 38.1468, center_longitude = 127.3137, center_location = ST_SetSRID(ST_MakePoint(127.3137, 38.1468), 4326) WHERE prefix = '42780'; -- 철원군
UPDATE region_type SET center_latitude = 38.1064, center_longitude = 127.7083, center_location = ST_SetSRID(ST_MakePoint(127.7083, 38.1064), 4326) WHERE prefix = '42790'; -- 화천군
UPDATE region_type SET center_latitude = 38.0679, center_longitude = 127.9893, center_location = ST_SetSRID(ST_MakePoint(127.9893, 38.0679), 4326) WHERE prefix = '42800'; -- 양구군
UPDATE region_type SET center_latitude = 38.0695, center_longitude = 128.1709, center_location = ST_SetSRID(ST_MakePoint(128.1709, 38.0695), 4326) WHERE prefix = '42810'; -- 인제군
UPDATE region_type SET center_latitude = 38.3805, center_longitude = 128.4686, center_location = ST_SetSRID(ST_MakePoint(128.4686, 38.3805), 4326) WHERE prefix = '42820'; -- 고성군
UPDATE region_type SET center_latitude = 38.0752, center_longitude = 128.6190, center_location = ST_SetSRID(ST_MakePoint(128.6190, 38.0752), 4326) WHERE prefix = '42830'; -- 양양군

-- 충청북도 (43)
UPDATE region_type SET center_latitude = 36.6426, center_longitude = 127.4890, center_location = ST_SetSRID(ST_MakePoint(127.4890, 36.6426), 4326) WHERE prefix = '43111'; -- 청주시 상당구
UPDATE region_type SET center_latitude = 36.6367, center_longitude = 127.3828, center_location = ST_SetSRID(ST_MakePoint(127.3828, 36.6367), 4326) WHERE prefix = '43112'; -- 청주시 서원구
UPDATE region_type SET center_latitude = 36.6356, center_longitude = 127.4455, center_location = ST_SetSRID(ST_MakePoint(127.4455, 36.6356), 4326) WHERE prefix = '43113'; -- 청주시 흥덕구
UPDATE region_type SET center_latitude = 36.6426, center_longitude = 127.5298, center_location = ST_SetSRID(ST_MakePoint(127.5298, 36.6426), 4326) WHERE prefix = '43114'; -- 청주시 청원구
UPDATE region_type SET center_latitude = 36.9910, center_longitude = 127.9267, center_location = ST_SetSRID(ST_MakePoint(127.9267, 36.9910), 4326) WHERE prefix = '43130'; -- 충주시
UPDATE region_type SET center_latitude = 37.1326, center_longitude = 128.1908, center_location = ST_SetSRID(ST_MakePoint(128.1908, 37.1326), 4326) WHERE prefix = '43150'; -- 제천시
UPDATE region_type SET center_latitude = 36.4894, center_longitude = 127.7293, center_location = ST_SetSRID(ST_MakePoint(127.7293, 36.4894), 4326) WHERE prefix = '43720'; -- 보은군
UPDATE region_type SET center_latitude = 36.3014, center_longitude = 127.5719, center_location = ST_SetSRID(ST_MakePoint(127.5719, 36.3014), 4326) WHERE prefix = '43730'; -- 옥천군
UPDATE region_type SET center_latitude = 36.1751, center_longitude = 127.7837, center_location = ST_SetSRID(ST_MakePoint(127.7837, 36.1751), 4326) WHERE prefix = '43740'; -- 영동군
UPDATE region_type SET center_latitude = 36.7896, center_longitude = 127.5816, center_location = ST_SetSRID(ST_MakePoint(127.5816, 36.7896), 4326) WHERE prefix = '43745'; -- 증평군
UPDATE region_type SET center_latitude = 36.8555, center_longitude = 127.4333, center_location = ST_SetSRID(ST_MakePoint(127.4333, 36.8555), 4326) WHERE prefix = '43750'; -- 진천군
UPDATE region_type SET center_latitude = 36.8151, center_longitude = 127.7869, center_location = ST_SetSRID(ST_MakePoint(127.7869, 36.8151), 4326) WHERE prefix = '43760'; -- 괴산군
UPDATE region_type SET center_latitude = 36.9403, center_longitude = 127.5773, center_location = ST_SetSRID(ST_MakePoint(127.5773, 36.9403), 4326) WHERE prefix = '43770'; -- 음성군
UPDATE region_type SET center_latitude = 36.9896, center_longitude = 128.3661, center_location = ST_SetSRID(ST_MakePoint(128.3661, 36.9896), 4326) WHERE prefix = '43800'; -- 단양군

-- 충청남도 (44)
UPDATE region_type SET center_latitude = 36.8151, center_longitude = 127.1539, center_location = ST_SetSRID(ST_MakePoint(127.1539, 36.8151), 4326) WHERE prefix = '44131'; -- 천안시 동남구
UPDATE region_type SET center_latitude = 36.8151, center_longitude = 127.1039, center_location = ST_SetSRID(ST_MakePoint(127.1039, 36.8151), 4326) WHERE prefix = '44133'; -- 천안시 서북구
UPDATE region_type SET center_latitude = 36.4466, center_longitude = 127.1188, center_location = ST_SetSRID(ST_MakePoint(127.1188, 36.4466), 4326) WHERE prefix = '44150'; -- 공주시
UPDATE region_type SET center_latitude = 36.3334, center_longitude = 126.6128, center_location = ST_SetSRID(ST_MakePoint(126.6128, 36.3334), 4326) WHERE prefix = '44180'; -- 보령시
UPDATE region_type SET center_latitude = 36.7896, center_longitude = 127.0017, center_location = ST_SetSRID(ST_MakePoint(127.0017, 36.7896), 4326) WHERE prefix = '44200'; -- 아산시
UPDATE region_type SET center_latitude = 36.7848, center_longitude = 126.4503, center_location = ST_SetSRID(ST_MakePoint(126.4503, 36.7848), 4326) WHERE prefix = '44210'; -- 서산시
UPDATE region_type SET center_latitude = 36.1869, center_longitude = 127.0987, center_location = ST_SetSRID(ST_MakePoint(127.0987, 36.1869), 4326) WHERE prefix = '44230'; -- 논산시
UPDATE region_type SET center_latitude = 36.2742, center_longitude = 127.2486, center_location = ST_SetSRID(ST_MakePoint(127.2486, 36.2742), 4326) WHERE prefix = '44250'; -- 계룡시
UPDATE region_type SET center_latitude = 36.8932, center_longitude = 126.6468, center_location = ST_SetSRID(ST_MakePoint(126.6468, 36.8932), 4326) WHERE prefix = '44270'; -- 당진시
UPDATE region_type SET center_latitude = 36.1089, center_longitude = 127.4882, center_location = ST_SetSRID(ST_MakePoint(127.4882, 36.1089), 4326) WHERE prefix = '44710'; -- 금산군
UPDATE region_type SET center_latitude = 36.2756, center_longitude = 126.9100, center_location = ST_SetSRID(ST_MakePoint(126.9100, 36.2756), 4326) WHERE prefix = '44760'; -- 부여군
UPDATE region_type SET center_latitude = 36.0813, center_longitude = 126.6919, center_location = ST_SetSRID(ST_MakePoint(126.6919, 36.0813), 4326) WHERE prefix = '44770'; -- 서천군
UPDATE region_type SET center_latitude = 36.4593, center_longitude = 126.8024, center_location = ST_SetSRID(ST_MakePoint(126.8024, 36.4593), 4326) WHERE prefix = '44790'; -- 청양군
UPDATE region_type SET center_latitude = 36.6013, center_longitude = 126.6650, center_location = ST_SetSRID(ST_MakePoint(126.6650, 36.6013), 4326) WHERE prefix = '44800'; -- 홍성군
UPDATE region_type SET center_latitude = 36.6828, center_longitude = 126.8507, center_location = ST_SetSRID(ST_MakePoint(126.8507, 36.6828), 4326) WHERE prefix = '44810'; -- 예산군
UPDATE region_type SET center_latitude = 36.7458, center_longitude = 126.2981, center_location = ST_SetSRID(ST_MakePoint(126.2981, 36.7458), 4326) WHERE prefix = '44825'; -- 태안군

-- 전북특별자치도 (45)
UPDATE region_type SET center_latitude = 35.8203, center_longitude = 127.1089, center_location = ST_SetSRID(ST_MakePoint(127.1089, 35.8203), 4326) WHERE prefix = '45111'; -- 전주시 완산구
UPDATE region_type SET center_latitude = 35.8494, center_longitude = 127.1289, center_location = ST_SetSRID(ST_MakePoint(127.1289, 35.8494), 4326) WHERE prefix = '45113'; -- 전주시 덕진구
UPDATE region_type SET center_latitude = 35.9676, center_longitude = 126.7369, center_location = ST_SetSRID(ST_MakePoint(126.7369, 35.9676), 4326) WHERE prefix = '45130'; -- 군산시
UPDATE region_type SET center_latitude = 35.9483, center_longitude = 126.9544, center_location = ST_SetSRID(ST_MakePoint(126.9544, 35.9483), 4326) WHERE prefix = '45140'; -- 익산시
UPDATE region_type SET center_latitude = 35.5697, center_longitude = 126.8560, center_location = ST_SetSRID(ST_MakePoint(126.8560, 35.5697), 4326) WHERE prefix = '45180'; -- 정읍시
UPDATE region_type SET center_latitude = 35.4164, center_longitude = 127.3903, center_location = ST_SetSRID(ST_MakePoint(127.3903, 35.4164), 4326) WHERE prefix = '45190'; -- 남원시
UPDATE region_type SET center_latitude = 35.8031, center_longitude = 126.8811, center_location = ST_SetSRID(ST_MakePoint(126.8811, 35.8031), 4326) WHERE prefix = '45210'; -- 김제시
UPDATE region_type SET center_latitude = 35.9053, center_longitude = 127.2466, center_location = ST_SetSRID(ST_MakePoint(127.2466, 35.9053), 4326) WHERE prefix = '45710'; -- 완주군
UPDATE region_type SET center_latitude = 35.7915, center_longitude = 127.4245, center_location = ST_SetSRID(ST_MakePoint(127.4245, 35.7915), 4326) WHERE prefix = '45720'; -- 진안군
UPDATE region_type SET center_latitude = 36.0071, center_longitude = 127.6605, center_location = ST_SetSRID(ST_MakePoint(127.6605, 36.0071), 4326) WHERE prefix = '45730'; -- 무주군
UPDATE region_type SET center_latitude = 35.6475, center_longitude = 127.5207, center_location = ST_SetSRID(ST_MakePoint(127.5207, 35.6475), 4326) WHERE prefix = '45740'; -- 장수군
UPDATE region_type SET center_latitude = 35.6178, center_longitude = 127.2861, center_location = ST_SetSRID(ST_MakePoint(127.2861, 35.6178), 4326) WHERE prefix = '45750'; -- 임실군
UPDATE region_type SET center_latitude = 35.3747, center_longitude = 127.1376, center_location = ST_SetSRID(ST_MakePoint(127.1376, 35.3747), 4326) WHERE prefix = '45770'; -- 순창군
UPDATE region_type SET center_latitude = 35.4357, center_longitude = 126.7018, center_location = ST_SetSRID(ST_MakePoint(126.7018, 35.4357), 4326) WHERE prefix = '45790'; -- 고창군
UPDATE region_type SET center_latitude = 35.7318, center_longitude = 126.7338, center_location = ST_SetSRID(ST_MakePoint(126.7338, 35.7318), 4326) WHERE prefix = '45800'; -- 부안군

-- 전라남도 (46)
UPDATE region_type SET center_latitude = 34.8118, center_longitude = 126.3922, center_location = ST_SetSRID(ST_MakePoint(126.3922, 34.8118), 4326) WHERE prefix = '46110'; -- 목포시
UPDATE region_type SET center_latitude = 34.7604, center_longitude = 127.6622, center_location = ST_SetSRID(ST_MakePoint(127.6622, 34.7604), 4326) WHERE prefix = '46130'; -- 여수시
UPDATE region_type SET center_latitude = 34.9506, center_longitude = 127.4872, center_location = ST_SetSRID(ST_MakePoint(127.4872, 34.9506), 4326) WHERE prefix = '46150'; -- 순천시
UPDATE region_type SET center_latitude = 35.0160, center_longitude = 126.7107, center_location = ST_SetSRID(ST_MakePoint(126.7107, 35.0160), 4326) WHERE prefix = '46170'; -- 나주시
UPDATE region_type SET center_latitude = 34.9404, center_longitude = 127.6961, center_location = ST_SetSRID(ST_MakePoint(127.6961, 34.9404), 4326) WHERE prefix = '46230'; -- 광양시
UPDATE region_type SET center_latitude = 35.3211, center_longitude = 126.9880, center_location = ST_SetSRID(ST_MakePoint(126.9880, 35.3211), 4326) WHERE prefix = '46710'; -- 담양군
UPDATE region_type SET center_latitude = 35.2820, center_longitude = 127.2864, center_location = ST_SetSRID(ST_MakePoint(127.2864, 35.2820), 4326) WHERE prefix = '46720'; -- 곡성군
UPDATE region_type SET center_latitude = 35.2025, center_longitude = 127.4626, center_location = ST_SetSRID(ST_MakePoint(127.4626, 35.2025), 4326) WHERE prefix = '46730'; -- 구례군
UPDATE region_type SET center_latitude = 34.6114, center_longitude = 127.2756, center_location = ST_SetSRID(ST_MakePoint(127.2756, 34.6114), 4326) WHERE prefix = '46770'; -- 고흥군
UPDATE region_type SET center_latitude = 34.7714, center_longitude = 127.0800, center_location = ST_SetSRID(ST_MakePoint(127.0800, 34.7714), 4326) WHERE prefix = '46780'; -- 보성군
UPDATE region_type SET center_latitude = 35.0644, center_longitude = 126.9865, center_location = ST_SetSRID(ST_MakePoint(126.9865, 35.0644), 4326) WHERE prefix = '46790'; -- 화순군
UPDATE region_type SET center_latitude = 34.6814, center_longitude = 126.9067, center_location = ST_SetSRID(ST_MakePoint(126.9067, 34.6814), 4326) WHERE prefix = '46800'; -- 장흥군
UPDATE region_type SET center_latitude = 34.6416, center_longitude = 126.7675, center_location = ST_SetSRID(ST_MakePoint(126.7675, 34.6416), 4326) WHERE prefix = '46810'; -- 강진군
UPDATE region_type SET center_latitude = 34.5738, center_longitude = 126.5986, center_location = ST_SetSRID(ST_MakePoint(126.5986, 34.5738), 4326) WHERE prefix = '46820'; -- 해남군
UPDATE region_type SET center_latitude = 34.8004, center_longitude = 126.6967, center_location = ST_SetSRID(ST_MakePoint(126.6967, 34.8004), 4326) WHERE prefix = '46830'; -- 영암군
UPDATE region_type SET center_latitude = 34.9910, center_longitude = 126.4811, center_location = ST_SetSRID(ST_MakePoint(126.4811, 34.9910), 4326) WHERE prefix = '46840'; -- 무안군
UPDATE region_type SET center_latitude = 35.0658, center_longitude = 126.5168, center_location = ST_SetSRID(ST_MakePoint(126.5168, 35.0658), 4326) WHERE prefix = '46860'; -- 함평군
UPDATE region_type SET center_latitude = 35.2772, center_longitude = 126.5121, center_location = ST_SetSRID(ST_MakePoint(126.5121, 35.2772), 4326) WHERE prefix = '46870'; -- 영광군
UPDATE region_type SET center_latitude = 35.3014, center_longitude = 126.7844, center_location = ST_SetSRID(ST_MakePoint(126.7844, 35.3014), 4326) WHERE prefix = '46880'; -- 장성군
UPDATE region_type SET center_latitude = 34.3114, center_longitude = 126.7553, center_location = ST_SetSRID(ST_MakePoint(126.7553, 34.3114), 4326) WHERE prefix = '46890'; -- 완도군
UPDATE region_type SET center_latitude = 34.4867, center_longitude = 126.2637, center_location = ST_SetSRID(ST_MakePoint(126.2637, 34.4867), 4326) WHERE prefix = '46900'; -- 진도군
UPDATE region_type SET center_latitude = 34.8257, center_longitude = 126.1086, center_location = ST_SetSRID(ST_MakePoint(126.1086, 34.8257), 4326) WHERE prefix = '46910'; -- 신안군

-- 경상북도 (47)
UPDATE region_type SET center_latitude = 36.0190, center_longitude = 129.3435, center_location = ST_SetSRID(ST_MakePoint(129.3435, 36.0190), 4326) WHERE prefix = '47111'; -- 포항시 남구
UPDATE region_type SET center_latitude = 36.0190, center_longitude = 129.3652, center_location = ST_SetSRID(ST_MakePoint(129.3652, 36.0190), 4326) WHERE prefix = '47113'; -- 포항시 북구
UPDATE region_type SET center_latitude = 35.8562, center_longitude = 129.2247, center_location = ST_SetSRID(ST_MakePoint(129.2247, 35.8562), 4326) WHERE prefix = '47130'; -- 경주시
UPDATE region_type SET center_latitude = 36.1397, center_longitude = 128.1136, center_location = ST_SetSRID(ST_MakePoint(128.1136, 36.1397), 4326) WHERE prefix = '47150'; -- 김천시
UPDATE region_type SET center_latitude = 36.5684, center_longitude = 128.7294,center_location = ST_SetSRID(ST_MakePoint(128.7294, 36.5684), 4326) WHERE prefix = '47170'; -- 안동시
UPDATE region_type SET center_latitude = 36.1195, center_longitude = 128.3446, center_location = ST_SetSRID(ST_MakePoint(128.3446, 36.1195), 4326) WHERE prefix = '47190'; -- 구미시
UPDATE region_type SET center_latitude = 36.8056, center_longitude = 128.6236, center_location = ST_SetSRID(ST_MakePoint(128.6236, 36.8056), 4326) WHERE prefix = '47210'; -- 영주시
UPDATE region_type SET center_latitude = 35.9733, center_longitude = 128.9386, center_location = ST_SetSRID(ST_MakePoint(128.9386, 35.9733), 4326) WHERE prefix = '47230'; -- 영천시
UPDATE region_type SET center_latitude = 36.4106, center_longitude = 128.1590, center_location = ST_SetSRID(ST_MakePoint(128.1590, 36.4106), 4326) WHERE prefix = '47250'; -- 상주시
UPDATE region_type SET center_latitude = 36.5864, center_longitude = 128.1897, center_location = ST_SetSRID(ST_MakePoint(128.1897, 36.5864), 4326) WHERE prefix = '47280'; -- 문경시
UPDATE region_type SET center_latitude = 35.8250, center_longitude = 128.7412, center_location = ST_SetSRID(ST_MakePoint(128.7412, 35.8250), 4326) WHERE prefix = '47290'; -- 경산시
UPDATE region_type SET center_latitude = 36.3524, center_longitude = 128.6972, center_location = ST_SetSRID(ST_MakePoint(128.6972, 36.3524), 4326) WHERE prefix = '47720'; -- 의성군
UPDATE region_type SET center_latitude = 36.4359, center_longitude = 129.0570, center_location = ST_SetSRID(ST_MakePoint(129.0570, 36.4359), 4326) WHERE prefix = '47730'; -- 청송군
UPDATE region_type SET center_latitude = 36.6666, center_longitude = 129.1125, center_location = ST_SetSRID(ST_MakePoint(129.1125, 36.6666), 4326) WHERE prefix = '47750'; -- 영양군
UPDATE region_type SET center_latitude = 36.4152, center_longitude = 129.3657, center_location = ST_SetSRID(ST_MakePoint(129.3657, 36.4152), 4326) WHERE prefix = '47760'; -- 영덕군
UPDATE region_type SET center_latitude = 35.6476, center_longitude = 128.4892, center_location = ST_SetSRID(ST_MakePoint(128.4892, 35.6476), 4326) WHERE prefix = '47770'; -- 청도군
UPDATE region_type SET center_latitude = 35.7253, center_longitude = 128.2619, center_location = ST_SetSRID(ST_MakePoint(128.2619, 35.7253), 4326) WHERE prefix = '47820'; -- 고령군
UPDATE region_type SET center_latitude = 35.9195, center_longitude = 128.2813, center_location = ST_SetSRID(ST_MakePoint(128.2813, 35.9195), 4326) WHERE prefix = '47830'; -- 성주군
UPDATE region_type SET center_latitude = 35.9951, center_longitude = 128.4013, center_location = ST_SetSRID(ST_MakePoint(128.4013, 35.9951), 4326) WHERE prefix = '47840'; -- 칠곡군
UPDATE region_type SET center_latitude = 36.6558, center_longitude = 128.4526, center_location = ST_SetSRID(ST_MakePoint(128.4526, 36.6558), 4326) WHERE prefix = '47850'; -- 예천군
UPDATE region_type SET center_latitude = 36.8935, center_longitude = 128.7329, center_location = ST_SetSRID(ST_MakePoint(128.7329, 36.8935), 4326) WHERE prefix = '47900'; -- 봉화군
UPDATE region_type SET center_latitude = 36.9930, center_longitude = 129.4003, center_location = ST_SetSRID(ST_MakePoint(129.4003, 36.9930), 4326) WHERE prefix = '47920'; -- 울진군
UPDATE region_type SET center_latitude = 37.4845, center_longitude = 130.8984, center_location = ST_SetSRID(ST_MakePoint(130.8984, 37.4845), 4326) WHERE prefix = '47930'; -- 울릉군

-- 경상남도 (48)
UPDATE region_type SET center_latitude = 35.2538, center_longitude = 128.6402, center_location = ST_SetSRID(ST_MakePoint(128.6402, 35.2538), 4326) WHERE prefix = '48121'; -- 창원시 의창구
UPDATE region_type SET center_latitude = 35.2538, center_longitude = 128.6809, center_location = ST_SetSRID(ST_MakePoint(128.6809, 35.2538), 4326) WHERE prefix = '48123'; -- 창원시 성산구
UPDATE region_type SET center_latitude = 35.1979, center_longitude = 128.5733, center_location = ST_SetSRID(ST_MakePoint(128.5733, 35.1979), 4326) WHERE prefix = '48125'; -- 창원시 마산합포구
UPDATE region_type SET center_latitude = 35.2283, center_longitude = 128.5733, center_location = ST_SetSRID(ST_MakePoint(128.5733, 35.2283), 4326) WHERE prefix = '48127'; -- 창원시 마산회원구
UPDATE region_type SET center_latitude = 35.1660, center_longitude = 128.6444, center_location = ST_SetSRID(ST_MakePoint(128.6444, 35.1660), 4326) WHERE prefix = '48129'; -- 창원시 진해구
UPDATE region_type SET center_latitude = 35.1800, center_longitude = 128.1076, center_location = ST_SetSRID(ST_MakePoint(128.1076, 35.1800), 4326) WHERE prefix = '48170'; -- 진주시
UPDATE region_type SET center_latitude = 34.8544, center_longitude = 128.4332, center_location = ST_SetSRID(ST_MakePoint(128.4332, 34.8544), 4326) WHERE prefix = '48220'; -- 통영시
UPDATE region_type SET center_latitude = 35.0036, center_longitude = 128.0642, center_location = ST_SetSRID(ST_MakePoint(128.0642, 35.0036), 4326) WHERE prefix = '48240'; -- 사천시
UPDATE region_type SET center_latitude = 35.2285, center_longitude = 128.8894, center_location = ST_SetSRID(ST_MakePoint(128.8894, 35.2285), 4326) WHERE prefix = '48250'; -- 김해시
UPDATE region_type SET center_latitude = 35.5040, center_longitude = 128.7469, center_location = ST_SetSRID(ST_MakePoint(128.7469, 35.5040), 4326) WHERE prefix = '48270'; -- 밀양시
UPDATE region_type SET center_latitude = 34.8806, center_longitude = 128.6211, center_location = ST_SetSRID(ST_MakePoint(128.6211, 34.8806), 4326) WHERE prefix = '48310'; -- 거제시
UPDATE region_type SET center_latitude = 35.3350, center_longitude = 129.0372, center_location = ST_SetSRID(ST_MakePoint(129.0372, 35.3350), 4326) WHERE prefix = '48330'; -- 양산시
UPDATE region_type SET center_latitude = 35.3224, center_longitude = 128.2618, center_location = ST_SetSRID(ST_MakePoint(128.2618, 35.3224), 4326) WHERE prefix = '48720'; -- 의령군
UPDATE region_type SET center_latitude = 35.2723, center_longitude = 128.4062, center_location = ST_SetSRID(ST_MakePoint(128.4062, 35.2723), 4326) WHERE prefix = '48730'; -- 함안군
UPDATE region_type SET center_latitude = 35.5445, center_longitude = 128.4926, center_location = ST_SetSRID(ST_MakePoint(128.4926, 35.5445), 4326) WHERE prefix = '48740'; -- 창녕군
UPDATE region_type SET center_latitude = 34.9731, center_longitude = 128.3223, center_location = ST_SetSRID(ST_MakePoint(128.3223, 34.9731), 4326) WHERE prefix = '48820'; -- 고성군
UPDATE region_type SET center_latitude = 34.8375, center_longitude = 127.8924, center_location = ST_SetSRID(ST_MakePoint(127.8924, 34.8375), 4326) WHERE prefix = '48840'; -- 남해군
UPDATE region_type SET center_latitude = 35.0679, center_longitude = 127.7514, center_location = ST_SetSRID(ST_MakePoint(127.7514, 35.0679), 4326) WHERE prefix = '48850'; -- 하동군
UPDATE region_type SET center_latitude = 35.4150, center_longitude = 127.8734, center_location = ST_SetSRID(ST_MakePoint(127.8734, 35.4150), 4326) WHERE prefix = '48860'; -- 산청군
UPDATE region_type SET center_latitude = 35.5204, center_longitude = 127.7253, center_location = ST_SetSRID(ST_MakePoint(127.7253, 35.5204), 4326) WHERE prefix = '48870'; -- 함양군
UPDATE region_type SET center_latitude = 35.6866, center_longitude = 127.9098, center_location = ST_SetSRID(ST_MakePoint(127.9098, 35.6866), 4326) WHERE prefix = '48880'; -- 거창군
UPDATE region_type SET center_latitude = 35.5665, center_longitude = 128.1656, center_location = ST_SetSRID(ST_MakePoint(128.1656, 35.5665), 4326) WHERE prefix = '48890'; -- 합천군

-- 제주특별자치도 (50)
UPDATE region_type SET center_latitude = 33.4996, center_longitude = 126.5312, center_location = ST_SetSRID(ST_MakePoint(126.5312, 33.4996), 4326) WHERE prefix = '50110'; -- 제주시
UPDATE region_type SET center_latitude = 33.2541, center_longitude = 126.5600, center_location = ST_SetSRID(ST_MakePoint(126.5600, 33.2541), 4326) WHERE prefix = '50130'; -- 서귀포시