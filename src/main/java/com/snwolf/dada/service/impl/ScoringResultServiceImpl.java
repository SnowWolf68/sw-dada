package com.snwolf.dada.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snwolf.dada.domain.dto.ScoringResultAddDTO;
import com.snwolf.dada.domain.dto.ScoringResultEditDTO;
import com.snwolf.dada.domain.dto.ScoringResultQueryDTO;
import com.snwolf.dada.domain.dto.ScoringResultUpdateDTO;
import com.snwolf.dada.domain.entity.ScoringResult;
import com.snwolf.dada.domain.entity.User;
import com.snwolf.dada.domain.vo.ScoringResultVO;
import com.snwolf.dada.domain.vo.UserVO;
import com.snwolf.dada.exception.DeletionNotAllowedException;
import com.snwolf.dada.exception.EditNotAllowedException;
import com.snwolf.dada.exception.ScoringResultNotExistException;
import com.snwolf.dada.mapper.ScoringResultMapper;
import com.snwolf.dada.service.IScoringResultService;
import com.snwolf.dada.service.IUserService;
import com.snwolf.dada.utils.SqlUtils;
import com.snwolf.dada.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoringResultServiceImpl extends ServiceImpl<ScoringResultMapper, ScoringResult> implements IScoringResultService {

    private final IUserService userService;

    @Override
    public void add(ScoringResultAddDTO scoringResultAddDTO) {
        ScoringResult scoringResult = BeanUtil.copyProperties(scoringResultAddDTO, ScoringResult.class, "resultProp");
        String resultPropStr = JSONUtil.toJsonStr(scoringResultAddDTO.getResultProp());
        scoringResult.setResultProp(resultPropStr);
        scoringResult.setUserId(UserHolder.getUser().getId());
        save(scoringResult);
    }

    @Override
    public void delete(Long id) {
        ScoringResult scoringResult = getById(id);
        if(scoringResult == null){
            throw new ScoringResultNotExistException("评分结果不存在");
        }
        Long scoringResultUserId = scoringResult.getUserId();
        Long userId = UserHolder.getUser().getId();
        String userRole = UserHolder.getUser().getUserRole();
        if(!(userId == scoringResultUserId || userRole.equals("admin"))){
            throw new DeletionNotAllowedException("无权限删除");
        }
        removeById(id);
    }

    @Override
    public void updateWithResultProp(ScoringResultUpdateDTO scoringResultUpdateDTO) {
        ScoringResult scoringResult = BeanUtil.copyProperties(scoringResultUpdateDTO, ScoringResult.class, "resultProp");
        if(CollectionUtil.isNotEmpty(scoringResultUpdateDTO.getResultProp())){
            List<String> resultPropList = scoringResultUpdateDTO.getResultProp();
            String resultPropStr = JSONUtil.toJsonStr(resultPropList);
            scoringResult.setResultProp(resultPropStr);
        }
        updateById(scoringResult);
    }

    @Override
    public ScoringResultVO getByIdWithUserVO(Long id) {
        ScoringResult scoringResult = getById(id);
        ScoringResultVO scoringResultVO = BeanUtil.copyProperties(scoringResult, ScoringResultVO.class, "resultProp", "userVO");
        List<String> resultProp = JSONUtil.toList(scoringResult.getResultProp(), String.class);
        scoringResultVO.setResultProp(resultProp);
        Long userId = UserHolder.getUser().getId();
        User user = userService.getById(userId);
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        scoringResultVO.setUserVO(userVO);
        return scoringResultVO;
    }

    @Override
    public Page<ScoringResultVO> pageQuery(ScoringResultQueryDTO scoringResultQueryDTO) {
        QueryWrapper<ScoringResult> queryWrapper = getQueryWrapper(scoringResultQueryDTO);
        Page<ScoringResult> page = new Page<>(scoringResultQueryDTO.getCurrent(), scoringResultQueryDTO.getPageSize());
        Page<ScoringResult> scoringResultPageResult = page(page, queryWrapper);
        List<ScoringResultVO> scoringResultVOPageResult = scoringResultPageResult.getRecords()
                .stream()
                .map(scoringResult -> {
                    ScoringResultVO scoringResultVO = BeanUtil.copyProperties(scoringResult, ScoringResultVO.class, "resultProp", "userVO");
                    List<String> resultProp = JSONUtil.toList(scoringResult.getResultProp(), String.class);
                    scoringResultVO.setResultProp(resultProp);
                    Long userId = scoringResult.getUserId();
                    User user = userService.getById(userId);
                    UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
                    scoringResultVO.setUserVO(userVO);
                    return scoringResultVO;
                })
                .collect(Collectors.toList());
        Page<ScoringResultVO> scoringResultVOPage = new Page<>(scoringResultQueryDTO.getCurrent(), scoringResultQueryDTO.getPageSize());
        scoringResultVOPage.setRecords(scoringResultVOPageResult);
        return scoringResultVOPage;
    }

    @Override
    public void edit(ScoringResultEditDTO scoringResultEditDTO) {
        Long scoringResultId = scoringResultEditDTO.getId();
        ScoringResult oldScoringResult = getById(scoringResultId);
        Long userId = oldScoringResult.getUserId();
        Long currentUserId = UserHolder.getUser().getId();
        String currentUserRole = UserHolder.getUser().getUserRole();
        if(!(userId == currentUserId || currentUserRole.equals("admin"))){
            throw new EditNotAllowedException("当前用户无权限编辑");
        }
        ScoringResult scoringResult = BeanUtil.copyProperties(scoringResultEditDTO, ScoringResult.class, "resultProp");
        if(CollectionUtil.isNotEmpty(scoringResultEditDTO.getResultProp())){
            List<String> resultPropList = scoringResultEditDTO.getResultProp();
            String resultPropStr = JSONUtil.toJsonStr(resultPropList);
            scoringResult.setResultProp(resultPropStr);
        }
        updateById(scoringResult);
    }

    public QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryDTO scoringResultQueryDTO) {
        QueryWrapper<ScoringResult> queryWrapper = new QueryWrapper<>();
        if (scoringResultQueryDTO == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = scoringResultQueryDTO.getId();
        String resultName = scoringResultQueryDTO.getResultName();
        String resultDesc = scoringResultQueryDTO.getResultDesc();
        String resultPicture = scoringResultQueryDTO.getResultPicture();
        String resultProp = scoringResultQueryDTO.getResultProp();
        Integer resultScoreRange = scoringResultQueryDTO.getResultScoreRange();
        Long appId = scoringResultQueryDTO.getAppId();
        Long userId = scoringResultQueryDTO.getUserId();
        Long notId = scoringResultQueryDTO.getNotId();
        String searchText = scoringResultQueryDTO.getSearchText();
        String sortField = scoringResultQueryDTO.getSortField();
        String sortOrder = scoringResultQueryDTO.getSortOrder();

        // 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("result_name", searchText).or().like("result_desc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(resultName), "result_name", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "result_desc", resultDesc);
        queryWrapper.like(StringUtils.isNotBlank(resultProp), "result_prop", resultProp);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "app_id", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultScoreRange), "result_score_range", resultScoreRange);
        queryWrapper.eq(StringUtils.isNotBlank(resultPicture), "result_picture", resultPicture);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals("ascend"),
                sortField);
        return queryWrapper;
    }
}
