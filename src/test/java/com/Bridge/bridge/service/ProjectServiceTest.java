package com.Bridge.bridge.service;

import com.Bridge.bridge.domain.Part;
import com.Bridge.bridge.domain.Project;
import com.Bridge.bridge.domain.User;
import com.Bridge.bridge.dto.ProjectListDto;
import com.Bridge.bridge.dto.PartRequestDto;
import com.Bridge.bridge.dto.ProjectRequestDto;
import com.Bridge.bridge.dto.response.ProjectResponseDto;
import com.Bridge.bridge.repository.ProjectRepository;
import com.Bridge.bridge.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProjectServiceTest {

    @Autowired
    ProjectService projectService;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    UserRepository userRepository;

    @DisplayName("모집글 검색 기능 test")
    @Test
    public void findProjects() {
        // given

        // When
        List<ProjectListDto> result = projectService.findByTitleAndContent("어플");

        // Then
        assertEquals(result.size(), 4);

    }

    @DisplayName("모집글 생성 기능 test")
    @Test
    void createProject() {
        // given
        User user = new User("test1@gmaill.com", "apple");
        userRepository.save(user);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<PartRequestDto> recruit = new ArrayList<>();
        recruit.add(PartRequestDto.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        ProjectRequestDto newProject = ProjectRequestDto.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userEmail(user.getEmail())
                .stage("Before Start")
                .build();

        // when
        Boolean result = projectService.createProject(newProject, user.getId());

        // then
        Assertions.assertThat(result).isEqualTo(true);

        Project project = projectRepository.findByUser_Id(user.getId()).get();
        projectRepository.delete(project);
        userRepository.delete(user);
    }

    @DisplayName("프로젝트 삭제 기능 - 삭제하려는 유저가 DB에 있을 때(올바른 접근)")
    @Test
    void deleteProject() {
        // given
        User user = new User("test2@gmaill.com", "apple");
        userRepository.save(user);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<PartRequestDto> recruit = new ArrayList<>();
        recruit.add(PartRequestDto.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        ProjectRequestDto newProject = ProjectRequestDto.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userEmail(user.getEmail())
                .stage("Before Start")
                .build();

        projectService.createProject(newProject, user.getId());

        Long userId = user.getId();
        Long projectId = projectRepository.findByUser_Id(userId).get().getId();


        // when
        Boolean result = projectService.deleteProject(projectId, userId);

        // then
        Assertions.assertThat(result).isEqualTo(true);
        userRepository.delete(user);

    }

    @DisplayName("프로젝트 삭제 기능 - 삭제하려는 유저가 DB에 없을 때(올바르지 못한 접근)")
    @Test
    void deleteProject_Wrong() {
        // given
        User user1 = new User("ImUser@gmail.com", "apple");
        userRepository.save(user1);

        User user2 = new User("NotUser@gmail.com", "google");
        userRepository.save(user2);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<PartRequestDto> recruit = new ArrayList<>();
        recruit.add(PartRequestDto.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        ProjectRequestDto newProject = ProjectRequestDto.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userEmail(user1.getEmail())
                .stage("Before Start")
                .build();

        projectService.createProject(newProject, user1.getId());

        Long userId = user1.getId();
        Long projectId = projectRepository.findByUser_Id(userId).get().getId();

        // when
        Boolean result = projectService.deleteProject(projectId, user2.getId());

        // then
        Assertions.assertThat(result).isEqualTo(false);

        Project project = projectRepository.findByUser_Id(user1.getId()).get();
        projectRepository.delete(project);

        userRepository.delete(user1);
        userRepository.delete(user2);
    }

    @Test
    @DisplayName("프로젝트 모집글 수정 테스트")
    void updateProject() {
        // given
        User user = new User("update@gmail.com", "updateTest");
        userRepository.save(user);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();
        projectRepository.save(newProject);


        List<String> updateSkill = new ArrayList<>();
        updateSkill.add("Javascript");
        updateSkill.add("React");

        List<PartRequestDto> updateRecruit = new ArrayList<>();
        updateRecruit.add(PartRequestDto.builder()
                .recruitPart("frontend")
                .recruitNum(2)
                .recruitSkill(updateSkill)
                .requirement("화이팅")
                .build());

        ProjectRequestDto updateProject = ProjectRequestDto.builder()
                .title("Update project")
                .overview("This is Updated Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(updateRecruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userEmail(user.getEmail())
                .stage("Before Start")
                .build();


        // when
        ProjectResponseDto result = projectService.updateProject(newProject.getId(), user.getId(), updateProject);

        // then
        Assertions.assertThat(result.getTitle()).isEqualTo("Update project");

        projectRepository.delete(newProject);

        userRepository.delete(user);
    }

    @Test
    @DisplayName("프로젝트 모집글 수정 테스트 _ 잘못된 프로젝트ID")
    void updateProject_wrongProjectId() {
        // given
        User user1 = new User("update@gmail.com", "updateTest");
        userRepository.save(user1);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user1)
                .stage("Before Start")
                .build();
        projectRepository.save(newProject);


        List<String> updateSkill = new ArrayList<>();
        updateSkill.add("Javascript");
        updateSkill.add("React");

        List<PartRequestDto> updateRecruit = new ArrayList<>();
        updateRecruit.add(PartRequestDto.builder()
                .recruitPart("frontend")
                .recruitNum(2)
                .recruitSkill(updateSkill)
                .requirement("화이팅")
                .build());

        ProjectRequestDto updateProject = ProjectRequestDto.builder()
                .title("Update project")
                .overview("This is Updated Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(updateRecruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userEmail(user1.getEmail())
                .stage("Before Start")
                .build();

        Long wrongId = Long.valueOf(123456789);

        // when
        ProjectResponseDto result = projectService.updateProject(wrongId, user1.getId(), updateProject);

        // then
        projectRepository.delete(newProject);

        userRepository.delete(user1);
    }

    @Test
    @DisplayName("프로젝트 모집글 수정 테스트 _ 잘못된 유저ID")
    void updateProject_NotSameWriterandUser() {
        // given
        User user1 = new User("wrongUserID@gmail.com", "updateTest");
        userRepository.save(user1);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user1)
                .stage("Before Start")
                .build();
        projectRepository.save(newProject);


        List<String> updateSkill = new ArrayList<>();
        updateSkill.add("Javascript");
        updateSkill.add("React");

        List<PartRequestDto> updateRecruit = new ArrayList<>();
        updateRecruit.add(PartRequestDto.builder()
                .recruitPart("frontend")
                .recruitNum(2)
                .recruitSkill(updateSkill)
                .requirement("화이팅")
                .build());

        ProjectRequestDto updateProject = ProjectRequestDto.builder()
                .title("Update project")
                .overview("This is Updated Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(updateRecruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userEmail(user1.getEmail())
                .stage("Before Start")
                .build();

        Long wrongId = Long.valueOf(123456789);

        // when
        ProjectResponseDto result = projectService.updateProject(newProject.getId(), wrongId, updateProject);

        // then
        projectRepository.delete(newProject);

        userRepository.delete(user1);
    }

    @Test
    @DisplayName("프로젝트 모집글 수정 테스트 _ 프로젝트 작성자 != 유저")
    void updateProject_wrongUserId() {
        // given
        User user1 = new User("user1@gmail.com", "updateTest");
        userRepository.save(user1);

        User user2 = new User("user2@gmail.com", "updateTest");
        userRepository.save(user2);

        List<String> skill = new ArrayList<>();
        skill.add("Java");
        skill.add("Spring boot");

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart("backend")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user1)
                .stage("Before Start")
                .build();
        projectRepository.save(newProject);


        List<String> updateSkill = new ArrayList<>();
        updateSkill.add("Javascript");
        updateSkill.add("React");

        List<PartRequestDto> updateRecruit = new ArrayList<>();
        updateRecruit.add(PartRequestDto.builder()
                .recruitPart("frontend")
                .recruitNum(2)
                .recruitSkill(updateSkill)
                .requirement("화이팅")
                .build());

        ProjectRequestDto updateProject = ProjectRequestDto.builder()
                .title("Update project")
                .overview("This is Updated Project.")
                .dueDate("2023-09-07")
                .startDate("2023-09-11")
                .endDate("2023-09-30")
                .recruit(updateRecruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userEmail(user1.getEmail())
                .stage("Before Start")
                .build();

        // when
         ProjectResponseDto result = projectService.updateProject(newProject.getId(), user2.getId(), updateProject);


        // then
        projectRepository.delete(newProject);

        userRepository.delete(user1);
        userRepository.delete(user2);
    }


}