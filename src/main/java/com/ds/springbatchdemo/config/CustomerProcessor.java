package com.ds.springbatchdemo.config;


import com.ds.springbatchdemo.bean.Customer;
import com.ds.springbatchdemo.controller.JobController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer,Customer> {
    private final Logger logger = LoggerFactory.getLogger(CustomerProcessor.class);
    @Override
    public Customer process(Customer customer) throws Exception {
        logger.info("processing customer of customer id :{} customer :{}",customer.getId(),customer);
//        if(customer.getCountry().equalsIgnoreCase("Sri Lanka"))
//            return customer;
//        else
//            return null;
        return customer;
    }
}
