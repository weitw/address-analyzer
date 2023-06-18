package com.weitw.analyzer;

import cn.hutool.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地址解析
 *
 * @author weitw
 * @date 2023/06/16
 */

public class AddressAnalyzer {

    private static Map<String, Map<String, List<String>>> provinceCityCounty = null;
    private static Map<String, List<String>> cityCounty = null;
    // 所有省份，包括模糊的
    private static List<String> provinces = null;
    // 所有市，包括模糊的
    private static List<String> citys = null;
    // 所有区，包括模糊的
    private static List<String> countys = null;

    public final static AddressAnalyzer analyzer = new AddressAnalyzer();

    private AddressAnalyzer() {
        System.out.println("正在加载数据>>>>>>>>>>");
        builder();
        System.out.println("数据加载完毕>>>>>>>>>>");
    }

    /**
     * 构建器
     *
     */
    private void builder() {
        final JSONObject jsonObject;
        try {
            String path = this.getClass().getClassLoader().getResource("").getPath();
            File file = new File(path + "/address.json");
            String content= FileUtils.readFileToString(file,"UTF-8");
            jsonObject = new JSONObject(content);
        } catch (IOException ioException) {
            System.out.println("文件读取异常>>>>>>>>>>" + ioException);
            return;
        } catch (Exception e) {
            System.out.println("加载异常>>>>>>>>>>" + e.getMessage());
            return;
        }

        provinceCityCounty = analyzerProvinceCityCounty(jsonObject);
        cityCounty = analyzerCityCounty(jsonObject);
        Map<String, List<String>> fuzzyProvinceCityCounty = analyzerFuzzyProvinceCityCounty(jsonObject);

        // >>>>>>>>>>>>>>build
        provinces = fuzzyProvinceCityCounty.get("province");
        citys = fuzzyProvinceCityCounty.get("city");
        countys = fuzzyProvinceCityCounty.get("county");
    }

    /**
     * 省市县
     *
     * @param sourceMap json文件源数据
     * @return {@link Map}<{@link String}, {@link Map}<{@link String}, {@link List}<{@link String}>>>
     */
    private Map<String, Map<String, List<String>>> analyzerProvinceCityCounty(Map<String, Object> sourceMap) {
        Map<String, Map<String, List<String>>> provinceCityCounty = new HashMap<>();
        Map<String, String> provinceMap = (Map<String, String>) sourceMap.get("100000");
        provinceMap.forEach((pcode, pname) -> {
            // 省
            provinceCityCounty.putIfAbsent(pname, new HashMap<>());
            Map<String, List<String>> cityCounty = provinceCityCounty.get(pname);
            if (sourceMap.containsKey(pcode)) {
                // 市
                Map<String, String> cityMap = (Map<String, String>) sourceMap.get(pcode);
                cityMap.forEach((ccode, cname) -> {
                    if (sourceMap.containsKey(ccode)) {
                        // 区
                        Map<String, String> countyMap = (Map<String, String>) sourceMap.get(ccode);
                        List<String> countyNames = new ArrayList<>();
                        countyMap.forEach((countyCode, countyName) -> {
                            countyNames.add(countyName);
                        });
                        cityCounty.put(cname, countyNames);
                    }
                });
                provinceCityCounty.putIfAbsent(pname, cityCounty);
            }
        });
        return provinceCityCounty;
    }

