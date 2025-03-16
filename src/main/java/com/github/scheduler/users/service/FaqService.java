package com.github.scheduler.users.service;

import com.github.scheduler.users.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

}
