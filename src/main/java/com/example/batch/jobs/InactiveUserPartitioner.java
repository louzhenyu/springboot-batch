package com.example.batch.jobs;

import com.example.batch.domain.enums.Grade;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gavinkim at 2018-12-09
 * 휴면회원으로 전환할 회원을 각 등급에 따라 병렬로 처리하기 위해 파티셔닝을 구현 한다.
 * 파티셔닝은 마스터 슬레이브로 나눈후 모든 슬레이브의 작업이 완료되면 결과가 합쳐져서 마스터가 완료되고, Step 이 마무리 된다.
 */
public class InactiveUserPartitioner implements Partitioner {

    private static final String GRADE = "grade";
    private static final String INACTIVE_USER_TASK = "InactiveUserTask";

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> map = new HashMap<>(gridSize); // gridSize 만큼 맵크기 할당
        Grade[] grades = Grade.values();
        for (int i = 0, length = grades.length; i < length; i++) { // grades 값만큼 파티션을 생성하기 위해 루프 실행
            ExecutionContext context = new ExecutionContext();
            context.putString(GRADE, grades[i].name()); // step 에서 파라미터로  Grade 값을 받아 사용. 이때 ExecutionContext 의 키값은 grade 이다. Grade Enum 의 이름값을 context 에 추가.
            map.put(INACTIVE_USER_TASK + i, context); // 반환되는 map 에 INACTIVE_USER_TASK1.2.3.... 형식의 파티션 키값을 지정하고, 위에서 추가한 ExecutionContext 를 map 에 추가
        }
        return map;
    }
}
