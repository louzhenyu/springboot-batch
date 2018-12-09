package com.example.batch.jobs;

import com.example.batch.domain.User;
import com.example.batch.domain.enums.UserStatus;
import com.example.batch.jobs.listener.InactiveUserChunkListener;
import com.example.batch.jobs.listener.InactiveUserJobListener;
import com.example.batch.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

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

    private final static int CHUNK_SIZE = 5;
    private final EntityManagerFactory entityManagerFactory;

    private UserRepository userRepository;

    /**
     * Job 생성을 위한 JobBuilderFactory 주입.
     * - 빈에 주입할 객체를 param 으로 명시할 경우 @Autowired 를 사용한것과 같다.
     */
    @Bean
    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory,
                               InactiveUserJobListener inactiveUserJobListener,//리스너 주입
                               Flow multiFlow
                               ){
        return jobBuilderFactory.get("inactiveUserJob") // inactiveUserJob 이라는 이름의 JobBuilder 생성
                .preventRestart() // Job 의 재실행 방지
                .listener(inactiveUserJobListener) // listener 추가.
                .start(multiFlow)//inactiveUserJob 시작시 Flow 를 거쳐 Step 을 실행 하도록  inactiveJobFlow 설정 한다.
                .end()
                .build();
    }

    @Bean
    public Flow multiFlow(Step inactiveJobStep){
        Flow flows[] = new Flow[CHUNK_SIZE];
        IntStream.range(0,flows.length).forEach(i->flows[i] =
                new FlowBuilder<Flow>("MultiFlow"+i).from(inactiveJobFlow(inactiveJobStep)).end());
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("MultiFlow");
        return flowBuilder
                .split(taskExecutor()) //multiFlow 에서 사용할 TaskExecutor 를 등록.
                .add(flows) // inactiveJobFlow CHUNK_SIZE 만큼 할당된 flows 배열 추가.
                .build();
    }
//    @Bean //빈으로 생성시 기본적으로 싱글톤이기 때문에 multiFlow 를 위해서는 빈으로 등록하지 말아야함.
    public Flow inactiveJobFlow(Step inactiveJobStep){
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("inactiveJobFlow");
        return flowBuilder
                //생성한 조건을 처리하는 InactiveJobExecutionDecider 클래스를 start 로 설정하여 맨 처음으로 시작 하도록 지정.
                .start(new InactiveJobExecutionDecider())
                //InactiveJobExecutionDecider 클래스의 decide 메서드를 거쳐 반환값으로 FlowExecutionStatus.FAILED 가 반환되면 end 를 사용해 끝내도록 설정
                .on(FlowExecutionStatus.FAILED.getName()).end()
                //InactiveJobExecutionDecider 클래스의 decide 메서드를 거쳐 반환값으로 FlowExecutionStatus.COMPLETED 가 반환되면 기존에 설정한 inactiveJobStep 을 실행하도록 한다.
                .on(FlowExecutionStatus.COMPLETED.getName()).to(inactiveJobStep)
                .end();
    }

    /**
     * Step 을 위한 StepBuilderFactory 주입.
     * JpaPagingItemReader 를 사용하여 데이터를 불러올때 페이징 처리하여 불러오도록 한다.
     * @param stepBuilderFactory
     * @return
     */
    @Bean
    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory,
                                ListItemReader<User> inactiveUerJpaReader,
                                InactiveUserChunkListener inactiveUserChunkListener, //chunk 단위로 실행 하기 때문에 chunklistener 를 사용하도록 한다.
                                TaskExecutor taskExecutor
    ){
        return stepBuilderFactory.get("inactiveUserStep")//inactiveUserStep 이라는 이름의 StepBuilder 생성
                //Generic 을 사용하여 chunk() 의 입력타입과 출력타입을 User 타입으로 설정.
                //chunk 단위로 묶어서 writer()를 실행시킬 단위를 지정한것이다.
                //commit 단위가 10개로 정한것이다.
                .<User, User> chunk(CHUNK_SIZE)
                .reader(inactiveUerJpaReader) //reader (회원 정보 가져오기)
                .processor(inactiveUserProcessor())//processor (로직 수행)
                .writer(inactiveUserWriter())//wirter (회원정보 저장)
                .listener(inactiveUserChunkListener) // step 실행전후 로직 실행을 위해 추가.
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor(){
        return new SimpleAsyncTaskExecutor("Batch_Task"); //SimpleAsyncTaskExecutor 는 스레드 요청시마다 스레드를 새로 생성한다.
    }

    /**
     * 배치가 실행시 처리할 로직을 담당하는 프로세서
     * @return
     */
    public ItemProcessor<? super User,? extends User> inactiveUserProcessor() {
        return User::setInactive;
    }

    /**
     * 기본 빈 생성은 싱글톤이다. StepScope 를 사용할 경우 해당 메서드는 Step 의 주기에 따라 새로운 빈을 생성한다.
     * step 의 실행마다 새로 빈을 만들기 때문에 지연 생성이 가능함.
     * 주의 사항: 기본 프록시 모드가 반환되는 클래스 타입을 참조하기 때문에 StepScope 를 사용할 경우 반드시 구현된 반환타입을 명시해 반환해야 한다.
     *
     * Spel 을 사용하여 jobparameters 에서 nowDate 파라미터를 전달받는다.
     * @return
     */
    // destroyMethod 를 사용해서 삭제할 빈을 자동으로 추적, "" 일 경우 warring 메시지 제거.
    @Bean(destroyMethod = "")
    @StepScope
    public ListItemReader<User> inactiveUserReader(@Value("#{jobParameters[nowDate]}") Date nowDate, UserRepository userRepository) {
        //Date 타입을 LocalDateTime 으로 전환한다.
        LocalDateTime now = LocalDateTime.ofInstant(nowDate.toInstant(), ZoneId.systemDefault());
        List<User> inactiveUsers = userRepository.findByUpdatedDateBeforeAndStatusEquals(now.minusYears(1),UserStatus.ACTIVE);
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
     * 저장 설정 필요 없이 generic 에 저장할 타입을 명시하고, entityManagerFactory 만 설정 하면 processor 에서 넘어온 데이터를
     * 청크 단위로 저장한다.
     * @return
     */
//    public ItemWriter<User> inactiveUserWriter() {
//        JpaItemWriter<User> jpaItemWriter = new JpaItemWriter<>();
//        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
//        return jpaItemWriter;
//    }

    public ItemWriter<User> inactiveUserWriter() {
        return ((List<? extends User> users) -> userRepository.saveAll(users));
    }

}
