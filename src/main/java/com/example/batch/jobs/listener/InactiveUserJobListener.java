package com.example.batch.jobs.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Created by gavinkim at 2018-12-09
 * job 의 전후 처리 실행시 특정 로직 (여기서는 로그만 출력하도록 한다.) 을 실행 하도록 한다.
 */
@Slf4j
@Component
public class InactiveUserJobListener implements JobExecutionListener {
    //Job 실행 전 수행될 로직을 구현 하도록 한다.
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Before Job");
    }

    //Job 실행후 수행될 로직을 구현 하도록 한다.
    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("After Job");
    }
}
