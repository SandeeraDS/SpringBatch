package com.ds.springbatchdemo.config;


import com.ds.springbatchdemo.bean.Customer;
import org.springframework.batch.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer,Customer> {
    @Override
    public Customer process(Customer customer) throws Exception {
        if(customer.getCountry().equalsIgnoreCase("Sri Lanka"))
            return customer;
        else
            return null;
    }
}
