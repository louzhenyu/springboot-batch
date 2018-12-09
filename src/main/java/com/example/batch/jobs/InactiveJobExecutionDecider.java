package com.example.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.util.Random;

/**
 * Created by gavinkim at 2018-12-09
 * 조건에 따라 JobStep 을 실행 시키도록 하기 위해 JobExecutionDecider 클래스를 구현 하도록 한다.
 *
 * FlowExecutionStatus 객체는 상태값 completed,stopped,failed,unknown 등을 제공한다.
 */
@Slf4j
public class InactiveJobExecutionDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        //예: 양수이면 JobStep 실행, 아니면 동작 하지 않도록 아래와 같이 랜덤 상수를 생성하여 판별한 후 상태를 리턴 하도록 한다.
        //음수가 나올 경우 실행 불가, 아닐 경우 실행 하도록 아래에서 조건 설정.
        if(new Random().nextInt() > 0){
            log.info("FlowExecutionStatus.COMPLETED");
            return FlowExecutionStatus.COMPLETED;
        }
        log.info("FlowExecutionStatus.FAILED");
        return FlowExecutionStatus.FAILED;
    }
}
