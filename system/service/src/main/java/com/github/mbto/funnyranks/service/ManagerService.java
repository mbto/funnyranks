package com.github.mbto.funnyranks.service;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.dao.ManagerDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
@Slf4j
public class ManagerService implements UserDetailsService {
    @Autowired
    private ManagerDao managerDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        FunnyRanksManager funnyRanksManager = managerDao.fetchManager(username);
        if (funnyRanksManager == null)
            throw new UsernameNotFoundException("Username '" + username + "' not founded");
        log.info("Try authentication - " + funnyRanksManager);
        return funnyRanksManager;
    }
}