package com.competition.service;

import com.competition.entity.JudgeAssignment;
import com.competition.repository.JudgeAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JudgeService {

    @Autowired
    private JudgeAssignmentRepository judgeAssignmentRepository;

    public List<JudgeAssignment> getAllAssignments() {
        return judgeAssignmentRepository.findAll();
    }

    public JudgeAssignment createAssignment(JudgeAssignment assignment) {
        return judgeAssignmentRepository.save(assignment);
    }

    public JudgeAssignment updateAssignment(JudgeAssignment assignment) {
        return judgeAssignmentRepository.save(assignment);
    }

    public List<JudgeAssignment> getAssignmentsByJudge(Integer userId) {
        return judgeAssignmentRepository.findByUserId(userId);
    }

    public List<JudgeAssignment> getAssignmentsBySubmission(Integer submissionId) {
        return judgeAssignmentRepository.findBySubmissionId(submissionId);
    }
}
