package com.ds.springbatchdemo.config;

import com.ds.springbatchdemo.bean.Customer;
import com.ds.springbatchdemo.partition.ColumnRangePartitioner;
import com.ds.springbatchdemo.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Repository;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    // factory for job
    private JobBuilderFactory jobBuilderFactory;
    // factory for step
    private StepBuilderFactory stepBuilderFactory;

    private CustomerWriter customerWriter;


    // Item Reader
    @Bean
    public FlatFileItemReader<Customer> reader(){
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1); //  skip header
        itemReader.setLineMapper(lineMapper());

        return itemReader;
    }

    private LineMapper<Customer> lineMapper() {
        // set how to read the csv file and how to map the customer object
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","firstName","lastName","email","gender","contactNo","country","dob"); // headers

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        // combine both line tokenizer and field mapper
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;

    }

    @Bean
    public CustomerProcessor customerProcessor(){
        return new CustomerProcessor();
    }

//    @Bean
//    public RepositoryItemWriter<Customer> writer(){
//        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
//        writer.setRepository(customerRepository);
//        writer.setMethodName("save");
//        return writer;
//    }

//    @Bean
//    public Step step1(){
//        return stepBuilderFactory.get("csv-step").<Customer,Customer>chunk(10)
//                .reader(reader())
//                .processor(customerProcessor())
//                .writer(writer())
//                .taskExecutor(taskExecutor())
//                .build();
//    }

    @Bean
    public Step slaveStep(){
        return stepBuilderFactory.get("slaveStep").<Customer,Customer>chunk(500)
                .reader(reader())
                .processor(customerProcessor())
                .writer(customerWriter)
                .build();
    }

    @Bean
    public Step masterStep(){
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep().getName(),columnRangePartitioner())
                .partitionHandler(partitionHandler())
                .build();
    }

//    @Bean
//    public Job runJob() {
//        return jobBuilderFactory.get("importCustomerInfo").flow(step1()).end().build();
//    }

    @Bean
    public Job runJob() {
        return jobBuilderFactory.get("importCustomerInfo").flow(masterStep()).end().build();
    }

//    @Bean
//    public TaskExecutor taskExecutor(){
//        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
//        simpleAsyncTaskExecutor.setConcurrencyLimit(10); // 10 tread execute concurrently
//        return simpleAsyncTaskExecutor;
//    }


    @Bean
    public TaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setQueueCapacity(10);
        return taskExecutor;
    }

    @Bean
    public ColumnRangePartitioner columnRangePartitioner(){
        return new ColumnRangePartitioner();
    }

    @Bean
    public PartitionHandler partitionHandler(){
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(2);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slaveStep());
        return taskExecutorPartitionHandler;
    }
}
