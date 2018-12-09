package com.example.batch;

import com.example.batch.domain.enums.UserStatus;
import com.example.batch.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by gavinkim at 2018-12-09
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class InactiveUserJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void convertDormantUser()throws Exception{
        Date nowDate = new Date();
        //launchJob() 메서드로 Job 실행
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(
                //JobParameters 생성
                //JobParameter -> Step 을 실행시킬때 동적으로 파라미터를 주입시킬수 있음.
                //nowDate 를 동일하게 chunk 단위로 가져오는 아이템들에 적용하도록 하기 위해 주입함.
                new JobParametersBuilder().addDate("nowDate",nowDate)
                .toJobParameters()
        );

        //completed 이면 job의 실행여부 테스트는 성공이다.
        assertEquals(BatchStatus.COMPLETED,jobExecution.getStatus());
        //업데이트된 날짜가 1년전이며, user상태값이 active 인 사용자들이 없어야 휴면회원 배치 테스트가 성공이다.
        assertEquals(0,userRepository.findByUpdatedDateBeforeAndStatusEquals(LocalDateTime.now().minusYears(1), UserStatus.ACTIVE).size());
    }
}
