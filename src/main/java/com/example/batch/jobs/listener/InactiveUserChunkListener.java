package com.example.batch.jobs.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;
import org.springframework.batch.core.ChunkListener;

/**
 * Created by gavinkim at 2018-12-09
 */
@Slf4j
@Component
public class InactiveUserChunkListener implements ChunkListener {

    @Override
    public void beforeChunk(ChunkContext chunkContext) {
        log.info("Before Chunk");
    }

    @Override
    public void afterChunk(ChunkContext chunkContext) {
        log.info("after Chunk");
    }

    @Override
    public void afterChunkError(ChunkContext chunkContext) {
        log.info("Afterchunk Error");
    }
}
