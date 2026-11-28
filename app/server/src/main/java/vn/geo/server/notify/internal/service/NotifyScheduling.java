package vn.geo.server.notify.internal.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Bật tác vụ định giờ cho việc dọn thông báo quá 90 ngày (BR-S3E4-27). */
@Configuration
@EnableScheduling
public class NotifyScheduling {}
