package com.example.fisherhelper
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.GetItemRequest
import kotlin.system.exitProcess

class DB {
//    tableName = fishery
    suspend fun getSpecificItem(tableNameVal: String, keyName: String, keyVal: String) {

        val keyToGet = mutableMapOf<String, AttributeValue>()
        keyToGet[keyName] = AttributeValue.S(keyVal)

        val request = GetItemRequest {
            key = keyToGet
            tableName = tableNameVal
        }

        DynamoDbClient { region = "us-east-1" }.use { ddb ->
            val returnedItem = ddb.getItem(request)
            val numbersMap = returnedItem.item
            numbersMap?.forEach { key1 ->
                println(key1.key)
                println(key1.value)
            }
        }
    }
}