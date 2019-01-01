package com.gopi.country.partitioner;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gopinathan Munappy on 24/11/2018
 *
 * Using the topic testing01
 *
 * Using KEY_SERIALIZER : "org.apache.kafka.common.serialization.ByteArraySerializer"
 *
 * Using VALUE_SERIALIZER : "org.apache.kafka.common.serialization.StringSerializer"
 *
 * We are creating CustomPartitioner called CountryPartitioner, Let's assume that we have a retail site that
 * consumers can use to order products anywhere in the world. Based on usage, we know that most consumers are in
 * either the United States or India. We want to partition our application to send orders from the US or India to
 * their own respective consumers, while orders from anywhere else will go to a third consumer.
 *
 */

public class CountryPartitioner implements Partitioner {

    private static Map<String,Integer> countryToPartitionMap;

    // This method will gets called at the start, you should use it to do one time startup activity
    public void configure(Map<String, ?> configs) {
        System.out.println("Inside CountryPartitioner.configure " + configs);
        countryToPartitionMap = new HashMap<String, Integer>();
        for(Map.Entry<String,?> entry: configs.entrySet()){
            if(entry.getKey().startsWith("partitions.")){
                String keyName = entry.getKey();
                String value = (String)entry.getValue();
                // System.out.println( keyName.substring(11));
                int paritionId = Integer.parseInt(keyName.substring(11));
                countryToPartitionMap.put(value,paritionId);
            }
        }
    }

    //This method will get called once for each message
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes,
                         Cluster cluster) {
        List partitions = cluster.availablePartitionsForTopic(topic);
        String valueStr = (String)value;
        String countryName = ((String) value).split(":")[0];
        if(countryToPartitionMap.containsKey(countryName)){
            //If the country is mapped to particular partition return it
            return countryToPartitionMap.get(countryName);
        }else {
            //If no country is mapped to particular partition distribute between remaining partitions
            int noOfPartitions = cluster.topics().size();
            return  value.hashCode()%noOfPartitions + countryToPartitionMap.size() ;
        }
    }

    // This method will get called at the end and gives your partitioner class chance to cleanup
    public void close() {

    }
}