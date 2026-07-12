package org.sopt.haphap.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.alram.domain.AlramSetting;
import org.sopt.haphap.domain.alram.domain.DeviceType;
import org.sopt.haphap.domain.alram.domain.PushToken;
import org.sopt.haphap.domain.alram.repository.AlramSettingRepository;
import org.sopt.haphap.domain.alram.repository.PushTokenRepository;
import org.sopt.haphap.domain.posting.domain.Category;
import org.sopt.haphap.domain.posting.domain.Company;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.repository.CategoryRepository;
import org.sopt.haphap.domain.posting.repository.CompanyRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
/*
@Slf4j
@Component
@Profile("retrytest")
@RequiredArgsConstructor
public class RetryTestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PostingRepository postingRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final PushTokenRepository pushTokenRepository;
    private final AlramSettingRepository alramSettingRepository;

    @Override
    public void run(String... args) {
        // 등록자 1명 + 구독자 1명
        User registrant = userRepository.save(User.builder()
                .anonymousName("등록자").name("등록자")
                .provider(Provider.LOCAL).providerId("registrant").build());
        User subscriber = userRepository.save(User.builder()
                .anonymousName("구독자").name("구독자")
                .provider(Provider.LOCAL).providerId("subscriber").build());

        Category category = categoryRepository.save(Category.create("백엔드"));
        Company company = companyRepository.save(Company.create("토스"));
        Posting posting = postingRepository.save(
                Posting.create("재시도 테스트 공고", LocalDate.now().plusDays(30), category, company));

        // 구독자가 알람 ON
        alramSettingRepository.save(AlramSetting.create(subscriber, posting, true));

        // 구독자에게 4종류 토큰 부여 → 발송 시 각각 다른 경로를 탐
        pushTokenRepository.save(PushToken.create(subscriber, "device-retry", "token-retry", DeviceType.ANDROID));
        pushTokenRepository.save(PushToken.create(subscriber, "device-dead", "token-dead", DeviceType.IOS));
        pushTokenRepository.save(PushToken.create(subscriber, "device-fail", "token-fail", DeviceType.WEB));
        pushTokenRepository.save(PushToken.create(subscriber, "device-ok", "token-ok-normal", DeviceType.ANDROID));

        log.info("=== retrytest 데이터 준비 완료 ===");
        log.info("등록자 userId={}, 구독자 userId={}, postingId={}",
                registrant.getId(), subscriber.getId(), posting.getId());
    }
}

 */
