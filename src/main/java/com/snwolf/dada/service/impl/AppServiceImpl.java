package com.snwolf.dada.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snwolf.dada.domain.dto.AppAddDTO;
import com.snwolf.dada.domain.dto.AppQueryDTO;
import com.snwolf.dada.domain.dto.ReviewDTO;
import com.snwolf.dada.domain.entity.App;
import com.snwolf.dada.domain.entity.User;
import com.snwolf.dada.domain.vo.AppVO;
import com.snwolf.dada.domain.vo.UserVO;
import com.snwolf.dada.exception.ParamException;
import com.snwolf.dada.exception.ReviewStatusException;
import com.snwolf.dada.mapper.AppMapper;
import com.snwolf.dada.service.IAppService;
import com.snwolf.dada.service.IUserService;
import com.snwolf.dada.utils.SqlUtils;
import com.snwolf.dada.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements IAppService {

    private final IUserService userService;

    @Override
    public Long add(AppAddDTO appAddDTO) {
        App app = BeanUtil.copyProperties(appAddDTO, App.class);
        app.setUserId(UserHolder.getUser().getId());
        save(app);
        return app.getId();
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    @Override
    public AppVO getByIdWithUserVO(Long appId) {
        if(appId == null){
            throw new ParamException("参数不能为空");
        }
        App app = getById(appId);
        Long userId = app.getUserId();
        User user = userService.getById(userId);
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        AppVO appVO = BeanUtil.copyProperties(app, AppVO.class);
        appVO.setUser(userVO);
        return appVO;
    }

    @Override
    public Page<App> pageQuery(AppQueryDTO appQueryDTO) {
        Page<App> page = new Page<>(appQueryDTO.getCurrent(), appQueryDTO.getPageSize());

        appQueryDTO.setReviewStatus(1);
        QueryWrapper<App> appQueryWrapper = getQueryWrapper(appQueryDTO);
        return page(page, appQueryWrapper);
    }

    @Override
    public void review(ReviewDTO reviewDTO) {
        Long appId = reviewDTO.getId();
        App app = getById(appId);
        if(app.getReviewStatus() != 0){
            throw new ReviewStatusException("该应用已审核");
        }
        app = App.builder()
                .id(appId)
                .reviewStatus(reviewDTO.getReviewStatus())
                .reviewMessage(reviewDTO.getReviewMessage())
                .reviewerId(UserHolder.getUser().getId())
                .reviewTime(LocalDateTime.now())
                .build();
        updateById(app);
    }

    public QueryWrapper<App> getQueryWrapper(AppQueryDTO appQueryDTO) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (appQueryDTO == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = appQueryDTO.getId();
        String appName = appQueryDTO.getAppName();
        String appDesc = appQueryDTO.getAppDesc();
        String appIcon = appQueryDTO.getAppIcon();
        Integer appType = appQueryDTO.getAppType();
        Integer scoringStrategy = appQueryDTO.getScoringStrategy();
        Integer reviewStatus = appQueryDTO.getReviewStatus();
        String reviewMessage = appQueryDTO.getReviewMessage();
        Long reviewerId = appQueryDTO.getReviewerId();
        Long userId = appQueryDTO.getUserId();
        Long notId = appQueryDTO.getNotId();
        String searchText = appQueryDTO.getSearchText();
        String sortField = appQueryDTO.getSortField();
        String sortOrder = appQueryDTO.getSortOrder();

        // 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("app_name", searchText).or().like("app_desc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(appName), "app_name", appName);
        queryWrapper.like(StringUtils.isNotBlank(appDesc), "app_desc", appDesc);
        queryWrapper.like(StringUtils.isNotBlank(reviewMessage), "review_message", reviewMessage);
        // 精确查询
        queryWrapper.eq(StringUtils.isNotBlank(appIcon), "app_icon", appIcon);
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "app_type", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoring_strategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus), "review_status", reviewStatus);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewerId), "reviewer_id", reviewerId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals("ascend"),
                sortField);
        return queryWrapper;
    }
}
