package org.example;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.ArrayList;
import java.util.List;

public class EC2ControlLambdaOff implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        // Khởi tạo client EC2
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        // Tạo filter để tìm EC2 instance theo tag
        Filter filter = new Filter("tag:Name").withValues("test-scritpt-on-off");

        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withFilters(filter);
        DescribeInstancesResult response = ec2.describeInstances(describeInstancesRequest);

        // Lấy danh sách InstanceId từ response
        List<String> instanceIds = new ArrayList<>();
        for (Reservation reservation : response.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                instanceIds.add(instance.getInstanceId());
            }
        }

        // Dừng các EC2 instance đã tìm thấy
        if (!instanceIds.isEmpty()) {
            StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(instanceIds);
            ec2.stopInstances(stopInstancesRequest);
            System.out.println("Stopped instances: " + instanceIds);
        } else {
            System.out.println("No instances found with the specified tag.");
        }
        return "success";
    }
}