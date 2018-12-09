package com.example.batch;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by gavinkim at 2018-12-09
 */
@Service
public class InactiveUserShceduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Scheduled(fixedDelay = 1000)
    public void executeJob(){
            
    }

}
