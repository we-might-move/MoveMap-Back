# 로컬 perf 전용 Elasticsearch(+Nori) 이미지.
# 버전은 co.elastic.clients:elasticsearch-java(스프링 부트 3.5.7 BOM이 관리하는
# spring-boot-starter-data-elasticsearch가 끌어오는 저수준 클라이언트)와 같은 마이너(8.18.x)를 사용한다.
# `./gradlew dependencies --configuration runtimeClasspath | grep elasticsearch-java`로 확인: 8.18.8 (마이너 8.18 일치).
FROM docker.elastic.co/elasticsearch/elasticsearch:8.18.3

RUN bin/elasticsearch-plugin install --batch analysis-nori
