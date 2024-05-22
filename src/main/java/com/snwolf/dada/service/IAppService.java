package com.snwolf.dada.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snwolf.dada.domain.dto.AppAddDTO;
import com.snwolf.dada.domain.dto.AppQueryDTO;
import com.snwolf.dada.domain.dto.ReviewDTO;
import com.snwolf.dada.domain.entity.App;
import com.snwolf.dada.domain.vo.AppVO;

public interface IAppService extends IService<App> {
    Long add(AppAddDTO appAddDTO);

    void delete(Long id);

    AppVO getByIdWithUserVO(Long id);

    Page<App> pageQuery(AppQueryDTO appQueryDTO);

    void review(ReviewDTO reviewDTO);
}
