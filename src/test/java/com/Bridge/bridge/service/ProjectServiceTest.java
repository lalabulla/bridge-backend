package com.Bridge.bridge.service;

import com.Bridge.bridge.domain.ApplyProject;
import com.Bridge.bridge.domain.Bookmark;
import com.Bridge.bridge.domain.Field;
import com.Bridge.bridge.domain.Part;
import com.Bridge.bridge.domain.Platform;
import com.Bridge.bridge.domain.Profile;
import com.Bridge.bridge.domain.Project;
import com.Bridge.bridge.domain.Stack;
import com.Bridge.bridge.domain.User;
import com.Bridge.bridge.dto.request.FilterRequest;
import com.Bridge.bridge.dto.request.ProjectUpdateRequest;
import com.Bridge.bridge.dto.response.*;
import com.Bridge.bridge.dto.request.PartRequest;
import com.Bridge.bridge.dto.request.ProjectRequest;
import com.Bridge.bridge.exception.conflict.ConflictApplyProjectException;
import com.Bridge.bridge.repository.BookmarkRepository;
import com.Bridge.bridge.exception.notfound.NotFoundProjectException;
import com.Bridge.bridge.repository.ApplyProjectRepository;
import com.Bridge.bridge.repository.ProjectRepository;
import com.Bridge.bridge.repository.SearchWordRepository;
import com.Bridge.bridge.repository.UserRepository;
import com.Bridge.bridge.security.JwtTokenProvider;
import com.google.auth.oauth2.GoogleCredentials;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProjectServiceTest {

    @Autowired
    ProjectService projectService;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BookmarkRepository bookmarkRepository;
    @Autowired
    SearchWordRepository searchWordRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ApplyProjectRepository applyProjectRepository;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @DisplayName("모집글 검색 기능 test")
    @Test
    @Transactional
    public void findProjects() {
        // given
        User user = new User("create", Platform.APPLE, "updateTest");
        userRepository.save(user);

        List<Stack> skill1 = new ArrayList<>();
        skill1.add(Stack.JAVA);
        skill1.add(Stack.SPRINGBOOT);

        List<Stack> skill2 = new ArrayList<>();
        skill1.add(Stack.JAVA);
        skill1.add(Stack.SPRINGBOOT);

        List<Part> recruit1 = new ArrayList<>();
        recruit1.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill1)
                .requirement("아무거나")
                .build());

        List<Part> recruit2 = new ArrayList<>();
        recruit2.add(Part.builder()
                .recruitPart(Field.FRONTEND)
                .recruitNum(2)
                .recruitSkill(skill2)
                .requirement("아무거나")
                .build());

        LocalDateTime time = LocalDateTime.of(2100, 1,1,0,0,0);

        Project newProject1 = Project.builder()
                .title("어플 프로젝트")
                .overview("This is new Project.")
                .dueDate(time)
                .startDate(time)
                .endDate(time)
                .uploadTime(time)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();
        recruit1.get(0).setProject(newProject1);
        projectRepository.save(newProject1);

        Project newProject2 = Project.builder()
                .title("New project")
                .overview("This is 맛집 어프")
                .dueDate(time)
                .startDate(time)
                .endDate(time)
                .uploadTime(time)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();
        recruit2.get(0).setProject(newProject2);
        projectRepository.save(newProject2);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);


        // When
        List<ProjectListResponse> result = projectService.findByTitleAndContent(request,"어플");

        // Then
        assertEquals(result.get(0).getTitle(),"어플 프로젝트" );

    }

    @DisplayName("모집글 생성 기능 test")
    @Test
    void createProject() {
        // given

        User user = new User("create", Platform.APPLE, "updateTest");
        userRepository.save(user);

        List<String> skill = new ArrayList<>();
        skill.add("JAVA");
        skill.add("SPRINGBOOT");

        List<PartRequest> recruit = new ArrayList<>();
        recruit.add(PartRequest.builder()
                .recruitPart("BACKEND")
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        LocalDateTime dueDate = LocalDateTime.of(2050,1,12,0,0,0);
        LocalDateTime startDate = LocalDateTime.of(2023,2,12,0,0,0);
        LocalDateTime endDate = LocalDateTime.of(2023,3,12,0,0,0);

        ProjectRequest newProject = ProjectRequest.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(dueDate.toString())
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .recruit(recruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .userId(user.getId())
                .stage("Before Start")
                .build();

        // when
        Long newProjectId = projectService.createProject(newProject);

        // then
        assertThat(newProjectId).isNotEqualTo(null);
    }

    @DisplayName("프로젝트 삭제 기능 - 삭제하려는 유저가 DB에 있을 때(올바른 접근)")
    @Test
    void deleteProject() {
        // given

        User user = new User("delete", Platform.APPLE, "updateTest");
        userRepository.save(user);

        List<Stack> skill1 = new ArrayList<>();
        skill1.add(Stack.JAVA);
        skill1.add(Stack.SPRINGBOOT);

        List<Part> recruit1 = new ArrayList<>();
        recruit1.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill1)
                .requirement("아무거나")
                .build());

        LocalDateTime dueDate = LocalDateTime.of(2024,1,12,0,0,0);
        LocalDateTime startDate = LocalDateTime.of(2023,2,12,0,0,0);
        LocalDateTime endDate = LocalDateTime.of(2023,3,12,0,0,0);

        Project newProject1 = Project.builder()
                .title("어플 프로젝트")
                .overview("This is new Project.")
                .dueDate(dueDate)
                .startDate(startDate)
                .endDate(endDate)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();

        recruit1.get(0).setProject(newProject1);
        Project saveProject = projectRepository.save(newProject1);

        // when
        Boolean result = projectService.deleteProject(saveProject.getId());

        // then
        assertThat(result).isEqualTo(true);
    }

    @Test
    @DisplayName("프로젝트 모집글 수정 테스트")
    void updateProject() {
        // given
        User user = new User("update", Platform.APPLE, "updateTest");
        userRepository.save(user);

        List<Stack> skill = new ArrayList<>();
        skill.add(Stack.JAVA);
        skill.add(Stack.SPRINGBOOT);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        LocalDateTime dueDate = LocalDateTime.of(2024,1,12,0,0,0);
        LocalDateTime startDate = LocalDateTime.of(2023,2,12,0,0,0);
        LocalDateTime endDate = LocalDateTime.of(2023,3,12,0,0,0);


        Project newProject = Project.builder()
                .title("어플 프로젝트")
                .overview("This is new Project.")
                .dueDate(dueDate)
                .startDate(startDate)
                .endDate(endDate)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();

        recruit.get(0).setProject(newProject);
        projectRepository.save(newProject);


        List<String> updateSkill = new ArrayList<>();
        updateSkill.add("JAVASCRIPT");
        updateSkill.add("REACT");

        List<PartRequest> updateRecruit = new ArrayList<>();
        updateRecruit.add(PartRequest.builder()
                .recruitPart("FRONTEND")
                .recruitNum(2)
                .recruitSkill(updateSkill)
                .requirement("화이팅")
                .build());

        ProjectUpdateRequest updateProject = ProjectUpdateRequest.builder()
                .title("Update project")
                .overview("This is Updated Project.")
                .dueDate(LocalDateTime.of(2024,12,31,0,0,0))
                .startDate(LocalDateTime.of(2024,12,31,0,0,0))
                .endDate(LocalDateTime.of(2025,12,31,0,0,0))
                .recruit(updateRecruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .stage("Before Start")
                .build();


        // when
        ProjectResponse result = projectService.updateProject(newProject.getId(), updateProject);

        // then

        LocalDateTime targetDuedate = LocalDateTime.of(2024,12,31,0,0,0);
        LocalDateTime targetStartdate = LocalDateTime.of(2024,12,31,0,0,0);
        LocalDateTime targetEnddate = LocalDateTime.of(2025,12,31,0,0,0);
        assertThat(result.getTitle()).isEqualTo("Update project");
        assertThat(result.getDueDate()).isEqualTo(targetDuedate+targetDuedate.format(DateTimeFormatter.ofPattern(":ss")));
        assertThat(result.getStartDate()).isEqualTo(targetStartdate+targetStartdate.format(DateTimeFormatter.ofPattern(":ss")));
        assertThat(result.getEndDate()).isEqualTo(targetEnddate+targetEnddate.format(DateTimeFormatter.ofPattern(":ss")));
        System.out.println(result);
    }

    @Test
    @DisplayName("프로젝트 모집글 수정 테스트 _ 잘못된 프로젝트ID")
    void updateProject_wrongProjectId() {
        // given
        User user1 = new User("update", Platform.APPLE, "update1Test");
        userRepository.save(user1);

        List<Stack> skill = new ArrayList<>();
        skill.add(Stack.JAVA);
        skill.add(Stack.SPRINGBOOT);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        LocalDateTime dueDate = LocalDateTime.of(2024,1,12,0,0,0);
        LocalDateTime startDate = LocalDateTime.of(2023,2,12,0,0,0);
        LocalDateTime endDate = LocalDateTime.of(2023,3,12,0,0,0);


        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(dueDate)
                .startDate(startDate)
                .endDate(endDate)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user1)
                .stage("Before Start")
                .build();

        recruit.get(0).setProject(newProject);
        projectRepository.save(newProject);


        List<String> updateSkill = new ArrayList<>();
        updateSkill.add("Javascript");
        updateSkill.add("React");

        List<PartRequest> updateRecruit = new ArrayList<>();
        updateRecruit.add(PartRequest.builder()
                .recruitPart("frontend")
                .recruitNum(2)
                .recruitSkill(updateSkill)
                .requirement("화이팅")
                .build());

        ProjectUpdateRequest updateProject = ProjectUpdateRequest.builder()
                .title("Update project")
                .overview("This is Updated Project.")
                .dueDate(dueDate)
                .startDate(startDate)
                .endDate(endDate)
                .recruit(updateRecruit)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .stage("Before Start")
                .build();

        Long wrongId = Long.valueOf(123456789);

        // expected
        assertThrows(NotFoundProjectException.class, () -> projectService.updateProject(wrongId, updateProject));
    }

    @DisplayName("모집글 상세보기 기능")
    @Test
    void detailProject() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        User user1 = new User("find", Platform.APPLE, "find1Test");
        User saveUser = userRepository.save(user1);

        List<Stack> skill = new ArrayList<>();
        skill.add(Stack.JAVA);
        skill.add(Stack.SPRINGBOOT);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        LocalDateTime dueDate = LocalDateTime.of(2024,1,12,0,0,0);
        LocalDateTime startDate = LocalDateTime.of(2023,2,12,0,0,0);
        LocalDateTime endDate = LocalDateTime.of(2023,3,12,0,0,0);


        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(dueDate)
                .startDate(startDate)
                .endDate(endDate)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user1)
                .stage("Before Start")
                .build();

        recruit.get(0).setProject(newProject);
        Project theProject = projectRepository.save(newProject);

        String token = Jwts.builder()
                .setSubject(String.valueOf(saveUser.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        // when
        ProjectResponse result = projectService.getProject(user1.getId(), theProject.getId());

        // then
        assertThat(result.getTitle()).isEqualTo(newProject.getTitle());
        assertThat(result.isMyProject()).isEqualTo(true);
    }

    @DisplayName("모집글 상세보기 기능 - 잘못된 모집글 Id")
    @Test
    void detailProject_wrongProjectId() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        User user1 = new User("detail_wrong", Platform.APPLE, "detail_wrongTest");
        User saveUser = userRepository.save(user1);

        List<Stack> skill = new ArrayList<>();
        skill.add(Stack.JAVA);
        skill.add(Stack.SPRINGBOOT);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        LocalDateTime dueDate = LocalDateTime.of(2024,1,12,0,0,0);
        LocalDateTime startDate = LocalDateTime.of(2023,2,12,0,0,0);
        LocalDateTime endDate = LocalDateTime.of(2023,3,12,0,0,0);


        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(dueDate)
                .startDate(startDate)
                .endDate(endDate)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user1)
                .stage("Before Start")
                .build();

        recruit.get(0).setProject(newProject);
        Project theProject = projectRepository.save(newProject);

        String token = Jwts.builder()
                .setSubject(String.valueOf(saveUser.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        // expected
        assertThrows(NotFoundProjectException.class,
                () -> {
                    ProjectResponse result = projectService.getProject(user1.getId(), theProject.getId() + Long.valueOf(123));
                });
    }

    @DisplayName("모집글 상세보기 기능 - 내가 만든 프로젝트가 아닌 경우")
    @Test
    @Transactional
    void detailProjectNotMine() {
        // given
        User user1 = new User("writer", Platform.APPLE, "find1Test");
        User user2 = new User("reader", Platform.APPLE, "find2Test");
        userRepository.save(user1);
        userRepository.save(user2);

        List<Stack> skill = new ArrayList<>();
        skill.add(Stack.JAVA);
        skill.add(Stack.SPRINGBOOT);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());

        LocalDateTime dueDate = LocalDateTime.of(2024,1,12,0,0,0);
        LocalDateTime startDate = LocalDateTime.of(2023,2,12,0,0,0);
        LocalDateTime endDate = LocalDateTime.of(2023,3,12,0,0,0);


        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(dueDate)
                .startDate(startDate)
                .endDate(endDate)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user1)
                .stage("Before Start")
                .build();

        recruit.get(0).setProject(newProject);
        projectRepository.save(newProject);
        Bookmark bookmark = Bookmark.builder()
                .project(newProject)
                .user(user2)
                .build();
        bookmarkRepository.save(bookmark);


        // when
        ProjectResponse result = projectService.getProject(user2.getId(), newProject.getId());

        // then
        assertThat(result.isScrap()).isEqualTo(true);
    }
    
    @DisplayName("모집글 필터링")
    @Test
    @Transactional
    void filtering() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        User user = new User("user", Platform.APPLE, "Test");
        userRepository.save(user);

        List<Stack> skill1 = new ArrayList<>();
        skill1.add(Stack.JAVA);
        skill1.add(Stack.SPRINGBOOT);

        List<Stack> skill2 = new ArrayList<>();
        skill2.add(Stack.JAVA);
        skill2.add(Stack.JAVASCRIPT);
        skill2.add(Stack.SPRINGBOOT);
        skill2.add(Stack.NODEJS);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill1)
                .requirement("아무거나")
                .build());
        recruit.add(Part.builder()
                .recruitPart(Field.FRONTEND)
                .recruitNum(1)
                .recruitSkill(skill2)
                .requirement("skill2")
                .build());

        List<Part> recruit2 = new ArrayList<>();
        recruit2.add(Part.builder()
                .recruitPart(Field.FRONTEND)
                .recruitNum(1)
                .recruitSkill(skill2)
                .requirement("skill2")
                .build());

        LocalDateTime dueDate = LocalDateTime.of(2050,1,12,0,0,0);
        LocalDateTime startDate = LocalDateTime.of(2023,2,12,0,0,0);
        LocalDateTime endDate = LocalDateTime.of(2023,3,12,0,0,0);


        Project newProject1 = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(dueDate)
                .startDate(startDate)
                .endDate(endDate)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();

        recruit.stream()
                .forEach((part -> part.setProject(newProject1)));

        Project newProject2 = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(dueDate)
                .startDate(startDate)
                .endDate(endDate)
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();

        recruit2.stream()
                .forEach((part -> part.setProject(newProject2)));

        projectRepository.save(newProject1);
        projectRepository.save(newProject2);

        List<String> findSkills = new ArrayList<>();
