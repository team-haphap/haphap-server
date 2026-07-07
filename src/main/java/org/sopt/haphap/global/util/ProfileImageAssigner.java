package org.sopt.haphap.global.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ProfileImageAssigner {

    // 수정
    private static final String BASE_URL =
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/";

    private static final List<String> PROFILE_IMAGE_URLS = List.of(
            BASE_URL + "profile_01.png",
            BASE_URL + "profile_02.png",
            BASE_URL + "profile_03.png",
            BASE_URL + "profile_04.png",
            BASE_URL + "profile_05.png",
            BASE_URL + "profile_06.png",
            BASE_URL + "profile_07.png",
            BASE_URL + "profile_08.png",
            BASE_URL + "profile_09.png",
            BASE_URL + "profile_10.png"
    );

    public String assign() {
        int index = ThreadLocalRandom.current().nextInt(PROFILE_IMAGE_URLS.size());
        return PROFILE_IMAGE_URLS.get(index);
    }
}