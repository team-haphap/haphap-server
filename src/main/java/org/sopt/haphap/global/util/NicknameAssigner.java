package org.sopt.haphap.global.util;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class NicknameAssigner {

    private final UserRepository userRepository;
    private final AnonymousNameGenerator anonymousNameGenerator;

    public String assign() {
        List<String> usedPresetNames = userRepository.findAnonymousNamesIn(PresetNicknames.POOL);
        List<String> available = PresetNicknames.POOL.stream()
                .filter(name -> !usedPresetNames.contains(name))
                .toList();

        if (!available.isEmpty()) {
            return available.get(ThreadLocalRandom.current().nextInt(available.size()));
        }

        // 프리셋 50개 소진 후 폴백 경로. 음절 조합 랜덤 생성이라 닉네임 중복 가능성 있음.
        // 스프린트 때 중복 방지 로직(재시도, 프리셋 확장 등) 추가 예정
        return anonymousNameGenerator.generate();
    }
}