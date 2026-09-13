package com.example.spacemuseum.service;

import com.example.spacemuseum.dto.StudyGroupDTO;
import com.example.spacemuseum.entity.StudyGroup;
import com.example.spacemuseum.repository.StudyGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StudyGroupService {

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Transactional
    public StudyGroup createStudyGroup(StudyGroupDTO dto) {
        StudyGroup group = new StudyGroup();
        group.setGroupCode(dto.getGroupCode());
        group.setGroupName(dto.getGroupName());
        group.setSchoolName(dto.getSchoolName());
        group.setContactPerson(dto.getContactPerson());
        group.setContactPhone(dto.getContactPhone());
        group.setTotalStudents(dto.getTotalStudents());
        group.setAverageAge(dto.getAverageAge());
        group.setVisitDate(dto.getVisitDate());
        group.setStatus(dto.getStatus());
        group.setRemark(dto.getRemark());
        
        return studyGroupRepository.save(group);
    }

    @Transactional
    public StudyGroup updateStudyGroup(Long id, StudyGroupDTO dto) {
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("研学团不存在"));
        
        group.setGroupCode(dto.getGroupCode());
        group.setGroupName(dto.getGroupName());
        group.setSchoolName(dto.getSchoolName());
        group.setContactPerson(dto.getContactPerson());
        group.setContactPhone(dto.getContactPhone());
        group.setTotalStudents(dto.getTotalStudents());
        group.setAverageAge(dto.getAverageAge());
        group.setVisitDate(dto.getVisitDate());
        group.setStatus(dto.getStatus());
        group.setRemark(dto.getRemark());
        
        return studyGroupRepository.save(group);
    }

    @Transactional
    public void deleteStudyGroup(Long id) {
        studyGroupRepository.deleteById(id);
    }

    public Optional<StudyGroup> getStudyGroupById(Long id) {
        return studyGroupRepository.findById(id);
    }

    public List<StudyGroup> getAllStudyGroups() {
        return studyGroupRepository.findAll();
    }

    public List<StudyGroup> getStudyGroupsByDate(LocalDate visitDate) {
        return studyGroupRepository.findByVisitDate(visitDate);
    }

    public List<StudyGroup> getStudyGroupsByStatus(Integer status) {
        return studyGroupRepository.findByStatus(status);
    }
}