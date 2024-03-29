package com.jxm.file.service;


import com.jxm.file.dto.DashboardUserFileParam;
import com.jxm.file.dto.FileOperateLogDetail;
import com.jxm.file.entity.FileOperateLog;
import com.jxm.file.entity.RPanUserFile;
import com.jxm.file.vo.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

/**
 * 用户文件业务处理接口
 * Created by RubinChu on 2021/1/22 下午 4:11
 */
public interface IUserFileService {

    List<RPanUserFileDisplayVO> filesForTable(Long pageType,String keyword,Integer fileType,Integer pageNum,Integer pageSize,Long depId);

    List<RPanUserFileDisplayVO> list( Long pageType,Long parentId, String fileTypes, Long depId);

    List<RPanUserFileVO> list(String fileIds);

    void createFolder(Boolean isPageType,Long parentId, String folderName, Object loginUser);

    void createDepRootFolder(Long parentId, String folderName, Long userId,Long depId);

    void saveSet(Long fileId, Integer isWaterMater, Long loginUserId);

    void updateFilename(Long fileId, String newFilename, Object loginUser);

    void delete(Long fileId);

    /**
     * 批量删除指定用户
     */
    @Transactional
    int deleteBatch(List<Long> idList);

    @Transactional
    int passBatch(List<Long> idList,Object loginUser);

    int deleteFile(Long id);

    Long getUserByFileId(Long fileId);

    int passFile(Long id,Object loginUser);

    void upload(MultipartFile file, Long parentId, Object loginUser, String identifier, Long totalSize, String filename);

    FileChunkUploadVO uploadWithChunk(MultipartFile file, Long userId, String identifier, Integer totalChunks, Integer chunkNumber, Long totalSize, String filename);

    void download(Long fileId, String waterMark,HttpServletResponse response);

    void uploadLog(Long fileId,Long userId);

    void downloadLog(Long fileId,Long userId,String waterMark);

    void deleteLog(Long fileId,Long userId);

    List<FolderTreeNodeVO> getFolderTree(Long fileRootId, Long depId);

    void transfer(String fileIds, Long targetParentId, Long depId);

    void copy(Integer pageType,String fileIds, Long targetParentId, Object loginUser);

    List<RPanUserFileSearchVO> search(String keyword, String fileTypes, Long userId);

    RPanUserFileDisplayVO detail(Long fileId, Long userId);

    List<BreadcrumbVO> getBreadcrumbs(Long fileId, Long depId);

    void preview(Long fileId,String userName, HttpServletResponse response);

    void preview(String filePath,String userName, HttpServletResponse response, String filePreviewContentType);

    void restoreUserFiles(String fileIds, HashMap<String, Integer> map);

    void physicalDeleteUserFiles(String fileIds, Long depId);

    List<RPanUserFileVO> allList(String fileIds);

    RPanUserFile getUserTopFileInfo(String depId);

    String getAllAvailableFileIdByFileIds(String fileIds);

    boolean checkAllUpFileAvailable(List<Long> fileIds);

    boolean secUpload(Integer pageType,Long parentId, String filename, String md5,Object loginUser);

    CheckFileChunkUploadVO checkUploadWithChunk(Long userId, String identifier);

    void mergeChunks(Integer pageType,String filename, String identifier, Long parentId, Long totalSize, Object loginUser);

    /**
     * 获取企业文件类型数量信息
     */
    List<DashboardUserFileParam> getTheNumberOfFileTypes();

}
