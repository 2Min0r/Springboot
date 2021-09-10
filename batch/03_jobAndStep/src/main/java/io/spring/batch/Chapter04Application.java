package io.spring.batch;

import io.spring.batch.com.DailyJobTimestamper;
import io.spring.batch.com.JobLoggerListener;
import io.spring.batch.com.ParameterValidator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.annotation.BeforeWrite;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@EnableBatchProcessing
@SpringBootApplication
public class Chapter04Application {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public CompositeJobParametersValidator validator() {
		CompositeJobParametersValidator validator = new CompositeJobParametersValidator();

		DefaultJobParametersValidator defaultJobParametersValidator =
				new DefaultJobParametersValidator(
								new String[] {"fileName"},
								new String[] {"name", "run.id", "currentDate"});

		defaultJobParametersValidator.afterPropertiesSet();

		validator.setValidators(
				Arrays.asList(new ParameterValidator(), defaultJobParametersValidator));

		return validator;
	}

	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("basicJob")
//				.start(step1())
				.start(step2())
				.validator(validator())
				.incrementer(new RunIdIncrementer())
				.incrementer(new DailyJobTimestamper())
				.listener(JobListenerFactoryBean.getListener(
							new JobLoggerListener()))
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
				.tasklet(lateBindingChapter04Tasklet(null, null))
				.build();
	}

	// Late-Binding : getJobParameter 없이 컴포넌트에 주입가능
	// StepScope 설정 필요
	@StepScope
	@Bean
	public Tasklet lateBindingChapter04Tasklet(
			@Value("#{jobParameters['name']}") String name,
			@Value("#{jobParameters['fileName']}") String fileName) {

		return (contribution, chunkContext) -> {

			System.out.println(String.format("Hello, %s", name));
			System.out.println(String.format("fileName = %s", fileName));

			return RepeatStatus.FINISHED;
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(Chapter04Application.class, args);
	}

}
