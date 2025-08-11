package com.example.demo.mongo.mail;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MailLogRepository extends MongoRepository<MailLog, String> {}
