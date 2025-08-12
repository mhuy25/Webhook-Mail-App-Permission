package com.example.demo.mongo.dlt;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DLTLogRepository extends MongoRepository<DLTLog, String> {}
