package io.spring.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@EnableBatchProcessing
@SpringBootApplication
public class AdvancedSystemCommandJob {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("systemCommandJob")
				.start(systemCommandStep())
				.build();
	}

	@Bean
	public Step systemCommandStep() {
		return this.stepBuilderFactory.get("systemCommandStep")
				.tasklet(systemCommandTasklet())
				.build();
	}

	@Bean
	public SystemCommandTasklet systemCommandTasklet() {
		SystemCommandTasklet tasklet = new SystemCommandTasklet();

		// jar 파일로 실행해야 정상 작동(intellij 내 실행안됨)
		tasklet.setCommand("touch tmp.txt");
		tasklet.setTimeout(5000);		// 5 sec
		tasklet.setInterruptOnCancel(true);

		// Check : local directory 설정
		tasklet.setWorkingDirectory("C:\\dev\\Springboot\\batch\\testfile");

		tasklet.setSystemProcessExitCodeMapper(touchCodeMapper());
		tasklet.setTerminationCheckInterval(5000);
		tasklet.setTaskExecutor(new SimpleAsyncTaskExecutor());
		tasklet.setEnvironmentParams(new String[] {
				"JAVA_HOME=C:\\Program Files\\Java",
				"BATCH_HOME=C:\\dev\\Springboot\\batch"
		});

		return tasklet;
	}

	@Bean
	public SimpleSystemProcessExitCodeMapper touchCodeMapper() {
		return new SimpleSystemProcessExitCodeMapper();
	}

	public static void main(String[] args) {
		SpringApplication.run(AdvancedSystemCommandJob.class, args);
	}

}
