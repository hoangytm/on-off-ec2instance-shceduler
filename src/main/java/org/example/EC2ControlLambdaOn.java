package org.example;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.ArrayList;
import java.util.List;

public class EC2ControlLambdaOn implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        String tagKey = "Name"; // Thay bằng tên tag của bạn
        String tagValue = "test-scritpt-on-off";       // Thay bằng giá trị tag của bạn

        try {
            // 1. Tìm EC2 instances có tag phù hợp
            DescribeInstancesRequest describeRequest = new DescribeInstancesRequest();
            List<Reservation> reservations = ec2.describeInstances(describeRequest).getReservations();

            List<String> instanceIdsToStart = getEc2Instance(reservations, tagKey, tagValue);

            if (instanceIdsToStart.isEmpty()) {
                context.getLogger().log("No instances found with tag " + tagKey + "=" + tagValue);
                return "No instances to start.";
            }

            // 2. Start các EC2 instances đã lọc
            StartInstancesRequest startRequest = new StartInstancesRequest()
                    .withInstanceIds(instanceIdsToStart);
            ec2.startInstances(startRequest);

            context.getLogger().log("Started instances: " + instanceIdsToStart);
            return "Started instances: " + instanceIdsToStart;

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private static List<String> getEc2Instance(List<Reservation> reservations, String tagKey, String tagValue) {
        List<String> instanceIdsToStart = new ArrayList<>();
        for (Reservation reservation : reservations) {
            for (Instance instance : reservation.getInstances()) {
                if (instance.getState().getName().equals("stopped")) { // Chỉ lọc instance đã dừng
                    for (Tag tag : instance.getTags()) {
                        if (tagKey.equals(tag.getKey()) && tagValue.equals(tag.getValue())) {
                            instanceIdsToStart.add(instance.getInstanceId());
                        }
                    }
                }
            }
        }
        return instanceIdsToStart;
    }
}
