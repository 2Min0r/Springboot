# 스텝
## 처리 모델
1. 태스크릿 모델
   - Tasklet 인터페이스 이용
   - `RepeatStatus.FINISHED` 반환할 때까지 반복
2. 청크 기반 처리 모델
   - `ItemReader`, `ItemProcessor`(필수아님), `ItemWriter`로 구성
   - `ItemReader`: 청크단위로 처리할 모든 레코드 반복적으로 읽어옴
   - `ItemProcessor`: 반복적 데이터 처리
   - `ItemWriter`: 한 번에 전달하여 기록

## 스텝 유형
### 1. 태스크릿 스텝
1. `MethodInvokingTaskletAdapter` 사용하여 사용자 태스크릿 스텝 정의
2. `taklet` 인터페이스 구현하여 RepeatStatus 객체 반환하도록 정의
   - `RepeatStatus.CONTINUABLE`: 조건 충족시까지 반복
   - `RepeatStatus.FINISHED`: 성공여부와 관계없이 완료 처리

#### 1) CallableTaskletAdapter
1. 특정 로직을 해당 스텝이 실행되는 스레드가 아닌, 다른 스레드에서 실행하고 싶을 때 사용
2. `Callable<V>` 인터페이스의 구현체 구성
   - `java.lang.Runnable` 인터페이스와 유사하지만, 값 반환, 예외 던지기 가능
3. 태스크릿과 스텝이 별개의 스레드에서 실행되지만, 병렬로 수행되는 것은 아님
4. `Callable` 객체의 `call()` 메서드를 호출하고, `call()` 메서드가 반환하는 값 반환하여 구현
```java
@Bean
public Callable<RepeatStatus> callableObject() {
    return () -> {
        System.out.println("This was exceuted in another thread");
        return RepeatStatus.FINISHED;
    };
}

@Bean
public CallableTaskletAdapter tasklet() {
    CallableTaskletAdapter callableTaskletAdpater =
        new CallableTaskletAdapter();
    
    callableTaskletAdapter.setCallable(callableObject());
    
    return callableTaskletAdapter;
```

#### 2) MethodInvokingTaskletAdapter
1. 배치 잡에서 한 번만 실행하고 싶은 로직을 호출할 때 사용
2. 별도의 `ExitStatus` 타입을 반환하지 않는 한 `ExitStatus.COMPLETED` 반환
3. 파라미터 전달 필요시 '늦은 바인딩' 방법 활용
   - 스텝 tasklet 전달시 `null` 전달
   - tasklet에서 `@Value("#{jobParamters['message']}") String message`로 jobParameter 설정
   - service(String message)로 전달하여 수신, 출력 등 가능
```java
@Bean
public MethodInvokingTaskletAdapter methodInvokingTasklet() {
    MethodInvokingTaskletAdapter methodInvokingTaskletAdapter =
        new MethodInvokingTaskletAdapter();
    
    methodInvokingTaskletAdapter.setTargetObject(service());
    methodInvokingTaskletAdapter.setTargetMethod("serviceMethod");
    
    return methodInvokingTaskletAdapter;
    
@Bean
public CustomService service() {
    return new CCustomService();
}
```
```java
// CustomService
public class CustomService {
    public void serviceMethod() {
        System.out.println("Service method was called");
    }
}
```

#### 3) SystemCommandTasklet
1. 시스템 명령을 실행할 때 사용
2. 시스템 명령은 **비동기**로 실행 -> 타임아웃 값 중요(ms)
> Commit: 008. Advanced System Command Job

### 2. 청크 기반 스텝
#### 1) 청크
   - 커밋 간격에 의해 정의됨
   - 중요한 이유
     1. 커밋 간격을 바탕으로 아이템을 처리하고 씀
     2. 청크마다 잡의 상태가 JobRepository에 갱신됨
     3. 어느정도 크게 설정하는 것이 성능에 좋음
```java
@Bean
public Step step1() {
    return this.stepBuilderFactory.get("step1")
        .<String, String>chunk(10)
        .reader(itemReader(null))
        .writer(itemWriter(null))
        .build();
        }
```
#### 2) 청크 크기
   1. 하드 코딩하여 청크 크기 설정
   2. `org.springframework.batch.repeat.CompletionPolicy`로 청크 완료 시점 정의(유동적)
      1. `SimpleCopletionPolicy`: 처리된 아이템 개수가 임곗값에 도달하면 청크 완료
      2. `TimeoutTermiantionPolicy`: 처리 시간이 해당 시간이 넘어갈 때 완료된 것으로 간주하고 모든 트랜잭션 처리
      3. `CompositeCompletiontPolicy`: 청크 완료 여부를 여러 정책 함께 구성가능
      4. [org.springframework.batch.repeat.policy](https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/repeat/policy/package-summary.html)
   ```java
@Bean
public Step chunkStep() {
    return this.stepBuilderFactory.get("chunkStep")
        .<String, String>chunk(completionPolicy())
        .reader(itemReader())
        .writer(itemWriter())
        .build();
        }

@Bean
public CompletionPolicy completionPolicy() {
    CompositeCompletionPolicy policy = new CompositeCompletionPolicy();
    policy.setPolicies(new CompletionPolicy[] {
            new TimeoutTerminationPolicy(3),
            new SimpleCompletionPolicy(1000)
        });
    return policy;
        }
   ```
> TODO : 실습으로 해보기

#### 3) CompletionPolicy
1. `start` 메서드: 청크 시작시 해당 구현체가 필요로 하는 모든 내부 상태 초기화
2. `update` 메서드: 각 아이템이 처리되면 한 번 씩 호출되면서 내부 상태 갱신
3. `isComplete` 메서드
   1. `RepeatContext`를 파라미터로 전달 받아 내부 상태를 이용해 청크 완료 판단
   2. `RepeatContext` 및 `RepeatStatus`를 파라미터로 전달 받아 청크 완료 여부의 상태를 기반으로 판단

> TODO : 실습으로 해보기(랜덤 청크 크기 결정)