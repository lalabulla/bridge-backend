package com.Bridge.bridge.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String name;                // 이름

    private String email;               // 이메일

    private String photo;               // 프로필 사진

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;            // 개인 프로필

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Project> projects = new ArrayList<>();             // 내가 만든 프로젝트 모집 글 목록

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ApplyProject> applyProjects = new ArrayList<>();   // 내가 지원한 프로젝트 목록

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Bookmark> bookmarks = new ArrayList<>();           // 내가 북마크한 프로젝트 글 목록

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<JoinChat> joinChats = new ArrayList<>();           // 내가 참여한 채팅방 목록

    @OneToMany(mappedBy = "rcvUser", cascade = CascadeType.ALL)
    private List<Alarm> rcvAlarms = new ArrayList<>();              // 알림 수신 목록

    @OneToMany(mappedBy = "sendUser", cascade = CascadeType.ALL)
    private List<Alarm> sendAlarms = new ArrayList<>();             // 알림 발신 목록
}
