package com.github.scheduler.admin.service;

import com.github.scheduler.admin.repository.AdminScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminScheduleService {

    private final AdminScheduleRepository adminScheduleRepository;

}
