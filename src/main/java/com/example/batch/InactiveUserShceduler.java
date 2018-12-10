package com.example.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by gavinkim at 2018-12-09
 */
@Slf4j
@Service
public class InactiveUserShceduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job processJob;

    @Scheduled(fixedDelay = 10000)
    public void executeJob(){
        log.info("Started Job Scheduler...");
        Date nowDate = new Date();
        try {
            JobParameters jobParameter = new JobParametersBuilder().addDate("nowDate",nowDate)
                    .toJobParameters();
            jobLauncher.run(processJob,jobParameter);
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }
}
