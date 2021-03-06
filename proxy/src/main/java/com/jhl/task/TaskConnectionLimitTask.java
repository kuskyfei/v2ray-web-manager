package com.jhl.task;

import com.jhl.constant.ManagerConstant;
import com.jhl.pojo.ConnectionLimit;
import com.jhl.task.inteface.AbstractTask;
import com.ljh.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class TaskConnectionLimitTask extends AbstractTask {
    private ConnectionLimit connectionLimit;


    public TaskConnectionLimitTask(ConnectionLimit connectionLimit) {
        this.connectionLimit = connectionLimit;
    }

    @Override
    public void beforeRun() {
    }

    @Override
    public void runTask(RestTemplate restTemplate, ManagerConstant managerConstant) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Result> resultEt = restTemplate.getForEntity(managerConstant.getReportOverConnectionLimitUrl(),
                Result.class, connectionLimit.getAccountNo());

        if (resultEt == null) {
            return;
        }
        if (!resultEt.getStatusCode().is2xxSuccessful()) {
            log.warn("上报失败:{}", resultEt);
            tryAgain(getTaskCondition());
        } else {
            Result resultEtBody = resultEt.getBody();

            if (resultEtBody.getCode() != 200) { //系统内部错误
                log.error("上报失败：{},{},error:{}", connectionLimit, resultEtBody);
                tryAgain(getTaskCondition());

            } else {
                log.info("上报成功");
            }
        }
    }

    @Override
    public void done() {

    }

    @Override
    public void catchException(Exception e) {
        tryAgain(getTaskCondition());
    }

    @Override
    public void setCondition(TaskCondition taskCondition) {
        taskCondition.setFailureTimes(0);
        taskCondition.setMaxFailureTimes(20);
        taskCondition.setInterval(6000);
    }


}
