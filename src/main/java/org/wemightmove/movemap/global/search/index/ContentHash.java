package org.wemightmove.movemap.global.search.index;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ES 문서의 {@code content_hash} 계산 유틸.
 * <p>
 * 색인 대상 텍스트 필드를 <b>고정된 순서</b>로 {@code |} 구분자로 이어붙인 canonical 문자열의
 * SHA-256 hex 를 반환한다. null 은 빈 문자열로 취급하며, 동일 입력에 대해 항상 동일한 값을
 * 산출한다(결정적). PG(원본)의 값과 ES(파생) 문서의 값을 비교해 드리프트를 탐지하는 데 쓰인다.
 * <ul>
 *   <li>program: {@code name|facility_name|facility_subtype|address}</li>
 *   <li>facility: {@code name|facility_type|facility_subtype|address}</li>
 * </ul>
 */
public final class ContentHash {

    private static final char SEPARATOR = '|';

    private ContentHash() {
    }

    /**
     * 주어진 파트들을 고정 순서로 {@code |} 결합(null→"")한 뒤 SHA-256 hex 를 반환한다.
     */
    public static String of(String... parts) {
        StringBuilder canonical = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                canonical.append(SEPARATOR);
            }
            canonical.append(parts[i] == null ? "" : parts[i]);
        }
        return sha256Hex(canonical.toString());
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 은 모든 JVM 이 반드시 지원(스펙 필수) → 실질적으로 도달 불가.
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
