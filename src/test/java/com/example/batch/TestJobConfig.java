package com.example.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by gavinkim at 2018-12-09
 */
@EnableBatchProcessing // 해당 어노테이션을 사용하여 미리정의된 설정들을 실행 시키도록 한다.
@Configuration
public class TestJobConfig {

    /**
     * 배치의 Job 을 실행하여 테스트 하는 유틸리티 클래스이다.
     * @return
     */
    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() {
        //Job 실행에 필요한 JobLauncher 를 필드값으로 갖는 JobLauncherTestUtils 를 빈으로 등록
        return new JobLauncherTestUtils();
    }
}
