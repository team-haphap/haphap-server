package org.sopt.haphap.domain.user.service;

import java.util.regex.Pattern;
import org.sopt.haphap.global.code.MemberErrorCode;
import org.sopt.haphap.global.exception.CustomException;

// 사용자가 정하는 닉네임 규칙. 저장은 anonymousName("익명의 " + 닉네임) 한 컬럼에 한다.
public final class NicknamePolicy {

    public static final String PREFIX = "익명의 ";
    public static final int MIN_LENGTH = 2;
    public static final int MAX_LENGTH = 5;

    // 활성 유저 닉네임 유일성 (V43 부분 유니크 인덱스)
    public static final String UNIQUE_CONSTRAINT = "uk_users_active_anonymous_name";

    // 완성형 한글, 영문, 숫자만. 자모(ㄱ, ㅏ)·공백·특수문자·이모지는 불가
    private static final Pattern DISALLOWED = Pattern.compile("[^가-힣a-zA-Z0-9]");

    private NicknamePolicy() {}

    // 형식 검증 후 저장할 anonymousName으로 바꾼다. 허용 안 되는 문자가 먼저, 그다음 길이(빈 문자열은 길이 에러)
    public static String toAnonymousName(String nickname) {
        if (DISALLOWED.matcher(nickname).find()) {
            throw new CustomException(MemberErrorCode.INVALID_NICKNAME_CHARACTER);
        }
        int length = nickname.codePointCount(0, nickname.length());
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new CustomException(MemberErrorCode.INVALID_NICKNAME_LENGTH);
        }
        return PREFIX + nickname;
    }

    public static String toValidationAnonymousName(String nickname) {
        if (nickname == null || DISALLOWED.matcher(nickname).find()) {
            throw new CustomException(MemberErrorCode.INVALID_NICKNAME_FORMAT);
        }

        int length = nickname.codePointCount(0, nickname.length());
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new CustomException(MemberErrorCode.INVALID_NICKNAME_FORMAT);
        }

        return PREFIX + nickname;
    }

    // anonymousName에서 닉네임 부분만. 접두어가 없는 예전 데이터는 그대로 돌려준다.
    public static String toNickname(String anonymousName) {
        if (anonymousName == null || !anonymousName.startsWith(PREFIX)) {
            return anonymousName;
        }
        return anonymousName.substring(PREFIX.length());
    }
}
