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
> 실습으로 해보기