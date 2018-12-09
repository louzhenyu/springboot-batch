package com.example.batch.jobs;

import com.example.batch.domain.User;
import com.example.batch.domain.enums.Grade;
import com.example.batch.domain.enums.UserStatus;
import com.example.batch.jobs.listener.InactiveUserChunkListener;
import com.example.batch.jobs.listener.InactiveUserJobListener;
import com.example.batch.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by gavinkim at 2018-12-09
 * 휴면 회원 배치 정보 설정
 * 1. 휴면 회원 Job 설정
 * 2. 휴면 회원 Step 설정
 * 3. 휴면 회원 Reader.Processor.Writer 설정
 */
@Slf4j
@AllArgsConstructor
@Configuration
public class InactiveUserJobConfig {

    private final static int CHUNK_SIZE = 5;
    private final EntityManagerFactory entityManagerFactory;

    private UserRepository userRepository;

    /**
     * Job 생성을 위한 JobBuilderFactory 주입.
     * - 빈에 주입할 객체를 param 으로 명시할 경우 @Autowired 를 사용한것과 같다.
     */
    @Bean
    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory,
                               InactiveUserJobListener jobListener,//리스너 주입
                               Step parititionerStep){
        return jobBuilderFactory.get("inactiveUserJob") // inactiveUserJob 이라는 이름의 JobBuilder 생성
                .preventRestart() // Job 의 재실행 방지
                .listener(jobListener) // listener 추가.
                .start(parititionerStep)
                .build();
    }

    @Bean
    @JobScope //job 실행시마다 새로 생성하도록 추가함.
    public Step parititionerStep(StepBuilderFactory stepBuilderFactory, Step inactiveJobStep) {
        return stepBuilderFactory
                .get("parititionerStep")
                .partitioner("parititionerStep", new InactiveUserPartitioner())
                .gridSize(CHUNK_SIZE) // CHUNK_SIZE 개수만큼 실행하도록 등록한다. Grade Enum 이 3이기때문에 3이상으로 지정하도록 한다.
                .step(inactiveJobStep)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory,
                                ListItemReader<User> inactiveUerJpaReader,
                                InactiveUserChunkListener inactiveUserChunkListener){
        return stepBuilderFactory.get("inactiveUserStep")//inactiveUserStep 이라는 이름의 StepBuilder 생성
                .<User, User> chunk(CHUNK_SIZE)
                .reader(inactiveUerJpaReader) //reader (회원 정보 가져오기)
                .processor(inactiveUserProcessor())//processor (로직 수행)
                .writer(inactiveUserWriter())//wirter (회원정보 저장)
                .listener(inactiveUserChunkListener) // step 실행전후 로직 실행을 위해 추가.
                .build();
    }


    /**
     * 배치가 실행시 처리할 로직을 담당하는 프로세서
     * @return
     */
    public ItemProcessor<? super User,? extends User> inactiveUserProcessor() {
        return new InactiveUserItemProcessor();
    }

    @Bean
    @StepScope
    public ListItemReader<User> inactiveUserReader(@Value("#{stepExecutionContext[grade]}") String grade, UserRepository userRepository) {
        log.info(Thread.currentThread().getName());
        List<User> inactiveUsers = userRepository.findByCreatedDateBeforeAndStatusEqualsAndGradeEquals(LocalDateTime.now().minusYears(1), UserStatus.ACTIVE, Grade.valueOf(grade));
        return new ListItemReader<>(inactiveUsers);
    }

    //페이징 처리 필요시 아래 리더를 사용할것.
    /*
    @Bean(destroyMethod="")
    @StepScope
    public JpaPagingItemReader<User> inactiveUserJpaReader(@Value("#{jobParameters[nowDate]}") Date nowDate) {
        JpaPagingItemReader<User> jpaPagingItemReader = new JpaPagingItemReader(){
            //반드시 CHUNK_SIZE 만큼씩 가져와서 작업하도록 하기 위해 0을 리턴 해주도록 한다.
            @Override
            public int getPage() {
                return 0;
            }
        };
        jpaPagingItemReader.setQueryString("select u from User as u where u.createdDate < :createdDate and u.status = :status");
        Map<String, Object> map = new HashMap<>();
        LocalDateTime now = LocalDateTime.ofInstant(nowDate.toInstant(), ZoneId.systemDefault());
        map.put("createdDate", now.minusYears(1));
        map.put("status", UserStatus.ACTIVE);
        jpaPagingItemReader.setParameterValues(map);
        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory);
        jpaPagingItemReader.setPageSize(CHUNK_SIZE);
        return jpaPagingItemReader;
    }
    */


    /**
     * 저장 설정 필요 없이 generic 에 저장할 타입을 명시하고, entityManagerFactory 만 설정 하면 processor 에서 넘어온 데이터를 청크 단위로 저장한다.
     */
    public ItemWriter<User> inactiveUserWriter() {
        JpaItemWriter<User> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    /*
    public ItemWriter<User> inactiveUserWriter() {
        return ((List<? extends User> users) -> userRepository.saveAll(users));
    }
    */

    @Bean
    public TaskExecutor taskExecutor(){
        return new SimpleAsyncTaskExecutor("Batch_Task"); //SimpleAsyncTaskExecutor 는 스레드 요청시마다 스레드를 새로 생성한다.
    }

}
