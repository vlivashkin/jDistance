package com.jdistance.workflow.task.competition;

import com.jdistance.adapter.generator.GraphBundle;
import com.jdistance.workflow.checker.Checker;
import com.jdistance.workflow.checker.ClustererChecker;

import java.util.List;

public class ClustererCompetitionTask extends CompetitionTask {
    public ClustererCompetitionTask(List<CompetitionDTO> competitionDTOs, GraphBundle forLearning, GraphBundle forCompetitions, Integer pointsCount, String fileName) {
        super(competitionDTOs, forLearning, forCompetitions, pointsCount, fileName);
    }

    @Override
    protected Checker getChecker(GraphBundle graphs, CompetitionDTO dto) {
        return new ClustererChecker(graphs, dto.k);
    }
}
