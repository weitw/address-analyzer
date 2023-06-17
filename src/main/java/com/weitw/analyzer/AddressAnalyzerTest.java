package com.weitw.analyzer;

import java.util.ArrayList;
import java.util.List;

public class AddressAnalyzerTest {
    public static void main(String[] args) {
//        for (int i = 0; i < 5; i++) {
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    AddressAnalyzer addressAnalyzer = AddressAnalyzer.analyzer;
//                    List<String> addressList = new ArrayList<>();
//                    addressList.add("贵州省七星关区何官屯镇刘家村11号");
//                    addressList.add("七星关区何官屯镇刘家村11号");
//                    addressList.add("福建省长汀县汀洲镇中心坝竹区8号");
//                    addressList.add("福建长汀汀洲镇中心坝竹区8号");
//                    addressList.add("毕节市何官屯镇刘家村11号");
//                    addressList.add("内蒙锡林郭勒盟二连浩特市肯特街南、建设路东");
//                    addressList.add("河北大城县北位乡魏胡村12街5巷437号");
//                    addressList.add("镇江市句容市崇明西路与玉清路交叉路口西侧(玉清小区)");
//                    addressList.add("北京朝阳方恒国际中心A座2601号");
//                    for (String address : addressList) {
//                        Address addressVO = addressAnalyzer.addressResolution(address);
//                        System.out.println("当前线程：" + Thread.currentThread().getName() + ",  address:" + address + ",  result:" + addressVO);
//                    }
//                }
//            });
//            thread.setName("线程" + i);
//            thread.start();
//        }
        AddressAnalyzer addressAnalyzer = AddressAnalyzer.analyzer;
        List<String> addressList = new ArrayList<>();
        addressList.add("贵州省七星关区何官屯镇刘家村2312号");
        addressList.add("七星关区何官屯镇刘家村2312号");
        addressList.add("福建省长汀县汀洲镇中心坝竹区8号");
        addressList.add("福建长汀汀洲镇中心坝竹区8号");
        addressList.add("毕节市何官屯镇刘家村11号");
        addressList.add("内蒙古自治区锡林郭勒盟二连浩特市肯特街南、建设路东");
        addressList.add("锡林郭勒盟二连浩特市肯特街南、建设路东");
        addressList.add("内蒙锡林郭勒盟二连浩特市肯特街南、建设路东");
        addressList.add("河北省廊坊市大城县xxxxxxxxxx");
        addressList.add("镇江市句容市崇明西路与玉清路交叉路口西侧(玉清小区)");
        addressList.add("北京朝阳方恒国际中心A座2601号");
        for (String address : addressList) {
            Address addressVO = AddressAnalyzer.analyzer.addressResolution(address);
            System.out.print("address:" + address);
            System.out.println(",   " + addressVO);
        }
    }

}
