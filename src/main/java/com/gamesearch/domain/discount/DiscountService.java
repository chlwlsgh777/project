package com.gamesearch.domain.discount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscountService {

    @Autowired
    private DiscountRepository discountRepository; 

    public Discount findByName(String name) {
        return discountRepository.findByName(name); 
    }
}