    /**
     * 解析出所有省市区常用叫法的列表。用于匹配用户输入的多种场景
     * 例如：内蒙古自治区，常用的叫法有：内蒙古自治区、内蒙古、内蒙
     *
     * @param sourceMap json文件源数据
     * @return {@link Map}<{@link String}, {@link List}<{@link String}>>
     */
    private Map<String, List<String>> analyzerFuzzyProvinceCityCounty(Map<String, Object> sourceMap) {
        JSONObject jsonObject = null;
        try {
            String path = this.getClass().getClassLoader().getResource("").getPath();
            File file = new File(path + "/vernacularProvinceCityCounty.json");
            String content= FileUtils.readFileToString(file,"UTF-8");
            jsonObject = new JSONObject(content);
        } catch (IOException ioException) {
            System.out.println("省市区白话配置文件读取异常>>>>>>>>>>" + ioException);
        }
        Map<String, List<String>> vernacularProvinceConfig = jsonObject != null ?
                (Map<String, List<String>>) jsonObject.getOrDefault("province", new HashMap<>()) : new HashMap<>();
        Map<String, List<String>> vernacularCityConfig = jsonObject != null ?
                (Map<String, List<String>>) jsonObject.getOrDefault("city", new HashMap<>()) : new HashMap<>();
        Map<String, List<String>> vernacularCountyConfig = jsonObject != null ?
                (Map<String, List<String>>) jsonObject.getOrDefault("county", new HashMap<>()) : new HashMap<>();

        List<String> provinces = new ArrayList<>(80);
        List<String> citys = new ArrayList<>(700);
        List<String> countys = new ArrayList<>(5900);
        Map<String, String> provinceMap = (Map<String, String>) sourceMap.get("100000");
        provinceMap.forEach((pcode, pname) -> {
            // 省
            boolean flag = true;
            provinces.add(pname);
            if (flag && pname.endsWith("省") || pname.endsWith("市")) {
                provinces.add(pname.substring(0, pname.length() - 1));
                flag = false;
            }
            if (flag && pname.endsWith("特别行政区")) {
                provinces.add(pname.substring(0, pname.length() - 5));
                flag = false;
            }
            // 配置中的特殊省份白话
            if (flag && vernacularProvinceConfig.containsKey(pname)) {
                provinces.addAll(vernacularProvinceConfig.get(pname));
                flag = false;
            }
            if (sourceMap.containsKey(pcode)) {
                // 市
                Map<String, String> cityMap = (Map<String, String>) sourceMap.get(pcode);
                cityMap.forEach((cityCode, cityName) -> {
                    boolean flag1 = true;
                    citys.add(cityName);
                    if (flag1 && cityName.endsWith("市")) {
                        citys.add(cityName.substring(0, cityName.length() - 1));
                        flag1 = false;
                    }
                    if (flag1 && pname.endsWith("地区") || pname.endsWith("城区") || pname.endsWith("郊县")) {
                        citys.add(pname.substring(0, pname.length() - 2));
                        flag1 = false;
                    }
                    // 配置中的特殊市，白话
                    if (flag1 && vernacularCityConfig.containsKey(pname)) {
                        citys.addAll(vernacularCityConfig.get(pname));
                        flag1 = false;
                    }
                    if (sourceMap.containsKey(cityCode)) {
                        // 区
                        Map<String, String> countyMap = (Map<String, String>) sourceMap.get(cityCode);
                        countyMap.forEach((countyCode, countyName) -> {
                            boolean flag2 = true;
                            countys.add(countyName);
                            if (countyName.endsWith("市") || countyName.endsWith("区") || countyName.endsWith("县")) {
                                countys.add(countyName.substring(0, countyName.length() - 1));
                                flag2 = false;
                            }
                            if (flag2 && countyName.endsWith("自治县") || countyName.endsWith("自治州")) {
                                countys.add(countyName.substring(0, countyName.length() - 3));
                                flag2 = false;
                            }
                            // 配置中的特殊区，白话
                            if (flag2 && vernacularCountyConfig.containsKey(pname)) {
                                countys.addAll(vernacularCountyConfig.get(pname));
                                flag2 = false;
                            }
                        });
                    }
                });
            }
        });
        Map<String, List<String>> resultMap = new HashMap<>();
        resultMap.put("province", provinces);
        resultMap.put("city", citys);
        resultMap.put("county", countys);
        System.out.println("province.size:" + provinces.size());
        System.out.println("city.size:" + citys.size());
        System.out.println("county.size:" + countys.size());
        return resultMap;
    }

