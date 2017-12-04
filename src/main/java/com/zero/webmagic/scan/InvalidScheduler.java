package com.zero.webmagic.scan;

import com.zero.webmagic.dao.IpRepository;
import com.zero.webmagic.entity.Ip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * description
 * <p>
 * 2017-12-04 13:07
 *
 * @author scvzerng
 **/
@Component
@Slf4j
public class InvalidScheduler {
    @Resource
    IpRepository ipRepository;

    /**
     * 每分钟一次定时任务
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void testIpValid(){
        Pageable pageable = PageRequest.of(1,100);
        pageable.getSort().and(Sort.by(Sort.Order.asc("updateTime")));
        Page<Ip> page = ipRepository.findAll(pageable);
        if(page.hasContent()){
           List<Ip> validIps =  page.getContent();
            validIps.stream().parallel().forEach(ip->{
                ip.setUpdateTime(LocalDateTime.now());

                try {
                    InetAddress address = InetAddress.getByName(ip.getIp());
                    Socket socket = new Socket(address,ip.getPort());
                    socket.setSoTimeout(200);
                    socket.close();
                    ip.setCanUse(true);
                    ipRepository.save(ip);
                    log.info("is valid {}:{}",ip.getIp(),ip.getPort());
                } catch (IOException e) {
                    ipRepository.save(ip);
                   log.info("is invalid {}:{}",ip.getIp(),ip.getPort());
                }
            });
        }
    }
}