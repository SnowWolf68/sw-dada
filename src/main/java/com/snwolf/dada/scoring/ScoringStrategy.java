package com.snwolf.dada.scoring;

import com.snwolf.dada.domain.entity.App;
import com.snwolf.dada.domain.entity.UserAnswer;

import java.util.List;

public interface ScoringStrategy {

    UserAnswer doScore(List<String> choices, App app);
}