    /**
     * 得到所有市和区的关系
     *
     * @param sourceMap json文件源数据
     * @return {@link Map}<{@link String}, {@link List}<{@link String}>>
     */
    private Map<String, List<String>> analyzerCityCounty(Map<String, Object> sourceMap) {
        Map<String, Object> provinceMap = (Map<String, Object>) sourceMap.get("100000");
        Map<String, List<String>> cityCountyMap = new HashMap<>();
        provinceMap.forEach((pcode, pname) -> {
            if (sourceMap.containsKey(pcode)) {
                // 市
                Map<String, String> cityMap = (Map<String, String>) sourceMap.get(pcode);
                cityMap.forEach((cityCode, cityName) -> {
                    if (sourceMap.containsKey(cityCode)) {
                        // 区
                        Map<String, String> countyMap = (Map<String, String>) sourceMap.get(cityCode);
                        List<String> countyNames = new ArrayList<>();
                        countyMap.forEach((countyCode, countyName) -> {
                            countyNames.add(countyName);
                        });
                        cityCountyMap.put(cityName, countyNames);
                    }
                });
            }
        });
        return cityCountyMap;
    }

    public Address addressResolution(String address) {
        if (address == null) {
            return null;
        }
        Address addressVO = new Address();
        addressVO.setAddress(address);
        // 解析省市区
        analyzerProvince(addressVO, addressVO.getAddress());
        analyzerCity(addressVO, addressVO.getAddress());
        analyzerCounty(addressVO, addressVO.getAddress());

        deduce(addressVO);  // 推算
        // 地址解析是否成功
        addressVO.setSuccess(true);
        return addressVO;
    }

    /**
     * 匹配省
     *
     * @param addressVO 地址
     * @param address   地址
     */
    public void analyzerProvince(Address addressVO, String address) {
        if (StringUtils.isBlank(address)) {
            return;
        }
        addressVO.setAddress(address);
        for (String province : provinces) {
            if (!address.startsWith(province)) {
                continue;
            }
            for (String fullProvince : provinceCityCounty.keySet()) {
                if (fullProvince.contains(province)) {
                    addressVO.setProvince(fullProvince);
                    addressVO.setAddress(address.substring(province.length()));
                    return;
                }
            }
            break;
        }
    }

    /**
     * 匹配市
     *
     * @param addressVO 地址
     * @param address   地址
     */
    public void analyzerCity(Address addressVO, String address) {
        if (StringUtils.isBlank(address)) {
            return;
        }
        List<String> citiesBelongProvince = getCitiesByProvince(addressVO.getProvince(), true);
        for (String city : citys) {
            if (!address.startsWith(city)) {
                continue;
            }
            for (String fullCity : cityCounty.keySet()) {
                if (fullCity.contains(city) && citiesBelongProvince.contains(fullCity)) {
                    addressVO.setCity(fullCity);
                    addressVO.setAddress(address.substring(city.length()));
                    return;
                }
            }
            if (addressVO.getCity() != null) {
                return;
            }
        }
    }

    /**
     * 匹配区
     *
     * @param addressVO 地址
     * @param address   地址
     */
    public void analyzerCounty(Address addressVO, String address) {
        if (StringUtils.isBlank(address)) {
            return;
        }
        List<String> countiesBelongCity = getCountiesByProvinceCity(addressVO.getProvince(), addressVO.getCity(), true);
        for (String county : countys) {
            if (!address.startsWith(county)) {
                continue;
            }
            List<String> countyList;
            for (String fullCity : cityCounty.keySet()) {
                countyList = cityCounty.get(fullCity);
                if (countyList == null) {
                    continue;
                }
                for (String ct : countyList) {
                    if (ct.contains(county) && countiesBelongCity.contains(ct)) {
                        addressVO.setCounty(ct);
                        addressVO.setAddress(address.substring(county.length()));
                        return;
                    }
                }
            }
            break;
        }
    }

