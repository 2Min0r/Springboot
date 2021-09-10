package io.spring.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})		// 아직 DB 연결을 하지 않아 exclude
public class HelloWorldApplication {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Step step() {
		return this.stepBuilderFactory.get("step1")
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
						System.out.println("Hello, World");
						return RepeatStatus.FINISHED;		// 태스크릿 완료(멈춤)
					}
				}).build();
	}

	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("job")
				.start(step())
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(HelloWorldApplication.class, args);
	}

}
