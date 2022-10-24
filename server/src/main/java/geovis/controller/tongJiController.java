package geovis.controller;


import geovis.param.*;
import geovis.service.dataSourceService;
import geovis.service.tongJiService;
import geovis.tools.Result;
import geovis.tools.wordUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/***
 * 创建：lxq 2022-8-9
 * 作用：资源数据的处理
 */

@Api(tags = "对外接口列表")
@RestController
public class tongJiController {

    @Value("${dataDir}")
    private String dataDir;
    @Autowired
    private dataSourceService datasourceservice;
    @Autowired
    private tongJiService tongjiservice;

    private static final String FIX="\uFEFF";
    private wordUtil wordutil = new wordUtil();



    @ApiOperation("自动统计所有地面数据")
    @RequestMapping(value = "/doSumAllSurfData", method = {RequestMethod.GET,RequestMethod.POST})
    public Result doSumAllSurfData(surfParam param) {
        Result res = tongjiservice.doSumAllSurfData(param);
        return res;
    }







// end
}