    /**
     * 获取指定省份对应的所有市
     *
     * @param province     省
     * @param isExtensible 是否允许扩展查询（如果否，则只查询对应省份下的市，如果省份是空，则返回空）
     * @return {@link List}<{@link String}>
     */
    private List<String> getCitiesByProvince(String province, boolean isExtensible) {
        List<String> cityList = new ArrayList<>();
        if (province == null && !isExtensible) {
            return cityList;
        }
        // province != null || isExtensible
        if (province == null) {
            // 扩展查询所有省份下的市
            cityList.addAll(cityCounty.keySet());
            return cityList;
        }
        // province != null
        Map<String, List<String>> cityCountyMap = provinceCityCounty.getOrDefault(province, null);
        if (cityCountyMap != null) {
            cityList.addAll(cityCountyMap.keySet());
        }
        return cityList;
    }

    /**
     * 被省县城市
     * 根据省份和市获取所有的区
     *
     * @param targetProvince 省
     * @param tartgetCity    城市
     * @param isExtensible   是否允许扩展查询（如果否，则只查询对应city下的区，如果city是空，则返回空）
     * @return {@link List}<{@link String}>
     */
    private List<String> getCountiesByProvinceCity(String targetProvince, String tartgetCity, boolean isExtensible) {
        List<String> countyList = new ArrayList<>();
        if (tartgetCity == null && !isExtensible) {
            return countyList;
        }
        List<String> citiesBelongProvince = getCitiesByProvince(targetProvince, isExtensible);
        if (tartgetCity == null) {
            // 没有指定市就获取省份下的所有区
            List<String> countyBelongCity;
            for (String cty : citiesBelongProvince) {
                countyBelongCity = cityCounty.getOrDefault(cty, null);
                if (countyBelongCity != null && !countyBelongCity.isEmpty()) {
                    countyList.addAll(countyBelongCity);
                }
            }
            return countyList;
        }
        if (!citiesBelongProvince.contains(tartgetCity)) {
            System.out.println("未知的城市：" + tartgetCity);
            return countyList;
        }
        List<String> countyBelongCity = cityCounty.getOrDefault(tartgetCity, null);
        if (countyBelongCity == null) {
            return countyList;
        }
        countyList.addAll(countyBelongCity);
        return countyList;
    }

    /**
     * 地址推算。
     * 根据区逆推市，根据市逆推省
     *
     * @param addressVO 地址
     */
    public void deduce(Address addressVO) {
        // 先校验地址的合理性
        if (addressVO.getCounty() != null && addressVO.getCity() == null) {
            // 逆推市
            List<String> county;
            for (String city : cityCounty.keySet()) {
                county = cityCounty.get(city);
                if (county.contains(addressVO.getCounty())) {
                    addressVO.setCity(city);
                }
            }
        }
        if (addressVO.getCity() != null && addressVO.getProvince() == null) {
            // 逆推省
            Map<String, List<String>> cc;
            for (String province : provinceCityCounty.keySet()) {
                cc = provinceCityCounty.get(province);
                if (cc != null && cc.containsKey(addressVO.getCity())) {
                    addressVO.setProvince(province);
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        AddressAnalyzer addressService = new AddressAnalyzer();
        addressService.builder();  //

        List<String> addressList = new ArrayList<>();
        addressList.add("贵州省七星关区何官屯镇刘家村11号");
        addressList.add("七星关区何官屯镇刘家村11号");
        addressList.add("福建省长汀县汀洲镇中心坝竹区8号");
        addressList.add("福建长汀汀洲镇中心坝竹区8号");
        addressList.add("毕节市何官屯镇刘家村11号");
        addressList.add("内蒙锡林郭勒盟二连浩特市肯特街南、建设路东");
        addressList.add("河北大城县北位乡魏胡村12街5巷437号");
        addressList.add("镇江市句容市崇明西路与玉清路交叉路口西侧(玉清小区)");
        addressList.add("北京朝阳方恒国际中心A座2601号");
        for (String address : addressList) {
            Address addressVO = addressService.addressResolution(address);
            System.out.print("address:" + address);
            System.out.println(",   result:" + addressVO);
        }
    }

}
