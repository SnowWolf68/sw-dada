package com.snwolf.dada.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snwolf.dada.domain.dto.ScoringResultAddDTO;
import com.snwolf.dada.domain.dto.ScoringResultEditDTO;
import com.snwolf.dada.domain.dto.ScoringResultQueryDTO;
import com.snwolf.dada.domain.dto.ScoringResultUpdateDTO;
import com.snwolf.dada.domain.entity.ScoringResult;
import com.snwolf.dada.domain.vo.ScoringResultVO;

public interface IScoringResultService extends IService<ScoringResult> {
    void add(ScoringResultAddDTO scoringResultAddDTO);

    void delete(Long id);

    void updateWithResultProp(ScoringResultUpdateDTO scoringResultUpdateDTO);

    ScoringResultVO getByIdWithUserVO(Long id);

    Page<ScoringResultVO> pageQuery(ScoringResultQueryDTO scoringResultQueryDTO);

    void edit(ScoringResultEditDTO scoringResultEditDTO);
}
