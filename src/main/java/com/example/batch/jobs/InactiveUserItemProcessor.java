package com.example.batch.jobs;

import com.example.batch.domain.User;
import org.springframework.batch.item.ItemProcessor;

/**
 * Created by gavinkim at 2018-12-09
 */
public class InactiveUserItemProcessor implements ItemProcessor<User,User> {

    @Override
    public User process(User user) {
        return user.setInactive();
    }
}
