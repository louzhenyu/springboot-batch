package com.example.batch.jobs;

import com.example.batch.domain.User;
import com.example.batch.domain.enums.UserStatus;
import com.example.batch.jobs.readers.QueueItemReader;
import com.example.batch.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by gavinkim at 2018-12-09
 * 휴면 회원 배치 정보 설정
 * 1. 휴면 회원 Job 설정
 * 2. 휴면 회원 Step 설정
 * 3. 휴면 회원 Reader.Processor.Writer 설정
 */
@AllArgsConstructor
@Configuration
public class InactiveUserJobConfig {

    private UserRepository userRepository;

    /**
     * Job 생성을 위한 JobBuilderFactory 주입.
     * - 빈에 주입할 객체를 param 으로 명시할 경우 @Autowired 를 사용한것과 같다.
     * @param jobBuilderFactory
     * @param inactiveJobStep
     * @return
     */
    @Bean
    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory, Step inactiveJobStep){
        return jobBuilderFactory.get("inactiveUserJob") // inactiveUserJob 이라는 이름의 JobBuilder 생성
                .preventRestart() // Job 의 재실행 방지
                .start(inactiveJobStep) // param 에서 주입받은 휴면 회원 관련 Step 인 inactiveJobStep 을 가장 먼저 실행 하도록 설정.
                .build();
    }

    /**
     * Step 을 위한 StepBuilderFactory 주입.
     * @param stepBuilderFactory
     * @return
     */
    @Bean
    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory){
        return stepBuilderFactory.get("inactiveUserStep")//inactiveUserStep 이라는 이름의 StepBuilder 생성
                //Generic 을 사용하여 chunk() 의 입력타입과 출력타입을 User 타입으로 설정.
                //chunk 단위로 묶어서 writer()를 실행시킬 단위를 지정한것이다.
                //commit 단위가 10개로 정한것이다.
                .<User, User> chunk(10)
                .reader(inactiveUserReader()) //reader (회원 정보 가져오기)
                .processor(inactiveUserProcessor())//processor (로직 수행)
                .writer(inactiveUserWriter())//wirter (회원정보 저장)
                .build();
    }

    /**
     * 기본 빈 생성은 싱글톤이다. StepScope 를 사용할 경우 해당 메서드는 Step 의 주기에 따라 새로운 빈을 생성한다.
     * step 의 실행마다 새로 빈을 만들기 때문에 지연 생성이 가능함.
     * 주의 사항: 기본 프록시 모드가 반환되는 클래스 타입을 참조하기 때문에 StepScope 를 사용할 경우 반드시 구현된 반환타입을 명시해 반환해야 한다.
     * @return
     */
    @Bean
    @StepScope
    public QueueItemReader<User> inactiveUserReader() {
        List<User> oldUsers = userRepository.findByUpdatedDateBeforeAndStatusEquals(LocalDateTime.now().minusYears(1), UserStatus.ACTIVE);
        return new QueueItemReader<>(oldUsers);
    }

    public ItemProcessor<? super User,? extends User> inactiveUserProcessor() {
        return User::setInactive;
    }

    public ItemWriter<User> inactiveUserWriter() {
        //return (userRepository::saveAll); // 아래와 동일함.
        return ((List<? extends User> users)->userRepository.saveAll(users));
    }

}
