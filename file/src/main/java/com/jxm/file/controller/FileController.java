package com.jxm.file.controller;

import com.jxm.common.api.CommonResult;
import com.jxm.common.service.RedisService;
import com.jxm.file.entity.RPanUserFile;
import com.jxm.file.feign.UpstageService;
import com.jxm.file.mapper.RPanUserFileMapper;
import com.jxm.file.po.*;
import com.jxm.file.service.IUserFileService;
import com.jxm.file.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

/**
 * 项目文件相关rest接口返回
 */
@RestController
@Validated
@Api(tags = "文件")
public class FileController {

    @Autowired
    @Qualifier(value = "userFileService")
    private IUserFileService iUserFileService;

    @Autowired
    @Qualifier(value = "rPanUserFileMapper")
    private RPanUserFileMapper rPanUserFileMapper;

    @Autowired
    private UpstageService upstageService;

    @Value("${spring.redis.key.admin}")
    private String REDIS_KEY_ADMIN;


    @ApiOperation(
            value = "获取文件列表",
            notes = "该接口提供了获取文件列表的功能"
    )
    @GetMapping("files")
    public CommonResult<List<RPanUserFileDisplayVO>> list(@NotNull(message = "父id不能为空") @RequestParam(value = "parentId", required = false) Long parentId,
                                                          @RequestParam(name = "fileTypes", required = false, defaultValue = "-1") String fileTypes) throws ParseException {
        return CommonResult.success(iUserFileService.list(parentId, fileTypes, getLoginDepId()));
    }

    @ApiOperation(
            value = "新建文件夹",
            notes = "该接口提供了新建文件夹的功能"
    )
    @PostMapping("file/folder")
    public CommonResult createFolder(@Validated @RequestBody CreateFolderPO createFolderPO) throws ParseException {
        iUserFileService.createFolder(createFolderPO.getParentId(), createFolderPO.getFolderName(), getLoginUserId(),getLoginDepId());
        return CommonResult.success();
    }

    @ApiOperation(
            value = "文件重命名",
            notes = "该接口提供了文件重命名的功能"
    )
    @PutMapping("file")
    public CommonResult updateFilename(@Validated @RequestBody UpdateFileNamePO updateFileNamePO) throws ParseException {
        Object loginUser = getLoginUser();
        HashMap<String, Integer> map = (HashMap<String, Integer>)loginUser;
        iUserFileService.updateFilename(updateFileNamePO.getFileId(), updateFileNamePO.getNewFilename(), map);
        return CommonResult.success();
    }

    @ApiOperation(
            value = "删除文件(批量)",
            notes = "该接口提供了删除文件(批量)的功能"
    )
    @DeleteMapping("file")
    public CommonResult delete(@Validated @RequestBody DeletePO deletePO) throws ParseException {
        Object loginUser = getLoginUser();
        HashMap<String, Integer> map = (HashMap<String, Integer>)loginUser;
        iUserFileService.delete(deletePO.getFileIds(), map);
        return CommonResult.success();
    }

    @ApiOperation(
            value = "上传单文件",
            notes = "该接口提供了上传单文件的功能"
    )
    @PostMapping("file/upload")
    public CommonResult upload(@Validated FileUploadPO fileUploadPO) throws ParseException {
        Object loginUser = getLoginUser();
        HashMap<String, Integer> map = (HashMap<String, Integer>)loginUser;
        iUserFileService.upload(fileUploadPO.getFile(), fileUploadPO.getParentId(), map, fileUploadPO.getIdentifier(), fileUploadPO.getTotalSize(), fileUploadPO.getFilename());
        return CommonResult.success();
    }

    @ApiOperation(
            value = "分片上传并检查已上传的分片",
            notes = "该接口提供了分片上传并检查已上传的分片的功能"
    )
    @GetMapping("file/chunk-upload")
    public CommonResult<CheckFileChunkUploadVO> checkUploadWithChunk(@Validated FileChunkCheckPO fileChunkCheckPO) throws IOException, ParseException {
        CheckFileChunkUploadVO checkFileChunkUploadVO = iUserFileService.checkUploadWithChunk(getLoginUserId(), fileChunkCheckPO.getIdentifier());
        return CommonResult.success(checkFileChunkUploadVO);
    }

