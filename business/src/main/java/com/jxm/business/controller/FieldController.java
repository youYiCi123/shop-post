package com.jxm.business.controller;

import com.jxm.business.dto.FieldDetailDto;
import com.jxm.business.dto.QuDto;
import com.jxm.business.dto.TempParam;
import com.jxm.business.dto.TempQuDto;
import com.jxm.business.model.CustomParam;
import com.jxm.business.service.FieldService;
import com.jxm.common.api.CommonPage;
import com.jxm.common.api.CommonResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/field")
public class FieldController {

    @Autowired
    private FieldService fieldService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<QuDto>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "quType", required = false) Integer quType,
                                                @RequestParam(value = "tempId", required = false) Long tempId,
                                                @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<QuDto> quDtoList = fieldService.list(keyword,quType,tempId, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(quDtoList));
    }

    @ApiOperation(value = "后台查询字段（用于编辑）")
    @RequestMapping(value = "/queryContent/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<FieldDetailDto> queryContent(@PathVariable Long id){
        return CommonResult.success(fieldService.queryContent(id));
    }

    /**
     * 添加字段内容
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult addContent(@RequestBody FieldDetailDto fieldDetailDto){
        int count = fieldService.addContent(fieldDetailDto);
        if(count!=0){
            return CommonResult.success();
        }else {
            return CommonResult.failed();
        }
    }

    /**
     * 修改字段内容
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateContent(@RequestBody FieldDetailDto fieldDetailDto){
        int count = fieldService.updateContent(fieldDetailDto);
        if(count!=0){
            return CommonResult.success();
        }else {
            return CommonResult.failed();
        }
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@PathVariable Long id) {
        int count=fieldService.delete(id);
        if(count<0)
            return CommonResult.failed("删除字段信息错误");
        return CommonResult.success();
    }

    @RequestMapping(value = "/tempDetail/{id}", method = { RequestMethod.GET})
    @ResponseBody
    public CommonResult<TempQuDto> getTempDetailResult(@PathVariable Long id) {
        TempQuDto respDTO = fieldService.getTempDetailResult(id);
        return CommonResult.success(respDTO);
    }

}
