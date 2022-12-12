package com.example.fisherhelper

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder

import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val AWS_ACCESS_KEY = "AKIA5FDGPMACUOUY5Y7J"
        val AWS_SECRET_KEY = "bsqF3GDyFYSUZyAvC1BDeH/KWVYSvNZg0WKi7kWH"

        // Create an AWS credentials provider using your access and secret keys
        val credentials: AWSCredentials = BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
        val credentialsProvider: AWSCredentialsProvider = AWSStaticCredentialsProvider(credentials)

        // Create an Amazon DynamoDB client using the credentials provider
        val dynamoDbClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1)
            .withCredentials(credentialsProvider)
            .build()

        try {
            val tables = dynamoDbClient.listTables()
            println("Tables in DynamoDB:")
            tables.tableNames.forEach { println(it) }
        } catch (e: Exception) {
            println("Error: Could not connect to DynamoDB")
        }

        map_button.setOnClickListener {
          val intent = Intent(this,MapsActivity::class.java)
            startActivity(intent)


        }
    }
}