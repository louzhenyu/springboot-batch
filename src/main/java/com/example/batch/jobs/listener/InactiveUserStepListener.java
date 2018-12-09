package com.example.batch.jobs.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;


/**
 * Created by gavinkim at 2018-12-09
 */
@Slf4j
@Component
public class InactiveUserStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution){
        log.info("Before Step");
    }

    //Step 실행후 실행될 로직 추가.
    @Override
    public ExitStatus afterStep(StepExecution stepExecution){
        log.info("After Step");
        return null;//fixme: 리턴 값은 무엇인가?
    }
}
