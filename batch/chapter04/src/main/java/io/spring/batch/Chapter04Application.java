package io.spring.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.annotation.BeforeWrite;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class Chapter04Application {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("basicJob")
//				.start(step1())
				.start(step2())
				.build();
	}

	@Bean
	public Step step1() {
		return this.stepBuilderFactory.get("step1")
				.tasklet(chapter04Tasklet())
				.build();
	}

	// Job 실행시점에 JobParamter 삽입 가능
	@Bean
	public Tasklet chapter04Tasklet() {
		return ((stepContribution, chunkContext) -> {
			String name = (String) chunkContext.getStepContext()
					.getJobParameters()
					.get("name");

			System.out.println(String.format("Hello, %s", name));
			return RepeatStatus.FINISHED;
		});
	}


	@Bean
	public Step step2() {
		return this.stepBuilderFactory.get("step2")
				.tasklet(lateBindingChapter04Tasklet(null))
				.build();
	}

	// Late-Binding : getJobParameter 없이 컴포넌트에 주입가능
	// StepScope 설정 필요
	@StepScope
	@Bean
	public Tasklet lateBindingChapter04Tasklet(
			@Value("#{jobParameters['name']}") String name) {
		return (contribution, chunkContext) -> {
			System.out.println(String.format("Hello, %s", name));
			return RepeatStatus.FINISHED;
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(Chapter04Application.class, args);
	}

}
