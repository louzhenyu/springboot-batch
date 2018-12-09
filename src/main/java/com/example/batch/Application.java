package com.example.batch;

import com.example.batch.domain.User;
import com.example.batch.domain.enums.Grade;
import com.example.batch.domain.enums.UserStatus;
import com.example.batch.repository.UserRepository;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@EnableBatchProcessing
@SpringBootApplication
public class Application {

    private static final long MIN_DAY = LocalDate.of(2015,12,9).toEpochDay();
    private static final long MAX_DAY = LocalDate.of(2018,12,9).toEpochDay();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    //테스트 데이터를 위해 더미 데이터를 생성한다.
    @Bean
    public CommandLineRunner runner(UserRepository userRepository){
        return (args -> {
            List<User> users = new ArrayList<>();

            IntStream.rangeClosed(1,100).forEach(index->users.add(User.builder()
            .name(String.format("user%s",index))
            .grade(index%2==0 ? Grade.FAMILY : index%3==0 ? Grade.GOLD : Grade.VIP)
            .status(UserStatus.ACTIVE)
            .email(String.format("test@email.com"))
            .createdDate(LocalDateTime.of(2015,12,9,0,0))
            .updatedDate(makeRandomDateTime())
            .build()));
            userRepository.saveAll(users);
        });
    }

    private LocalDateTime makeRandomDateTime() {
        long randomDay = ThreadLocalRandom.current().nextLong(MIN_DAY, MAX_DAY);
        return LocalDateTime.of(LocalDate.ofEpochDay(randomDay),LocalTime.MIN);
    }
}
