package com.example.fisherhelper
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest
import com.amazonaws.services.dynamodbv2.model.ListTablesResult
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB

class DB {


//    val AWS_ACCESS_KEY = "AKIA5FDGPMACUOUY5Y7J"
//    val AWS_SECRET_KEY = "bsqF3GDyFYSUZyAvC1BDeH/KWVYSvNZg0WKi7kWH"
//
//    // Create an AWS credentials provider using your access and secret keys
//    val credentials: AWSCredentials = BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
//    val credentialsProvider: AWSCredentialsProvider = AWSStaticCredentialsProvider(credentials)
//
//    // Create an Amazon DynamoDB client using the credentials provider
//    val dynamoDbClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1)
//        .withCredentials(credentialsProvider)
//        .build()
//
//    try {
//        val tables = dynamoDbClient.listTables()
//        println("Tables in DynamoDB:")
//        tables.tableNames.forEach { println(it) }
//    } catch (e: Exception) {
//        println("Error: Could not connect to DynamoDB")
//    }


}