    @ApiOperation(
            value = "分片上传文件",
            notes = "该接口提供了分片上传文件的功能"
    )
    @PostMapping("file/chunk-upload")
    public CommonResult<FileChunkUploadVO> uploadWithChunk(@Validated FileChunkUploadPO fileChunkUploadPO) throws ParseException {
        FileChunkUploadVO fileChunkUploadVO = iUserFileService.uploadWithChunk(fileChunkUploadPO.getFile(), getLoginUserId(), fileChunkUploadPO.getIdentifier(), fileChunkUploadPO.getTotalChunks(), fileChunkUploadPO.getChunkNumber(), fileChunkUploadPO.getTotalSize(), fileChunkUploadPO.getFilename());
        return CommonResult.success(fileChunkUploadVO);
    }

    @ApiOperation(
            value = "合并文件分片",
            notes = "该接口提供了合并文件分片的功能"
    )
    @PostMapping("file/merge")
    public CommonResult mergeChunks(@Validated @RequestBody FileChunkMergePO fileChunkMergePO) throws ParseException {
        Object loginUser = getLoginUser();
        HashMap<String, Integer> map = (HashMap<String, Integer>)loginUser;
        iUserFileService.mergeChunks(fileChunkMergePO.getFilename(), fileChunkMergePO.getIdentifier(), fileChunkMergePO.getParentId(), fileChunkMergePO.getTotalSize(), map);
        return CommonResult.success();
    }

    @ApiOperation(
            value = "秒传文件",
            notes = "该接口提供了秒传文件的功能，在文件生成唯一标识之后上传，根据返回结果决定是否要执行物理上传"
    )
    @PostMapping("file/sec-upload")
    public CommonResult secUpload(@Validated @RequestBody FileSecUploadPO fileSecUploadPO) throws ParseException {
        Object loginUser = getLoginUser();
        HashMap<String, Integer> map = (HashMap<String, Integer>)loginUser;
        if (iUserFileService.secUpload(fileSecUploadPO.getParentId(), fileSecUploadPO.getFilename(), fileSecUploadPO.getIdentifier(), map)) {
            return CommonResult.success();
        }
        return CommonResult.failed("文件唯一标识不存在，请执行物理上传");
    }

    @ApiOperation(
            value = "下载文件(只支持单个下载)",
            notes = "该接口提供了下载文件(只支持单个下载)的功能"
    )
    @GetMapping("file/download")
    public void download(@NotNull(message = "请选择要下载的文件") @RequestParam(value = "fileId", required = false) Long fileId,
                         HttpServletResponse response) throws ParseException {
        iUserFileService.download(fileId, response, getLoginUserId());
    }

    private Object getLoginUser() throws ParseException{
        return upstageService.getCurrentAdmin().getData();
    }

    private Long getLoginDepId() throws ParseException{
        Object data = upstageService.getCurrentAdmin().getData();
        HashMap<String, Integer> map = (HashMap<String, Integer>)data;
        long depId = map.get("depId").longValue();
        return depId;
    }

    private Long getLoginUserId() throws ParseException{
        Object data = upstageService.getCurrentAdmin().getData();
        HashMap<String, Integer> map = (HashMap<String, Integer>)data;
        long userId = map.get("userId").longValue();
        return userId;
    }


    @ApiOperation(
            value = "获取文件夹树",
            notes = "该接口提供了获取文件夹树的功能"
    )
    @GetMapping("file/folder/tree")
    public CommonResult<List<FolderTreeNodeVO>> getFolderTree() throws ParseException {
        return CommonResult.success(iUserFileService.getFolderTree(getLoginDepId()));
    }


