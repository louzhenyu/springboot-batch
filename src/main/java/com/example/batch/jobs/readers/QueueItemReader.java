package com.example.batch.jobs.readers;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by gavinkim at 2018-12-09
 * ItemReader 를 직접 구현함.
 * 큐를 사용해 저장하도록 한다.
 *
 */
public class QueueItemReader<T> implements ItemReader {

    private Queue<T> queue;

    /**
     * 휴면회원으로 지정될 데이터를 한번에 불러와 큐에 담는다.
     * @param data
     */
    public QueueItemReader(List<T> data){
        this.queue = new LinkedList<>(data);
    }

    /**
     * 큐의 poll 을 사용하여 큐에서 데이터를 하나씩 반환한다.
     * @return
     * @throws Exception
     * @throws UnexpectedInputException
     * @throws ParseException
     * @throws NonTransientResourceException
     */
    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return this.queue.poll();
    }
}
