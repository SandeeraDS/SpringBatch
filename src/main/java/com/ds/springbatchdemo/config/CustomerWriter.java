package com.ds.springbatchdemo.config;

import com.ds.springbatchdemo.bean.Customer;
import com.ds.springbatchdemo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerWriter implements ItemWriter<Customer> {
    private final Logger logger = LoggerFactory.getLogger(CustomerWriter.class);
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public void write(List<? extends Customer> list) throws Exception {
        logger.info("Thread Name : {}",Thread.currentThread().getName());
        logger.info("Saving list size of : {}",list.size());
        customerRepository.saveAll(list);
    }
}