    @ApiOperation(
            value = "转移文件(批量)",
            notes = "该接口提供了转移文件(批量)的功能"
    )
    @PostMapping("file/transfer")
    public CommonResult transfer(@Validated @RequestBody TransferPO transferPO) throws ParseException {
        iUserFileService.transfer(transferPO.getFileIds(), transferPO.getTargetParentId(), getLoginDepId());
        return CommonResult.success();
    }

    @ApiOperation(
            value = "复制文件(批量)",
            notes = "该接口提供了复制文件(批量)的功能"
    )
    @PostMapping("file/copy")
    public CommonResult copy(@Validated @RequestBody CopyPO copyPO) throws ParseException {
        iUserFileService.copy(copyPO.getFileIds(), copyPO.getTargetParentId(), getLoginDepId());
        return CommonResult.success();
    }

    @ApiOperation(
            value = "获取用户File根信息",
            notes = "该接口提供了获取用户File根信息的功能"
    )
    @GetMapping("file/getUserTopFileInfo")
    public CommonResult getUserTopFileInfo(Long depId){
        return CommonResult.success( rPanUserFileMapper.selectTopFolderByUserId(depId));
    }
    /**
     * 通过文件名搜索文件列表
     *
     * @param keyword
     * @param fileTypes
     * @return
     */
    @ApiOperation(
            value = "通过文件名搜索文件列表",
            notes = "该接口提供了通过文件名搜索文件列表的功能"
    )
    @GetMapping("file/search")
    public CommonResult<List<RPanUserFileSearchVO>> search(@NotBlank(message = "关键字不能为空") @RequestParam(value = "keyword", required = false) String keyword,
                                                           @RequestParam(name = "fileTypes", required = false, defaultValue = "-1") String fileTypes) throws ParseException {
        return CommonResult.success(iUserFileService.search(keyword, fileTypes, getLoginUserId()));
    }

    /**
     * 查询文件详情
     *
     * @param fileId
     * @return
     */
    @ApiOperation(
            value = "查询文件详情",
            notes = "该接口提供了查询文件详情的功能"
    )
    @GetMapping("file")
    public CommonResult<RPanUserFileDisplayVO> detail(@NotNull(message = "文件id不能为空") @RequestParam(value = "fileId", required = false) Long fileId) throws ParseException {
        return CommonResult.success(iUserFileService.detail(fileId, getLoginUserId()));
    }

    /**
     * 获取面包屑列表
     *
     * @return
     */
    @ApiOperation(
            value = "获取面包屑列表",
            notes = "该接口提供了获取面包屑列表的功能"
    )
    @GetMapping("file/breadcrumbs")
    public CommonResult<List<BreadcrumbVO>> getBreadcrumbs(@NotNull(message = "文件id不能为空") @RequestParam(value = "fileId", required = false) Long fileId) throws ParseException {
        return CommonResult.success(iUserFileService.getBreadcrumbs(fileId, getLoginDepId()));
    }

    /**
     * 预览单个文件
     *
     * @param fileId
     * @return
     */
    @ApiOperation(
            value = "预览单个文件",
            notes = "该接口提供了预览单个文件的功能"
    )
    @GetMapping("preview")
    public void preview(@NotNull(message = "文件id不能为空") @RequestParam(value = "fileId", required = false) Long fileId,
                        HttpServletResponse response) throws ParseException {
        Long depId = getLoginDepId();
        iUserFileService.preview(fileId, response,depId);
    }

    @GetMapping("image/{fileName}")
    public void getCommentImage(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        //1.file源文件
        ServletOutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream("d:\\Users\\naccl\\Desktop\\upload\\"+fileName);
        response.setContentType("image/png");//告诉浏览器显示图片
        //response.setContentType("multipart/form-data");//告诉浏览器下载图片
        out = response.getOutputStream();
        //读取文件流
        int len = 0;
        byte[] buffer = new byte[1024 * 10];
        while ((len = in.read(buffer)) != -1){
            out.write(buffer,0,len);
        }
        out.flush();
    }

}