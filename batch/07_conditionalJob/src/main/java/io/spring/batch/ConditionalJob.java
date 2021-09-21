package io.spring.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
public class ConditionalJob {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Tasklet passTasklet() {
		return ((stepContribution, chunkContext) -> {
			return RepeatStatus.FINISHED;
//			throw new RuntimeException("This is a failure");
		});
	}

	@Bean
	public Tasklet successTasklet() {
		return ((stepContribution, chunkContext) -> {
			System.out.println("Success!");
			return RepeatStatus.FINISHED;
		});
	}

	@Bean
	public Tasklet failTasklet() {
		return ((stepContribution, chunkContext) -> {
			System.out.println("Failure!");
			return RepeatStatus.FINISHED;
		});
	}

	@Bean
	public Job listenerJob() {
		return this.jobBuilderFactory.get("listenerJob")
				.start(firstStep())
				.on("FAILED").to(failureStep())
				.from(firstStep()).on("*").to(successStep())
				.end()
				.build();
	}

	@Bean
	public Step firstStep() {
		return this.stepBuilderFactory.get("firstStep")
				.tasklet(passTasklet())
				.build();
	}

	@Bean
	public Step successStep() {
		return this.stepBuilderFactory.get("successStep")
				.tasklet(successTasklet())
				.build();
	}

	@Bean
	public Step failureStep() {
		return this.stepBuilderFactory.get("failureStep")
				.tasklet(failTasklet())
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ConditionalJob.class, args);
	}

}
