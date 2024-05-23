package com.snwolf.dada.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snwolf.dada.domain.dto.*;
import com.snwolf.dada.domain.entity.App;
import com.snwolf.dada.domain.entity.User;
import com.snwolf.dada.domain.entity.UserAnswer;
import com.snwolf.dada.domain.vo.UserAnswerVO;
import com.snwolf.dada.domain.vo.UserVO;
import com.snwolf.dada.exception.*;
import com.snwolf.dada.mapper.UserAnswerMapper;
import com.snwolf.dada.service.IAppService;
import com.snwolf.dada.service.IUserAnswerService;
import com.snwolf.dada.service.IUserService;
import com.snwolf.dada.utils.SqlUtils;
import com.snwolf.dada.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer> implements IUserAnswerService {

    private final IAppService appService;

    private final IUserService userService;

    @Override
    public void add(UserAnswerAddDTO userAnswerAddDTO) {
        Long appId = userAnswerAddDTO.getAppId();
        App app = appService.getById(appId);
        if(app == null){
            throw new AppNotExistException("评分应用不存在");
        }
        if(app.getReviewStatus() != 1){
            throw new AppNotReviewedException("应用未通过审核, 不能答题");
        }
        String choiceStr = JSONUtil.toJsonStr(userAnswerAddDTO.getChoices());
        UserAnswer userAnswer = BeanUtil.copyProperties(userAnswerAddDTO, UserAnswer.class);
        userAnswer.setChoices(choiceStr);
        Long userId = UserHolder.getUser().getId();
        userAnswer.setUserId(userId);
        save(userAnswer);
    }

    @Override
    public void delete(Long id) {
        UserAnswer userAnswer = getById(id);
        if(userAnswer == null){
            throw new UserAnswerNotExistException("要删除的用户回答不存在");
        }
        Long userId = userAnswer.getUserId();
        Long currentUserId = UserHolder.getUser().getId();
        String currentUserRole = UserHolder.getUser().getUserRole();
        if(!(userId.equals(currentUserId) || currentUserRole.equals("admin"))){
            throw new DeletionNotAllowedException("当前用户无权限删除");
        }
        removeById(id);
    }

    @Override
    public void updateWithChoices(UserAnswerUpdateDTO userAnswerUpdateDTO) {
        List<String> choicesList = userAnswerUpdateDTO.getChoices();
        String choiceStr = JSONUtil.toJsonStr(choicesList);
        UserAnswer userAnswer = BeanUtil.copyProperties(userAnswerUpdateDTO, UserAnswer.class, "choice");
        userAnswer.setChoices(choiceStr);
        updateById(userAnswer);
    }

    @Override
    public UserAnswerVO getUserAnswerByIdWithUserVO(Long id) {
        UserAnswer userAnswer = getById(id);
        UserAnswerVO userAnswerVO = BeanUtil.copyProperties(userAnswer, UserAnswerVO.class, "choice");
        String choicesStr = userAnswer.getChoices();
        List<String> choiceList = JSONUtil.toList(choicesStr, String.class);
        userAnswerVO.setChoices(choiceList);
        UserDTO userDTO = UserHolder.getUser();
        UserVO userVO = BeanUtil.copyProperties(userDTO, UserVO.class);
        userAnswerVO.setUserVO(userVO);
        return userAnswerVO;
    }

    @Override
    public Page<UserAnswerVO> pageQuery(UserAnswerQueryDTO userAnswerQueryDTO) {
        QueryWrapper<UserAnswer> queryWrapper = getQueryWrapper(userAnswerQueryDTO);
        Page<UserAnswer> userAnswerPage = new Page<>(userAnswerQueryDTO.getCurrent(), userAnswerQueryDTO.getPageSize());
        Page<UserAnswer> userAnswerPageResult = page(userAnswerPage, queryWrapper);
        List<UserAnswerVO> userAnswerVOList = userAnswerPageResult.getRecords()
                .stream()
                .map(userAnswer -> {
                    UserAnswerVO userAnswerVO = BeanUtil.copyProperties(userAnswer, UserAnswerVO.class, "choice");
                    String choicesStr = userAnswer.getChoices();
                    List<String> choiceList = JSONUtil.toList(choicesStr, String.class);
                    userAnswerVO.setChoices(choiceList);
                    Long userId = userAnswer.getUserId();
                    User user = userService.getById(userId);
                    UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
                    userAnswerVO.setUserVO(userVO);
                    return userAnswerVO;
                })
                .collect(Collectors.toList());
        Page<UserAnswerVO> userAnswerVOPageResult = new Page<>(userAnswerQueryDTO.getCurrent(), userAnswerQueryDTO.getPageSize());
        userAnswerVOPageResult.setRecords(userAnswerVOList);
        return userAnswerVOPageResult;
    }

    @Override
    public void edit(UserAnswerEditDTO userAnswerEditDTO) {
        Long userAnswerId = userAnswerEditDTO.getId();
        UserAnswer oldUserAnswer = getById(userAnswerId);
        if(oldUserAnswer == null){
            throw new UserAnswerNotExistException("要修改的用户回答不存在");
        }
        Long userId = oldUserAnswer.getUserId();
        Long currentUserId = UserHolder.getUser().getId();
        String currentUserRole = UserHolder.getUser().getUserRole();
        if(!(userId.equals(currentUserId) || currentUserRole.equals("admin"))){
            throw new EditNotAllowedException("当前用户无权限编辑");
        }
        Long appId = userAnswerEditDTO.getAppId();
        UserAnswer newUserAnswer = UserAnswer.builder()
                .id(userAnswerEditDTO.getId())
                .appId(appId)
                .build();
        List<String> choiceList = userAnswerEditDTO.getChoices();
        if(CollectionUtil.isNotEmpty(choiceList)){
            String choiceStr = JSONUtil.toJsonStr(choiceList);
            newUserAnswer.setChoices(choiceStr);
        }
        updateById(newUserAnswer);
    }

    public QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryDTO userAnswerQueryDTO) {
        QueryWrapper<UserAnswer> queryWrapper = new QueryWrapper<>();
        if (userAnswerQueryDTO == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = userAnswerQueryDTO.getId();
        Long appId = userAnswerQueryDTO.getAppId();
        Integer appType = userAnswerQueryDTO.getAppType();
        Integer scoringStrategy = userAnswerQueryDTO.getScoringStrategy();
        String choices = userAnswerQueryDTO.getChoices();
        Long resultId = userAnswerQueryDTO.getResultId();
        String resultName = userAnswerQueryDTO.getResultName();
        String resultDesc = userAnswerQueryDTO.getResultDesc();
        String resultPicture = userAnswerQueryDTO.getResultPicture();
        Integer resultScore = userAnswerQueryDTO.getResultScore();
        Long userId = userAnswerQueryDTO.getUserId();
        Long notId = userAnswerQueryDTO.getNotId();
        String searchText = userAnswerQueryDTO.getSearchText();
        String sortField = userAnswerQueryDTO.getSortField();
        String sortOrder = userAnswerQueryDTO.getSortOrder();

        // 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("result_name", searchText).or().like("result_desc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(choices), "choices", choices);
        queryWrapper.like(StringUtils.isNotBlank(resultName), "result_name", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "result_desc", resultDesc);
        queryWrapper.like(StringUtils.isNotBlank(resultPicture), "result_picture", resultPicture);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultId), "result_id", resultId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "app_id", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "app_type", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultScore), "result_score", resultScore);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoring_strategy", scoringStrategy);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals("ascend"),
                sortField);
        return queryWrapper;
    }
}