//        findSkills.add("REACT");

        FilterRequest filterRequest = FilterRequest.builder()
                .part("FRONTEND")
                .skills(findSkills)
                .build();

        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        // when
        int result = projectService.filterProjectList(request, filterRequest).size();

        // then
        System.out.println(projectService.filterProjectList(request, filterRequest));
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("지원한 프로젝트 목록 반환 - 개수 확인")
    void getApplyProjectsNum() {
        //given
        User user1 = new User("bridge1", Platform.APPLE, "1");
        User user2 = new User("bridge2", Platform.APPLE, "1");
        userRepository.save(user1);

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .user(user1)
                .stage("stage1")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .build();

        Project project2 = Project.builder()
                .title("title2")
                .overview("overview2")
                .user(user1)
                .stage("stage2")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .build();

        projectRepository.save(project1);
        projectRepository.save(project2);

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user2, project1);
        ApplyProject applyProject2 = new ApplyProject();
        applyProject2.setUserAndProject(user2, project2);

        user2.getApplyProjects().add(applyProject1);
        user2.getApplyProjects().add(applyProject2);
        User saveUser2 = userRepository.save(user2);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = Jwts.builder()
                .setSubject(String.valueOf(user2.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        //when
        List<ApplyProjectResponse> applyProjects = projectService.getApplyProjects(request);

        //then
        assertEquals(2, applyProjectRepository.count());
        assertEquals(2, saveUser2.getApplyProjects().size());
        assertEquals(2, applyProjects.size());
    }

    @Test
    @DisplayName("지원한 프로젝트 목록 반환 - 내용 확인")
    void getApplyProjectsDetail() {
        //given
        User user1 = new User("bridge1", Platform.APPLE, "1");

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .stage("stage1")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .build();

        projectRepository.save(project1);

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user1, project1);


        user1.getApplyProjects().add(applyProject1);
        User saveUser1 = userRepository.save(user1);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = Jwts.builder()
                .setSubject(String.valueOf(user1.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        //when
        List<ApplyProjectResponse> applyProjects = projectService.getApplyProjects(request);

        //then
        ApplyProjectResponse response = applyProjects.get(0);
        assertEquals("결과 대기중", response.getStage());
        assertEquals("title1", response.getTitle());
        assertEquals("overview1", response.getOverview());
        assertEquals(String.valueOf(LocalDateTime.of(2050,1,12,0,0,0)), response.getDueDate());
    }
    @Test
    @Transactional
    @DisplayName("프로젝트 지원하기")
    void apply() {
        //given
        User user1 = new User("bridge1", Platform.APPLE, "1");
        User user2 = new User("bridge2", Platform.APPLE, "1");
        userRepository.save(user1);

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .user(user1)
                .stage("stage1")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .build();

        Project saveProject = projectRepository.save(project1);
        User saveUser2 = userRepository.save(user2);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = Jwts.builder()
                .setSubject(String.valueOf(user2.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        //when
        boolean apply = projectService.apply(request, saveProject.getId());

        //then
        assertEquals(1, applyProjectRepository.count());
        assertEquals(1, saveUser2.getApplyProjects().size());
        assertEquals(1, saveProject.getApplyProjects().size());
        assertTrue(apply);
    }

    @Test
    @Transactional
    @DisplayName("프로젝트 지원하기 - 중복지원 시 예외 반환")
    void apply_Dup() {
        //given
        User user1 = new User("bridge1", Platform.APPLE, "1");
        User user2 = new User("bridge2", Platform.APPLE, "1");
        userRepository.save(user1);

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .user(user1)
                .stage("stage1")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .build();

        Project saveProject = projectRepository.save(project1);
        User saveUser2 = userRepository.save(user2);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = Jwts.builder()
                .setSubject(String.valueOf(user2.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        boolean apply = projectService.apply(request, saveProject.getId());

        //when
        assertThrows(ConflictApplyProjectException.class, () -> projectService.apply(request, saveProject.getId()));
    }

    @DisplayName("내가 작성한 모집글")
    @Test
    void findMyProject() {
        // given
        User user = new User("user", Platform.APPLE, "Test");
        userRepository.save(user);

        List<Stack> skill1 = new ArrayList<>();
        skill1.add(Stack.JAVA);
        skill1.add(Stack.SPRINGBOOT);

        List<Stack> skill2 = new ArrayList<>();
        skill2.add(Stack.JAVA);
        skill2.add(Stack.JAVASCRIPT);
        skill2.add(Stack.SPRINGBOOT);
        skill2.add(Stack.NODEJS);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill1)
                .requirement("아무거나")
                .build());

        List<Part> recruit2 = new ArrayList<>();
        recruit2.add(Part.builder()
                .recruitPart(Field.FRONTEND)
                .recruitNum(1)
                .recruitSkill(skill2)
                .requirement("skill2")
                .build());

        Project newProject1 = Project.builder()
                .title("Find MyProject1")
                .overview("This is My Project1")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,2,12,0,0,0))
                .endDate(LocalDateTime.of(2024,3,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();

        recruit.stream()
                .forEach((part -> part.setProject(newProject1)));

        Project newProject2 = Project.builder()
                .title("Find MyProject2")
                .overview("This is My Project2")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("ONline")
                .user(user)
                .stage("Before Start")
                .build();

        recruit2.stream()
                .forEach((part -> part.setProject(newProject2)));

        projectRepository.save(newProject1);
        projectRepository.save(newProject2);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        // when
        List<MyProjectResponse> response = projectService.findMyProjects(request);

        // then
        Assertions.assertThat(response.size()).isEqualTo(2);
    }

    @DisplayName("내가 작성한 모집글 불러오기 - 하나도 없을 때")
    @Test
    void NoProjects() {
        // given
        User user = new User("user", Platform.APPLE, "Test");
        User newUser = userRepository.save(user);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = Jwts.builder()
                .setSubject(String.valueOf(newUser.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        // when
        List<MyProjectResponse> myProjects = projectService.findMyProjects(request);

        //then
        assertEquals(new ArrayList<>(), myProjects);
    }

    @DisplayName("모든 모집글")
    @Test
    void findAllProject() {
        // given
        User user1 = new User("user1", Platform.APPLE, "Test");
        userRepository.save(user1);

        User user2 = new User("user2", Platform.APPLE, "Test");
        userRepository.save(user2);

        User user3 = new User("user3", Platform.APPLE, "Test");
        userRepository.save(user3);

        List<Stack> skill1 = new ArrayList<>();
        skill1.add(Stack.JAVA);
        skill1.add(Stack.SPRINGBOOT);

        List<Stack> skill2 = new ArrayList<>();
        skill2.add(Stack.JAVA);
        skill2.add(Stack.JAVASCRIPT);
        skill2.add(Stack.SPRINGBOOT);
        skill2.add(Stack.NODEJS);

        List<Stack> skill3 = new ArrayList<>();
        skill2.add(Stack.PYTHON);
        skill2.add(Stack.JAVA);
        skill2.add(Stack.SPRINGBOOT);
        skill2.add(Stack.DJANGO);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill1)
                .requirement("아무거나")
                .build());

        List<Part> recruit2 = new ArrayList<>();
        recruit2.add(Part.builder()
                .recruitPart(Field.FRONTEND)
                .recruitNum(1)
                .recruitSkill(skill2)
                .requirement("skill2")
                .build());

        List<Part> recruit3 = new ArrayList<>();
        recruit3.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(5)
                .recruitSkill(skill3)
                .requirement("skill3")
                .build());

        Project newProject1 = Project.builder()
                .title("Find AllProject1")
                .overview("This is My Project1")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user1)
                .stage("Before Start")
                .build();

        recruit.stream()
                .forEach((part -> part.setProject(newProject1)));

        Project newProject2 = Project.builder()
                .title("Find AllProject2")
                .overview("This is My Project2")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("ONline")
                .user(user2)
                .stage("Before Start")
                .build();

        recruit2.stream()
                .forEach((part -> part.setProject(newProject2)));

        Project newProject3 = Project.builder()
                .title("Find AllProject3")
                .overview("This is My Project3")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("ONline")
                .user(user3)
                .stage("Before Start")
                .build();

        recruit3.stream()
                .forEach((part -> part.setProject(newProject3)));

        projectRepository.save(newProject1);
        projectRepository.save(newProject2);
        projectRepository.save(newProject3);

        Bookmark bookmark1 = Bookmark.builder()
                        .project(newProject2)
                        .user(user1)
                        .build();
        bookmarkRepository.save(bookmark1);
        user1.getBookmarks().add(bookmark1);
        newProject2.getBookmarks().add(bookmark1);

        Bookmark bookmark2 = Bookmark.builder()
                .project(newProject3)
                .user(user1)
                .build();
        bookmarkRepository.save(bookmark2);
        user1.getBookmarks().add(bookmark2);
        newProject3.getBookmarks().add(bookmark1);

        // when
        List<ProjectListResponse> response = projectService.allProjects(null);

        // then
        Assertions.assertThat(response.get(1).isScrap()).isEqualTo(false);
        Assertions.assertThat(response.get(2).isScrap()).isEqualTo(false);
    }

    @DisplayName("내 분야 모집글")
    @Test
    void findMyPartProjects() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        User user1 = new User("user", Platform.APPLE, "Test");
        userRepository.save(user1);

        List<Stack> skill1 = new ArrayList<>();
        skill1.add(Stack.JAVA);
        skill1.add(Stack.SPRINGBOOT);

        List<Stack> skill2 = new ArrayList<>();
        skill2.add(Stack.JAVA);
        skill2.add(Stack.JAVASCRIPT);
        skill2.add(Stack.SPRINGBOOT);
        skill2.add(Stack.NODEJS);

        List<Stack> skill3 = new ArrayList<>();
        skill2.add(Stack.PYTHON);
        skill2.add(Stack.JAVA);
        skill2.add(Stack.SPRINGBOOT);
        skill2.add(Stack.DJANGO);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill1)
                .requirement("아무거나")
                .build());

        List<Part> recruit2 = new ArrayList<>();
        recruit2.add(Part.builder()
                .recruitPart(Field.FRONTEND)
                .recruitNum(1)
                .recruitSkill(skill2)
                .requirement("skill2")
                .build());

        List<Part> recruit3 = new ArrayList<>();
        recruit3.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(5)
                .recruitSkill(skill3)
                .requirement("skill3")
                .build());

        Project newProject1 = Project.builder()
                .title("Find AllProject1")
                .overview("This is My Project1")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user1)
                .stage("Before Start")
                .build();

        recruit.stream()
                .forEach((part -> part.setProject(newProject1)));

        Project newProject2 = Project.builder()
                .title("Find AllProject2")
                .overview("This is My Project2")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("ONline")
                .user(user1)
                .stage("Before Start")
                .build();

        recruit2.stream()
                .forEach((part -> part.setProject(newProject2)));

        Project newProject3 = Project.builder()
                .title("Find AllProject3")
                .overview("This is My Project3")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("ONline")
                .user(user1)
                .stage("Before Start")
                .build();

        recruit3.stream()
                .forEach((part -> part.setProject(newProject3)));

        projectRepository.save(newProject1);
        projectRepository.save(newProject2);
        projectRepository.save(newProject3);

        String token = Jwts.builder()
                .setSubject(String.valueOf(user1.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        // when
        List<ProjectListResponse> response = projectService.findMyPartProjects(request, "BACKEND");

        // then
        Assertions.assertThat(response.size()).isEqualTo(2);
    }

    @DisplayName("모집글 마감 기능")
    @Test
    void deadline() {
        // given
        User user = new User("updateDeadline", Platform.APPLE, "updateDeadlineTest");
        userRepository.save(user);

        List<Stack> skill = new ArrayList<>();
        skill.add(Stack.JAVA);
        skill.add(Stack.SPRINGBOOT);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());


        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();

        recruit.get(0).setProject(newProject);
        projectRepository.save(newProject);

        // when
        ProjectResponse response = projectService.closeProject(newProject.getId());

        // then
        Assertions.assertThat(response.getDueDate()).isNotEqualTo(newProject.getDueDate());
    }

    @DisplayName("모집글 마감 기능_이미 마감된 모집글")
    @Test
    void alreadyClosed() {
        // given
        User user = new User("alreadyClosed", Platform.APPLE, "alreadyClosedTest");
        userRepository.save(user);

        List<Stack> skill = new ArrayList<>();
        skill.add(Stack.JAVA);
        skill.add(Stack.SPRINGBOOT);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());


        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(LocalDateTime.of(2023,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();

        recruit.get(0).setProject(newProject);
        projectRepository.save(newProject);

        // when
        System.out.println(assertThrows(IllegalStateException.class, ()-> projectService.closeProject(newProject.getId()))
                .getMessage());
    }

    @DisplayName("모집글 스크랩 기능")
    @Test
    void scrap() {
        // given
        User user = new User("scrap", Platform.APPLE, "scrapTest");
        user = userRepository.save(user);

        List<Stack> skill = new ArrayList<>();
        skill.add(Stack.JAVA);
        skill.add(Stack.SPRINGBOOT);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());


        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();

        recruit.get(0).setProject(newProject);
        newProject = projectRepository.save(newProject);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        // when

        BookmarkResponse bookmarkResponse = projectService.scrap(request, newProject.getId());
        Assertions.assertThat(bookmarkResponse.getScrap()).isEqualTo("스크랩이 설정되었습니다.");

    }

    @DisplayName("모집글 스크랩 해제")
    @Test
    void unscrap() {
        // given
        User user = new User("user1", Platform.APPLE, "Test");
        user = userRepository.save(user);

        List<Stack> skill = new ArrayList<>();
        skill.add(Stack.JAVA);
        skill.add(Stack.SPRINGBOOT);

        List<Part> recruit = new ArrayList<>();
        recruit.add(Part.builder()
                .recruitPart(Field.BACKEND)
                .recruitNum(3)
                .recruitSkill(skill)
                .requirement("아무거나")
                .build());


        Project newProject = Project.builder()
                .title("New project")
                .overview("This is new Project.")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .startDate(LocalDateTime.of(2024,1,12,0,0,0))
                .endDate(LocalDateTime.of(2024,1,12,0,0,0))
                .tagLimit(new ArrayList<>())
                .meetingWay("Offline")
                .user(user)
                .stage("Before Start")
                .build();

        recruit.get(0).setProject(newProject);
        newProject = projectRepository.save(newProject);

        Bookmark newBookmark = Bookmark.builder()
                .user(user)
                .project(newProject)
                .build();

        newBookmark = bookmarkRepository.save(newBookmark);

        // user - bookmark 연관관계 맵핑
        user.setBookmarks(newBookmark);
        userRepository.save(user);

        // project - bookmark 연관관계 맵핑
        newProject.setBookmarks(newBookmark);
        projectRepository.save(newProject);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        // when
        BookmarkResponse bookmarkResponse = projectService.scrap(request, newProject.getId());
        Assertions.assertThat(bookmarkResponse.getScrap()).isEqualTo("스크랩이 해제되었습니다.");
    }

    @Test
    @DisplayName("프로젝트 지원 취소하기")
    @Transactional
    void cancelApply() {
        //given
        User user1 = new User("bridge1", Platform.APPLE, "1");

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .stage("stage1")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .build();

        Project saveProject = projectRepository.save(project1);

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user1, project1);


        user1.getApplyProjects().add(applyProject1);
        User saveUser1 = userRepository.save(user1);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = Jwts.builder()
                .setSubject(String.valueOf(user1.getId()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenProvider.getKey())
                .compact();

        request.addHeader("Authorization", "Bearer " + token);

        //when
        boolean cancelApply = projectService.cancelApply(request, saveProject.getId());

        //then
        assertEquals(0, applyProjectRepository.count());
        assertEquals(0, saveUser1.getApplyProjects().size());
        assertEquals(0, saveProject.getApplyProjects().size());
        assertTrue(cancelApply);
    }

    @Test
    @DisplayName("프로젝트 지원자 목록 - 지원자 수 확인")
    void getApplyUsersNum() {
        //given
        User user1 = new User("bridge1", Platform.APPLE, "1");
        User user2 = new User("bridge2", Platform.APPLE, "2");
        User user3 = new User("bridge3", Platform.APPLE, "3");

        user1.getFields().add(Field.BACKEND);
        user2.getFields().add(Field.FRONTEND);

        Profile profile1 = Profile.builder()
                .career("career1")
                .build();

        Profile profile2 = Profile.builder()
                .career("career2")
                .build();

        user1.updateProfile(profile1);
        user2.updateProfile(profile2);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .stage("stage1")
                .user(user3)
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .build();

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user1, project1);
        ApplyProject applyProject2 = new ApplyProject();
        applyProject2.setUserAndProject(user2, project1);

        project1.getApplyProjects().add(applyProject1);
        project1.getApplyProjects().add(applyProject2);
        Project saveProject = projectRepository.save(project1);


        //when
        List<ApplyUserResponse> applyUsers = projectService.getApplyUsers(saveProject.getId());

        //then
        assertEquals(2, applyUsers.size());
    }

    @Test
    @DisplayName("프로젝트 지원자 목록 - 수락 or 거절한 지원자는 반영x 확인")
    void getApplyUsersNumAcceptOrReject() {
        //given
        User user1 = new User("bridge1", Platform.APPLE, "1");
        User user2 = new User("bridge2", Platform.APPLE, "2");
        User user3 = new User("bridge3", Platform.APPLE, "3");

        user1.getFields().add(Field.BACKEND);
        user2.getFields().add(Field.FRONTEND);


        Profile profile1 = Profile.builder()
                .career("career1")
                .build();

        Profile profile2 = Profile.builder()
                .career("career2")
                .build();

        user1.updateProfile(profile1);
        user2.updateProfile(profile2);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .stage("stage1")
                .user(user3)
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .build();

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user1, project1);
        applyProject1.changeStage("수락");
        ApplyProject applyProject2 = new ApplyProject();
        applyProject2.setUserAndProject(user2, project1);

        project1.getApplyProjects().add(applyProject1);
        project1.getApplyProjects().add(applyProject2);
        Project saveProject = projectRepository.save(project1);


        //when
        List<ApplyUserResponse> applyUsers = projectService.getApplyUsers(saveProject.getId());

        //then
        assertEquals(1, applyUsers.size());
    }

    @Test
    @DisplayName("프로젝트 지원자 목록 - 지원자 내용 확인")
    void getApplyUsersDetail() {
        //given
        User user1 = new User("bridge1", Platform.APPLE, "1");

        user1.getFields().add(Field.BACKEND);

        Profile profile1 = Profile.builder()
                .career("career1")
                .build();

        user1.updateProfile(profile1);

        userRepository.save(user1);

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .stage("stage1")
                .dueDate(LocalDateTime.of(2024,1,12,0,0,0))
                .build();

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user1, project1);

        project1.getApplyProjects().add(applyProject1);
        Project saveProject = projectRepository.save(project1);


        //when
        List<ApplyUserResponse> applyUsers = projectService.getApplyUsers(saveProject.getId());

        //then
        ApplyUserResponse response = applyUsers.get(0);
        assertEquals("bridge1", response.getName());
        assertEquals("백엔드", response.getFields().get(0));
        assertEquals("career1", response.getCareer());
    }

    @Test
    @DisplayName("프로젝트 수락하기 - 일치하는 경우")
    void acceptApply() {
        //given
        User user1 = new User("bridge1", Platform.APPLE, "test");
        User user2 = new User("bridge2", Platform.APPLE, "test2");
        User saveUser2 = userRepository.save(user2);

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .user(user2)
                .stage("stage1")
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .build();

        Project saveProject = projectRepository.save(project1);

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user1, project1);


        user1.getApplyProjects().add(applyProject1);
        User saveUser1 = userRepository.save(user1);

        //when
        projectService.acceptApply(saveProject.getId(), saveUser1.getId());

        //then
        assertEquals("수락", applyProjectRepository.findAll().get(0).getStage());
    }

    @Test
    @DisplayName("프로젝트 거절하기 - 일치하는 경우")
    void rejectApply() throws IOException {
        //given
        User user1 = new User("bridge1", Platform.APPLE, "test");
        User user2 = new User("bridge2", Platform.APPLE, "test2");
        User saveUser2 = userRepository.save(user2);

        Project project1 = Project.builder()
                .title("title1")
                .overview("overview1")
                .stage("stage1")
                .user(saveUser2)
                .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                .build();

        Project saveProject = projectRepository.save(project1);

        ApplyProject applyProject1 = new ApplyProject();
        applyProject1.setUserAndProject(user1, project1);

        user1.getApplyProjects().add(applyProject1);
        User saveUser1 = userRepository.save(user1);



        //when
        projectService.rejectApply(saveProject.getId(), saveUser1.getId());

        //then
        assertEquals("거절", applyProjectRepository.findAll().get(0).getStage());
    }

    @DisplayName("인기글 조회")
    @Test
    @Transactional
    void topProjects() {
        // given
        User user = new User("bridge1", Platform.APPLE, "1");
        userRepository.save(user);

        for(int i=1; i<26; i++){
            List<Part> recruit = new ArrayList<>();
            recruit.add(Part.builder()
                    .recruitPart(Field.BACKEND)
                    .recruitNum(3)
                    .recruitSkill(new ArrayList<>())
                    .requirement("아무거나")
                    .build());
            recruit.add(Part.builder()
                    .recruitPart(Field.FRONTEND)
                    .recruitNum(1)
                    .recruitSkill(new ArrayList<>())
                    .requirement("skill2")
                    .build());

            Project newProject = Project.builder()
                    .title("제목"+i)
                    .dueDate(LocalDateTime.of(2050,1,12,0,0,0))
                    .build();

            recruit.stream().forEach(p -> p.setProject(newProject));

            Project project = projectRepository.save(newProject);

            for(int j=i; j<21; j++){
                project.increaseBookmarksNum();
            }
        }

        // when
        List<TopProjectResponse> result = projectService.topProjects(null);

        // 0초일 경우 초 단위가 출력되지 않는 현상을 방지하기 위해
        String duedate = LocalDateTime.of(2050,1,12,0,0,0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                +"T"
                +LocalDateTime.of(2050,1,12,0,0,0).format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // then
        Assertions.assertThat(result.size()).isEqualTo(20);
        Assertions.assertThat(result.get(0).getTitle()).isEqualTo("제목1");
        Assertions.assertThat(result.get(19).getTitle()).isEqualTo("제목20");
        Assertions.assertThat(result.get(19).getDueDate()).isEqualTo(duedate);
    }

    @DisplayName("인기글 조회_마감 지난 게시글은 제외")
    @Test
    void topProjects_dateOption() {
        // given
        User user = new User("bridge1", Platform.APPLE, "1");
        userRepository.save(user);

        LocalDateTime localDateTime = LocalDateTime.now();
        int year = localDateTime.getYear();
        int month = localDateTime.getMonthValue();
        int day = localDateTime.getDayOfMonth();

        for(int i=1; i<29; i++) {
            List<Part> recruit = new ArrayList<>();
            recruit.add(Part.builder()
                    .recruitPart(Field.BACKEND)
                    .recruitNum(3)
                    .recruitSkill(new ArrayList<>())
                    .requirement("아무거나")
                    .build());
            recruit.add(Part.builder()
                    .recruitPart(Field.FRONTEND)
                    .recruitNum(1)
                    .recruitSkill(new ArrayList<>())
                    .requirement("skill2")
                    .build());

            Project newProject = Project.builder()
                    .title("제목" + i)
                    .dueDate(LocalDateTime.of(year, month, i, 23, 59, 59))
                    .build();

            recruit.stream().forEach(p -> p.setProject(newProject));

            Project project = projectRepository.save(newProject);

            for(int j=i; j<31; j++){
                project.increaseBookmarksNum();
            }
            projectRepository.save(project);
        }

        // when
        List<TopProjectResponse> result = projectService.topProjects(null);

        // then
        Assertions.assertThat(result.size()).isEqualTo(28 - day + 1);
    }

    @DisplayName("마감 임박 프로젝트 조회 기능")
    @Test
    @Transactional
    void getImminentProjects() {
        // given
        User user = new User("bridge1", Platform.APPLE, "1");
        userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();

        for (int i=1; i<29; i++){
            List<Part> recruit = new ArrayList<>();
            recruit.add(Part.builder()
                    .recruitPart(Field.BACKEND)
                    .recruitNum(3)
                    .recruitSkill(new ArrayList<>())
                    .requirement("아무거나")
                    .build());

            Project project = Project.builder()
                    .title("project"+i)
                    .dueDate(LocalDateTime.of(year, month, i,23,59,59))
                    .build();

            recruit.get(0).setProject(project);
            projectRepository.save(project);

        }

        // when
        List<imminentProjectResponse> responses = projectService.getdeadlineImminentProejcts(user.getId());


        // then
        Assertions.assertThat(responses.size()).isEqualTo(28 - day + 1);
        Assertions.assertThat(responses.get(0).getDueDate()).isEqualTo(LocalDateTime.of(year,month,day,23,59,59).toString());
        Assertions.assertThat(responses.get(0).getTitle()).isEqualTo("project" + day);

    }

}
